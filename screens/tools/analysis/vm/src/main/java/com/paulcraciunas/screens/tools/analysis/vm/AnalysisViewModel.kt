package com.paulcraciunas.screens.tools.analysis.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.analysis.AnalyzePosition
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.data.BoardState
import com.paulcraciunas.screens.data.GameSessionFactory
import com.paulcraciunas.screens.data.SessionSettings
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
import kotlinx.coroutines.flow.map
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
    appSettingsRepository: AppSettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    private val factory = GameSessionFactory()
    private val session = factory.get()
    private val adapter = EngineDataAdapter()
    private var analysisJob: Job? = null
    private var navigationDebounceJob: Job? = null
    private var enableThrottling: Boolean = true

    init {
        session.bindSettings(viewModelScope, appSettingsRepository.appSettings.map {
            SessionSettings(it.autoPromote, it.enableAnimations)
        })
    }

    internal fun disableThrottling() {
        enableThrottling = false
    }

    fun loadPosition(fen: String? = null, firstMove: String? = null) {
        viewModelScope.launch {
            val targetFen = fen ?: Serializer.STARTING_FEN
            if (firstMove != null) {
                try {
                    val game = fenSerializer.from(targetFen)
                    factory.load(viewModelScope, game, game.info.turn.other())
                    game.play(firstMove)
                    session.refresh()
                    beginGame()
                    return@launch
                } catch (e: Exception) {
                    Timber.w(e, "Failed to load FEN position: $fen")
                }
            }
            val game = fenSerializer.from(targetFen)
            factory.load(viewModelScope, game, game.info.turn)
            beginGame()
        }
    }

    fun onSquareClicked(locus: Locus) {
        if (!session.isLoaded()) {
            loadPosition()
            return
        }
        session.onClick(locus)
        applyMoveAndAnalyze()
    }

    fun onPromote(to: Piece) {
        session.promoteIfPending(to)
        applyMoveAndAnalyze()
    }

    fun onJumpToStart() = navigate { session.undoAll() }
    fun onPreviousMove() = navigate { session.undoLast() }
    fun onNextMove() = navigate { session.replayNext() }
    fun onJumpToEnd() = navigate { session.replayAll() }

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

    private fun beginGame() {
        updateState(session.currentState)
        analyze(fen = currentFen(), sideToMove = session.toMove(), start = true)
    }

    private fun applyMoveAndAnalyze() {
        val data = session.currentState
        updateState(data)
        if (data.movePlayed) {
            navigationDebounceJob?.cancel()
            analyze(fen = currentFen(), sideToMove = session.toMove())
        }
    }

    private fun navigate(action: () -> Unit) {
        action()
        updateState(session.currentState)

        navigationDebounceJob?.cancel()
        navigationDebounceJob = viewModelScope.launch {
            delay(NAVIGATION_DEBOUNCE_MS)
            analyze(fen = currentFen(), sideToMove = session.toMove())
        }
    }

    private fun updateState(data: BoardState) = _uiState.update {
        it.copy(
            data = data,
            canNavigateForward = session.canReplay(),
            canNavigateBack = session.canUndo(),
        )
    }

    private fun currentFen(): String = fenSerializer.of(factory.currentGame())
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
