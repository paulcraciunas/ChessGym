package com.paulcraciunas.screens.tools.analysis.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.analysis.AnalyzePosition
import com.paulcraciunas.game.engine.api.EngineLine
import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.model.BoardViewDataBuilder
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
import kotlin.collections.mutableMapOf
import kotlin.collections.set

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    @param:SerializerFen private val fenSerializer: Serializer,
    private val analyzePosition: AnalyzePosition,
) : ViewModel(), AnalysisScreenInteractor {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    private var game: Game? = null
    private var analysisJob: Job? = null
    private var isFlipped: Boolean = false
    internal var enableThrottling: Boolean = true

    internal fun disableThrottling() {
        enableThrottling = false
    }

    fun loadPosition(fen: String? = null) {
        val targetFen = fen ?: STARTING_FEN
        try {
            val importedGame = fenSerializer.from(targetFen)
            if (importedGame.state == Game.GameState.Ready) {
                importedGame.start()
            }
            game = importedGame
            _uiState.update {
                it.copy(
                    boardData = BoardViewDataBuilder.fromBoard(importedGame.board),
                    playerSide = importedGame.info.turn,
                    captured = HashMap<Side, List<Piece>>().apply {
                        this[Side.WHITE] = emptyList()
                        this[Side.BLACK] = emptyList()
                    },
                )
            }
            startAnalysis(targetFen)
        } catch (e: Exception) {
            Timber.w(e, "Failed to load FEN position: $fen")
        }
    }

    override fun onSquareClicked(locus: Locus) {
        val currentGame = game ?: return
        if (currentGame.state is Game.GameState.Finished) return

        val currentState = _uiState.value
        val currentlySelected = currentState.selectedSquare
        if (currentlySelected != null) {
            handleMoveAttempt(currentlySelected, locus, currentGame)
        } else {
            handleSelection(locus, currentGame)
        }
    }

    override fun onPromote(to: Piece) {
        val currentGame = game ?: return
        val currentState = _uiState.value
        val pending = currentState.pendingPromotion ?: return

        val ply = currentGame.plies(pending.from).find { it.to == pending.to }
        if (ply != null) {
            ply.promote(to)
            currentGame.play(ply)
            onMoveCompleted(currentGame)
        }
        _uiState.value = _uiState.value.copy(
            pendingPromotion = null,
            selectedSquare = null,
            legalMoves = emptyList(),
        )
    }

    override fun onFlipBoard() {
        isFlipped = !isFlipped
        val currentSide = _uiState.value.playerSide
        _uiState.value = _uiState.value.copy(
            playerSide = currentSide.other(),
        )
    }

    override fun onCleared() {
        analysisJob?.cancel()
        // Use GlobalScope or a dedicated long-lived scope for process cleanup
        // to ensure the native engine process is actually killed.
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            withContext(NonCancellable) {
                runCatching { analyzePosition.stopAnalysis() }
                runCatching { analyzePosition.shutdown() }
            }
        }
    }

    private fun handleSelection(locus: Locus, currentGame: Game) {
        val plies = currentGame.plies(locus)
        if (plies.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                selectedSquare = null,
                legalMoves = emptyList(),
            )
        } else {
            _uiState.value = _uiState.value.copy(
                selectedSquare = locus,
                legalMoves = plies.map { it.to },
            )
        }
    }

    private fun handleMoveAttempt(from: Locus, to: Locus, currentGame: Game) {
        val ply = currentGame.plies(from).find { it.to == to }
        if (ply == null) {
            handleSelection(to, currentGame)
            return
        }

        if (ply.isPromotion()) {
            _uiState.value = _uiState.value.copy(
                pendingPromotion = PendingPromotion(from = from, to = to),
                selectedSquare = null,
                legalMoves = emptyList(),
            )
            return
        }

        currentGame.play(ply)
        onMoveCompleted(currentGame)
    }

    private fun onMoveCompleted(currentGame: Game) {
        val capturedPieces = mutableMapOf<Side, MutableList<Piece>>().apply {
            this[Side.WHITE] = mutableListOf()
            this[Side.BLACK] = mutableListOf()
        }
        currentGame.history.forEach { ply ->
            val captured = ply.captured()
            if (captured != null) {
                if (ply.turn == Side.WHITE) {
                    capturedPieces[Side.BLACK]!!.add(captured)
                } else {
                    capturedPieces[Side.WHITE]!!.add(captured)
                }
            }
        }

        _uiState.value = _uiState.value.copy(
            boardData = BoardViewDataBuilder.fromBoard(currentGame.board),
            selectedSquare = null,
            legalMoves = emptyList(),
            captured = capturedPieces,
        )

        val currentFen = fenSerializer.of(currentGame)
        startAnalysis(currentFen)
    }

    @OptIn(FlowPreview::class)
    private fun startAnalysis(fen: String) {
        analysisJob?.cancel()
        analysisJob = viewModelScope.launch {
            analyzePosition.prepare()
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
                            evaluation = result.evaluation,
                            engineLines = result.lines,
                            analysisDepth = result.depth,
                            topMoveArrow = extractTopMoveArrow(result.lines),
                        )
                    }
                }
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
        internal const val STARTING_FEN =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    }
}
