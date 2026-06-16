package com.paulcraciunas.screens.tools.analysis.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.analysis.AnalyzePosition
import com.paulcraciunas.domain.api.puzzles.GetPuzzleFen
import com.paulcraciunas.game.engine.api.Evaluation
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.global.qualifiers.ApplicationScope
import com.paulcraciunas.global.qualifiers.DefaultDispatcher
import com.paulcraciunas.global.sounds.SoundCoordinator
import com.paulcraciunas.screens.data.Outcome
import com.paulcraciunas.screens.data.engine.PlayIntent
import com.paulcraciunas.screens.data.engine.SinglePlaySession
import com.paulcraciunas.screens.data.engine.SingleSessionConfiguration
import com.paulcraciunas.screens.data.engine.SingleSessionState
import com.paulcraciunas.screens.data.engine.toSoundEvents
import com.paulcraciunas.screens.data.utils.SequentialJob
import com.paulcraciunas.serializer.api.Serializer
import com.paulcraciunas.serializer.di.SerializerFen
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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
    private val sounds: SoundCoordinator,
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
    private val vmState = MutableStateFlow<VmState>(VmState())
    private var runJob = SequentialJob(viewModelScope)
    private var analysisJob = SequentialJob(viewModelScope)

    val uiState: StateFlow<AnalysisUiState> = combine(
        vmState,
        playSession.state
    ) { vmState, state -> state.toUiState(vmState) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AnalysisUiState()
        )

    init {
        playSession.state
            .toSoundEvents()
            .onEach { sounds.trigger(it) }
            .launchIn(viewModelScope)
    }

    fun loadPosition(puzzleId: Int? = null) {
        puzzleId?.let { session.withPuzzle(it) }
        runJob.launch(dispatcher) {
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
    fun onFlipBoard() = vmState.update { it.copy(orientation = it.orientation.other()) }

    override fun onCleared() {
        analysisJob.cancel()
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
        analysisJob.launch(dispatcher) {
            analyzePosition.prepare()
            playSession.state
                .filter { it.status == SingleSessionState.Status.Ready || it.status == SingleSessionState.Status.Playing }
                .map { session.moveIndex }
                .distinctUntilChanged()
                .debounce(ANALYSIS_DEBOUNCE_MS)
                .flatMapLatest {
                    vmState.update { it.copy(engineData = null) }
                    analyzePosition.analyze(session.fen)
                        .map { fen -> adapter.from(fen, session.turn) }
                        .catch { Timber.e(it, "Analysis error") }
                }
                .collect { result -> vmState.update { it.copy(engineData = result) } }
        }
    }

    private fun SingleSessionState.toUiState(vmState: VmState): AnalysisUiState = AnalysisUiState(
        data = this.boardState,
        orientation = vmState.orientation,
        engineData = vmState.engineData.withGameResult(boardState.outcome, boardState.player),
        canNavigateForward = navigation?.canGoForward == true,
        canNavigateBack = navigation?.canGoBack == true,
    )

    private fun AnalysisUiState.EngineData?.withGameResult(outcome: Outcome?, player: Side): AnalysisUiState.EngineData? {
        if (outcome == null) return this

        val isWhiteWinner = (outcome == Outcome.Won && player == Side.WHITE)
            || (outcome == Outcome.Lost && player == Side.BLACK)
        val display = when (outcome) {
            Outcome.Won -> if (player == Side.WHITE) "1-0" else "0-1"
            Outcome.Lost -> if (player == Side.WHITE) "0-1" else "1-0"
            Outcome.Drew -> "1/2-1/2"
        }
        val fraction = when {
            outcome == Outcome.Drew -> 0.5f
            isWhiteWinner -> Evaluation.MAX_FRACTION
            else -> Evaluation.MIN_FRACTION
        }
        return AnalysisUiState.EngineData(
            evaluation = AnalysisUiState.EngineData.CurrentEvaluation(
                normalised = fraction,
                display = display,
            ),
            engineLines = emptyList(),
            topMove = null,
            analysisDepth = 0,
        )
    }

    private data class VmState(
        val orientation: Side = Side.WHITE,
        val engineData: AnalysisUiState.EngineData? = null,
    )

    companion object {
        private const val ANALYSIS_DEBOUNCE_MS = 200L
    }
}
