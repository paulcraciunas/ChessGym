package com.paulcraciunas.screens.puzzles.rush.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.general.CountdownTimer
import com.paulcraciunas.domain.api.puzzles.GetBufferedPuzzleSeries
import com.paulcraciunas.domain.api.puzzles.OnPuzzleRushComplete
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.global.navigation.NavigationDispatcher
import com.paulcraciunas.global.qualifiers.DefaultDispatcher
import com.paulcraciunas.global.sounds.SoundCoordinator
import com.paulcraciunas.screens.data.engine.PlayIntent
import com.paulcraciunas.screens.data.engine.PlaySession
import com.paulcraciunas.screens.data.engine.PlaySessionState
import com.paulcraciunas.screens.data.engine.toSoundEvents
import com.paulcraciunas.screens.data.utils.SequentialJob
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import com.paulcraciunas.user.api.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PuzzleRushViewModel @Inject constructor(
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val adapter: PuzzleRushUiStateAdapter,
    private val userRepository: UserRepository,
    private val navDispatcher: NavigationDispatcher,
    sounds: SoundCoordinator,
    timer: CountdownTimer,
    getBufferedPuzzleSeries: GetBufferedPuzzleSeries,
    onPuzzleRushComplete: OnPuzzleRushComplete,
    appSettingsRepository: AppSettingsRepository,
) : ViewModel() {
    private val playSession = PlaySession(
        timer = timer,
        settingsRepository = appSettingsRepository,
        config = rushConfiguration(),
        sessions = PuzzleRushSessions(getBufferedPuzzleSeries),
        onPlayComplete = PuzzleRushOnComplete(onPuzzleRushComplete),
    )

    private var runJob = SequentialJob(viewModelScope)
    private val highScore = MutableStateFlow(0)

    val uiState: StateFlow<PuzzleRushUiState> = combine(
        playSession.state,
        highScore,
    ) { state, score -> adapter.toUiState(state, score) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PuzzleRushUiState.Loading
        )

    init {
        startRush()
        playSession.state
            .toSoundEvents(PuzzleRushUiStateAdapter.DANGER_THRESHOLD)
            .onEach { sounds.trigger(it) }
            .launchIn(viewModelScope)
    }

    fun onNavigateBackPressed(): Boolean {
        val state = playSession.stateValue
        if (state.status == PlaySessionState.Status.Playing) {
            playSession.accept(intent = PlayIntent.RequestAbandon)
        }
        return state.status == PlaySessionState.Status.Playing
    }

    fun onSquareClicked(selection: Locus) = playSession.accept(intent = PlayIntent.SelectSquare(selection))
    fun onPromote(to: Piece) = playSession.accept(intent = PlayIntent.Promote(to))
    fun onAbandonDismissed() = playSession.accept(intent = PlayIntent.DismissAbandon)
    fun onAbandonConfirmed() = playSession.accept(intent = PlayIntent.ConfirmAbandon)
    fun onPlayAgain() = startRush()
    fun onDismissSummary() = playSession.clearSummary()
    fun onAnalyzeFailedPuzzle(puzzleId: Int) = navDispatcher.navigate(NavigationDispatcher.Destination.Analysis(puzzleId))

    private fun startRush() {
        runJob.launch(defaultDispatcher) {
            highScore.update { userRepository.get().highScores.puzzleRush }
            playSession.run()
        }
    }
}
