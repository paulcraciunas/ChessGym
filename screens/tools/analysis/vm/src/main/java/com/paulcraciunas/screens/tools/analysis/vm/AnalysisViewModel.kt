package com.paulcraciunas.screens.tools.analysis.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.analysis.AnalyzePosition
import com.paulcraciunas.domain.api.puzzles.GetPuzzleFen
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.global.qualifiers.ApplicationScope
import com.paulcraciunas.global.qualifiers.DefaultDispatcher
import com.paulcraciunas.screens.data.engine.PlayIntent
import com.paulcraciunas.screens.data.engine.SinglePlaySession
import com.paulcraciunas.screens.data.engine.SingleSessionConfiguration
import com.paulcraciunas.screens.data.engine.SingleSessionState
import com.paulcraciunas.serializer.api.Serializer
import com.paulcraciunas.serializer.di.SerializerFen
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    @param:ApplicationScope private val appScope: CoroutineScope,
    private val analyzePosition: AnalyzePosition,
    @SerializerFen fenSerializer: Serializer,
    getPuzzleFen: GetPuzzleFen,
    appSettingsRepository: AppSettingsRepository,
) : ViewModel() {
    private val adapter = EngineDataAdapter()
    private val session = AnalysisSession(fenSerializer, getPuzzleFen)
    private val playSession = SinglePlaySession(
        settingsRepository = appSettingsRepository,
        config = SingleSessionConfiguration(gameOverBehavior = SingleSessionConfiguration.GameOverBehavior.AllowNavigation),
    )
    private val engineData = MutableStateFlow<AnalysisUiState.EngineData?>(null)
    private var runJob: Job? = null
    private var analysisJob: Job? = null
    private var enableThrottling: Boolean = true

    val uiState: StateFlow<AnalysisUiState> = combine(
        engineData,
        playSession.state
    ) { data, state -> state.toUiState(data) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AnalysisUiState()
        )

    internal fun disableThrottling() {
        enableThrottling = false
    }

    fun loadPosition(puzzleId: Int? = null) {
        puzzleId?.let { session.withPuzzle(it) }
        runJob?.cancel()
        analysisJob?.cancel()
        runJob = viewModelScope.launch(dispatcher) {
            playSession.run(session.createSession())
        }
        observePositionChanges()
    }

    fun onSquareClicked(selection: Locus) = playSession.accept(intent = PlayIntent.SelectSquare(selection))
    fun onPromote(to: Piece) = playSession.accept(intent = PlayIntent.Promote(to))
    fun onJumpToStart() = navigate(PlayIntent.Navigation.ToStart)
    fun onPreviousMove() = navigate(PlayIntent.Navigation.Back)
    fun onNextMove() = navigate(PlayIntent.Navigation.Forward)
    fun onJumpToEnd() = navigate(PlayIntent.Navigation.ToEnd)

    override fun onCleared() {
        analysisJob?.cancel()
        appScope.launch {
            withContext(NonCancellable) {
                runCatching { analyzePosition.shutdown() }
                    .onFailure { Timber.w(it, "Failed to shutdown engine") }
            }
        }
    }

    private fun navigate(type: PlayIntent.Navigation) {
        playSession.accept(intent = PlayIntent.Navigate(type))
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observePositionChanges() {
        analysisJob = viewModelScope.launch(dispatcher) {
            analyzePosition.prepare()
            playSession.state
                .filter { it.status == SingleSessionState.Status.Ready || it.status == SingleSessionState.Status.Playing }
                .map { session.moveIndex }
                .distinctUntilChanged()
                .debounce(ANALYSIS_DEBOUNCE_MS)
                .flatMapLatest {
                    engineData.update { null }
                    analyzePosition.analyze(session.fen)
                        .map { fen -> adapter.from(fen, session.turn) }
                }
                .throttle()
                .catch { Timber.e(it, "Analysis error") }
                .collect { result -> engineData.update { result } }
        }
    }

    private fun SingleSessionState.toUiState(engineData: AnalysisUiState.EngineData?): AnalysisUiState = AnalysisUiState(
        data = this.boardState,
        engineData = engineData,
        canNavigateForward = navigation?.canGoForward == true,
        canNavigateBack = navigation?.canGoBack == true,
    )

    @OptIn(FlowPreview::class)
    private fun <T> Flow<T>.throttle(): Flow<T> =
        if (enableThrottling) sample(ANALYSIS_SAMPLE_PERIOD_MS) else this

    companion object {
        private const val ANALYSIS_SAMPLE_PERIOD_MS = 200L
        private const val ANALYSIS_DEBOUNCE_MS = 200L
    }
}
