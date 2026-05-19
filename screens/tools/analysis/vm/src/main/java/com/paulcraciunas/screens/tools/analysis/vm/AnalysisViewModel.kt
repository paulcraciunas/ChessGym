package com.paulcraciunas.screens.tools.analysis.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.analysis.AnalyzePosition
import com.paulcraciunas.game.engine.api.EngineLine
import com.paulcraciunas.game.engine.api.Evaluation
import com.paulcraciunas.game.logic.api.GameInteractor
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.model.GameData
import com.paulcraciunas.screens.common.model.GameViewModelHelper
import com.paulcraciunas.serializer.api.Serializer
import com.paulcraciunas.serializer.di.SerializerFen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    @param:SerializerFen private val fenSerializer: Serializer,
    private val analyzePosition: AnalyzePosition,
    gameInteractor: GameInteractor,
) : ViewModel(), AnalysisScreenInteractor {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    private val helper = GameViewModelHelper(gameInteractor)
    private val moveHistory = AnalysisMoveHistory(fenSerializer)
    private var analysisJob: Job? = null
    private var navigationDebounceJob: Job? = null
    internal var enableThrottling: Boolean = true

    internal fun disableThrottling() {
        enableThrottling = false
    }

    fun loadPosition(fen: String? = null, firstMove: String? = null) {
        val targetFen = fen ?: STARTING_FEN
        if (firstMove != null) {
            try {
                val game = fenSerializer.from(targetFen)
                moveHistory.initialize(targetFen)

                helper.load(game = game, player = game.info.turn.other())
                val gameData = helper.playMove(firstMove)
                helper.lastMove()?.let { lastMove ->
                    if (helper.isPromotion(firstMove) != null) {
                        moveHistory.recordMove(lastMove.from, lastMove.to, helper.isPromotion(firstMove))
                    } else {
                        moveHistory.recordMove(lastMove.from, lastMove.to, null)
                    }
                }
                beginGame(gameData)
                return
            } catch (e: Exception) {
                Timber.w(e, "Failed to load FEN position: $fen")
            }
        }

        val game = fenSerializer.from(targetFen)
        moveHistory.initialize(targetFen)
        beginGame(helper.load(game = game, player = game.info.turn))
    }

    override fun onSquareClicked(locus: Locus) {
        if (!helper.isReady()) {
            loadPosition()
        }

        if (!moveHistory.isAtLatestPosition) {
            moveHistory.truncate()
            val fen = moveHistory.fenAtIndex(moveHistory.currentIndex)
            val game = fenSerializer.from(fen)
            helper.load(game, game.info.turn)
        }

        val result = helper.handleSquareClick(locus)
        applyGameData(result.data)

        if (result.promotion != null) {
            _uiState.update {
                it.copy(
                    pendingPromotion = PendingPromotion(
                        from = result.moveFrom ?: locus,
                        to = result.promotion!!.at,
                    )
                )
            }
            return
        }

        if (result.movePlayed && result.moveFrom != null) {
            moveHistory.recordMove(result.moveFrom!!, locus, null)
            updateNavigationState()
            navigationDebounceJob?.cancel()
            val currentFen = moveHistory.fenAtIndex(moveHistory.currentIndex)
            val sideToMove = moveHistory.sideToMoveAt(moveHistory.currentIndex)
            analyze(fen = currentFen, sideToMove = sideToMove)
        }
    }

    override fun onPromote(to: Piece) {
        val pending = _uiState.value.pendingPromotion ?: return
        val result = helper.promote(to, pending.to)
        applyGameData(result.data)
        _uiState.update { it.copy(pendingPromotion = null) }

        moveHistory.recordMove(pending.from, pending.to, to)
        updateNavigationState()

        navigationDebounceJob?.cancel()
        val currentFen = moveHistory.fenAtIndex(moveHistory.currentIndex)
        val sideToMove = moveHistory.sideToMoveAt(moveHistory.currentIndex)
        analyze(fen = currentFen, sideToMove = sideToMove)
    }

    override fun onJumpToStart() {
        moveHistory.jumpToStart()
        onNavigationChanged()
    }

    override fun onPreviousMove() {
        moveHistory.previousMove()
        onNavigationChanged()
    }

    override fun onNextMove() {
        moveHistory.nextMove()
        onNavigationChanged()
    }

    override fun onJumpToEnd() {
        moveHistory.jumpToEnd()
        onNavigationChanged()
    }

    override fun onCleared() {
        analysisJob?.cancel()
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            withContext(NonCancellable) {
                runCatching { analyzePosition.stopAnalysis() }
                    .onFailure { Timber.w(it, "Failed to stop analysis on cleanup") }
                runCatching { analyzePosition.shutdown() }
                    .onFailure { Timber.w(it, "Failed to shutdown engine on cleanup") }
            }
        }
    }

    private fun beginGame(gameData: GameData) {
        applyGameData(gameData)
        updateNavigationState()
        val currentFen = moveHistory.fenAtIndex(moveHistory.currentIndex)
        val sideToMove = moveHistory.sideToMoveAt(moveHistory.currentIndex)
        analyze(fen = currentFen, sideToMove = sideToMove, start = true)
    }

    private fun onNavigationChanged() {
        val fen = moveHistory.fenAtIndex(moveHistory.currentIndex)
        val game = fenSerializer.from(fen)
        val gameData = helper.load(game, game.info.turn)
        applyGameData(gameData)
        updateNavigationState()

        val sideToMove = game.info.turn
        debounceAnalysis(fen, sideToMove)
    }

    private fun applyGameData(data: GameData) {
        _uiState.update {
            it.copy(
                boardData = data.boardData,
                playerSide = data.player,
                captured = data.captured,
            )
        }
    }

    private fun updateNavigationState() {
        _uiState.update {
            it.copy(
                currentMoveIndex = moveHistory.currentIndex,
                totalMoves = moveHistory.totalMoves,
            )
        }
    }

    private fun debounceAnalysis(fen: String, sideToMove: Side) {
        navigationDebounceJob?.cancel()
        navigationDebounceJob = viewModelScope.launch {
            kotlinx.coroutines.delay(NAVIGATION_DEBOUNCE_MS)
            analyze(fen = fen, sideToMove = sideToMove)
        }
    }

    private fun analyze(fen: String, sideToMove: Side = Side.WHITE, start: Boolean = false) {
        val previousJob = analysisJob
        analysisJob = viewModelScope.launch {
            if (previousJob != null) {
                previousJob.cancel()
                analyzePosition.stopAnalysis()
                previousJob.join()
            }
            if (start) {
                analyzePosition.prepare()
            }
            analyzePosition(fen)
                .onStart {
                    _uiState.update {
                        it.copy(
                            isAnalyzing = true,
                            evaluation = null,
                            engineLines = emptyList(),
                            topMoveArrow = null,
                            analysisDepth = 0,
                        )
                    }
                }
                .throttle()
                .catch { e ->
                    Timber.e(e, "Analysis error")
                    _uiState.update { it.copy(isAnalyzing = false) }
                }
                .onCompletion {
                    _uiState.update { it.copy(isAnalyzing = false) }
                }
                .collect { result ->
                    _uiState.update {
                        it.copy(
                            evaluation = normalizeEvaluation(
                                result.evaluation, sideToMove
                            ),
                            engineLines = result.lines.map { line ->
                                line.copy(
                                    evaluation = normalizeEvaluation(
                                        line.evaluation, sideToMove
                                    )
                                )
                            },
                            analysisDepth = result.depth,
                            topMoveArrow = extractTopMoveArrow(result.lines),
                        )
                    }
                }
        }
    }

    private fun normalizeEvaluation(evaluation: Evaluation, sideToMove: Side): Evaluation {
        if (sideToMove == Side.WHITE) return evaluation
        return when (evaluation) {
            is Evaluation.Centipawns -> Evaluation.Centipawns(-evaluation.value)
            is Evaluation.Mate -> Evaluation.Mate(-evaluation.movesToMate)
        }
    }

    @OptIn(FlowPreview::class)
    private fun <T> Flow<T>.throttle(): Flow<T> =
        if (enableThrottling) sample(ANALYSIS_SAMPLE_PERIOD_MS) else this

    private fun extractTopMoveArrow(lines: List<EngineLine>): MoveArrow? {
        val topMove = lines.firstOrNull()?.moves?.firstOrNull() ?: return null
        return MoveArrow(from = topMove.from, to = topMove.to)
    }

    companion object {
        private const val ANALYSIS_SAMPLE_PERIOD_MS = 200L
        private const val NAVIGATION_DEBOUNCE_MS = 300L
        internal const val STARTING_FEN =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    }
}
