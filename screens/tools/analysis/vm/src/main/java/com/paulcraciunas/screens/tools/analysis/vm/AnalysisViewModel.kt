package com.paulcraciunas.screens.tools.analysis.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.analysis.AnalyzePosition
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.model.BoardInteractionHelper
import com.paulcraciunas.screens.common.model.ClickResult
import com.paulcraciunas.screens.common.model.GameNavigation
import com.paulcraciunas.screens.common.model.GamePlayableBoard
import com.paulcraciunas.screens.common.model.PlayableData
import com.paulcraciunas.screens.common.model.Promotion
import com.paulcraciunas.serializer.api.Serializer
import com.paulcraciunas.serializer.di.SerializerFen
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
    private val appSettingsRepository: AppSettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    private val helper = BoardInteractionHelper(navigation = GameNavigation())
    private val adapter = EngineDataAdapter()
    private lateinit var currentGame: GamePlayableBoard
    private var analysisJob: Job? = null
    private var navigationDebounceJob: Job? = null
    private var enableThrottling: Boolean = true

    init {
        viewModelScope.launch {
            appSettingsRepository.appSettings.collect { settings ->
                helper.autoPromote = settings.autoPromote
            }
        }
    }

    internal fun disableThrottling() {
        enableThrottling = false
    }

    fun loadPosition(fen: String? = null, firstMove: String? = null) {
        val targetFen = fen ?: Serializer.STARTING_FEN
        if (firstMove != null) {
            try {
                val game = fenSerializer.from(targetFen)
                currentGame = GamePlayableBoard(game, game.info.turn.other())
                helper.load(currentGame)
                game.play(firstMove)
                beginGame(gameData = helper.refresh())
                return
            } catch (e: Exception) {
                Timber.w(e, "Failed to load FEN position: $fen")
            }
        }
        // The try should return; if there's an exception, as a fallback, we load the default board
        val game = fenSerializer.from(targetFen)
        currentGame = GamePlayableBoard(game, game.info.turn)
        beginGame(gameData = helper.load(currentGame))
    }

    fun onSquareClicked(locus: Locus) {
        if (!helper.isLoaded()) {
            loadPosition()
        }
        applyMoveResult(helper.handleSquareClick(locus))
    }

    fun onPromote(to: Piece) = uiState.value.promotion?.let { promotion ->
        applyMoveResult(helper.promote(to, promotion.at))
    }

    fun onJumpToStart() = navigate { helper.undoAll() }
    fun onPreviousMove() = navigate { helper.undoLast() }
    fun onNextMove() = navigate { helper.replayNext() }
    fun onJumpToEnd() = navigate { helper.replayAll() }

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

    private fun beginGame(gameData: PlayableData) {
        updateState(data = gameData, promotion = null)
        analyze(fen = currentFen(), sideToMove = helper.toMove(), start = true)
    }

    private fun applyMoveResult(result: ClickResult) {
        updateState(data = result.data, promotion = result.promotion)

        if (result.movePlayed) {
            navigationDebounceJob?.cancel()
            analyze(fen = currentFen(), sideToMove = helper.toMove())
        }
    }

    private fun navigate(gameDataSource: () -> PlayableData) {
        updateState(data = gameDataSource(), promotion = null)

        navigationDebounceJob?.cancel()
        navigationDebounceJob = viewModelScope.launch {
            delay(NAVIGATION_DEBOUNCE_MS)
            analyze(fen = currentFen(), sideToMove = helper.toMove())
        }
    }

    private fun updateState(data: PlayableData, promotion: Promotion?) {
        _uiState.update {
            it.copy(
                data = data,
                promotion = promotion,
                canNavigateForward = helper.canReplay(),
                canNavigateBack = helper.canUndo(),
            )
        }
    }

    private fun currentFen(): String = fenSerializer.of(currentGame.game)
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
                .onStart { _uiState.update { it.copy(engineData = null) } }
                .throttle()
                .catch { Timber.e(it, "Analysis error") }
                .collect { result -> _uiState.update { it.copy(engineData = adapter.from(result, sideToMove)) } }
        }
    }

    @OptIn(FlowPreview::class)
    private fun <T> Flow<T>.throttle(): Flow<T> =
        if (enableThrottling) sample(ANALYSIS_SAMPLE_PERIOD_MS) else this

    companion object {
        private const val ANALYSIS_SAMPLE_PERIOD_MS = 200L
        private const val NAVIGATION_DEBOUNCE_MS = 300L
    }
}
