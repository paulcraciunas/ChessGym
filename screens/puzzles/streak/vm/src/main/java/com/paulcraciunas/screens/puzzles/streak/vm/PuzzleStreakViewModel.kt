package com.paulcraciunas.screens.puzzles.streak.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.general.Timer
import com.paulcraciunas.domain.api.puzzles.GetStreakPuzzle
import com.paulcraciunas.domain.api.puzzles.OnStreakComplete
import com.paulcraciunas.domain.api.puzzles.OnStreakPuzzleComplete
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PuzzleStreakViewModel @Inject constructor(
    private val timer: Timer,
    private val userRepository: UserRepository,
    private val appSettingsRepository: AppSettingsRepository,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    getStreakPuzzle: GetStreakPuzzle,
    onStreakPuzzleComplete: OnStreakPuzzleComplete,
    onStreakComplete: OnStreakComplete,
    sounds: SoundCoordinator,
) : ViewModel() {
    private val sessions = PuzzleStreakSessions(getStreakPuzzle)
    private val adapter = PuzzleStreakUiStateAdapter(sessions)
    private val playSession = PlaySession(
        settingsRepository = appSettingsRepository,
        config = streakConfiguration(),
        sessions = sessions,
        onPlayComplete = { onStreakComplete(timer.elapsed()) },
        onSessionComplete = { if (it.boardState.won) onStreakPuzzleComplete(timer.elapsed()) },
    )

    private val runJob = SequentialJob(viewModelScope)
    private val highScore = MutableStateFlow(0)

    val uiState: StateFlow<PuzzleStreakUiState> = combine(
        playSession.state,
        highScore,
    ) { state, score -> adapter.toUiState(state, score) }
        .antiFlicker()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PuzzleStreakUiState.Loading,
        )

    init {
        startNewStreak()
        playSession.state
            .toSoundEvents()
            .onEach { sounds.trigger(it) }
            .launchIn(viewModelScope)
    }

    fun onStop() { timer.pause() }
    fun onStart() { timer.resume() }

    fun onNavigateBackPressed(): Boolean {
        val state = playSession.stateValue
        if (state.status == PlaySessionState.Status.Playing) {
            playSession.accept(intent = PlayIntent.RequestAbandon)
        }
        return state.status == PlaySessionState.Status.Playing
    }

    fun onSquareClicked(selection: Locus) = playSession.accept(intent = PlayIntent.SelectSquare(selection))
    fun onPromote(to: Piece) = playSession.accept(intent = PlayIntent.Promote(to))
    fun onHintRequested() = playSession.accept(intent = PlayIntent.Hint)
    fun onAbandon() = playSession.accept(intent = PlayIntent.RequestAbandon)
    fun onAbandonDismissed() = playSession.accept(intent = PlayIntent.DismissAbandon)
    fun onAbandonConfirmed() = playSession.accept(intent = PlayIntent.ConfirmAbandon)
    fun onDismissSummary() = playSession.clearSummary()
    fun onNextPuzzle() = playSession.accept(intent = PlayIntent.Resume)
    fun onNewStreak() {
        if (uiState.value is PuzzleStreakUiState.StreakEnded) {
            startNewStreak()
        }
    }

    fun onAutoNext(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsRepository.updateAutoNextPuzzle(enabled)
        }
    }

    private fun startNewStreak() {
        timer.start()
        runJob.launch(defaultDispatcher) {
            highScore.value = userRepository.get().highScores.puzzleStreak
            playSession.run()
        }
    }
}

private fun Flow<PuzzleStreakUiState>.antiFlicker(): Flow<PuzzleStreakUiState> =
    scan(PuzzleStreakUiState.Loading as PuzzleStreakUiState) { prev, current ->
        if (current is PuzzleStreakUiState.Loading && prev is PuzzleStreakUiState.WithBoard) {
            PuzzleStreakUiState.ReLoad(data = prev.data, streakCount = prev.streakCount)
        } else {
            current
        }
    }
