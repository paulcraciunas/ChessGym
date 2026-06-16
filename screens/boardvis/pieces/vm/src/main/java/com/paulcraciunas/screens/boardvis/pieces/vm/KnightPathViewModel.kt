package com.paulcraciunas.screens.boardvis.pieces.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.boardvis.GetKnightPathBufferedSeries
import com.paulcraciunas.domain.api.boardvis.KnightPathResult
import com.paulcraciunas.domain.api.boardvis.OnKnightPathComplete
import com.paulcraciunas.domain.api.general.CountdownTimer
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.global.qualifiers.DefaultDispatcher
import com.paulcraciunas.global.sounds.SoundCoordinator
import com.paulcraciunas.screens.data.engine.PlayIntent
import com.paulcraciunas.screens.data.engine.PlaySession
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
class KnightPathViewModel @Inject constructor(
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    private val userRepository: UserRepository,
    sounds: SoundCoordinator,
    appSettingsRepository: AppSettingsRepository,
    exercises: GetKnightPathBufferedSeries,
    onKnightPathComplete: OnKnightPathComplete,
    countdownTimer: CountdownTimer,
) : ViewModel() {
    private val adapter = KnightPathUiStateAdapter()
    private val sessions = KnightPathSessions(exercises)
    private val playSession = PlaySession(
        settingsRepository = appSettingsRepository,
        config = knightPathConfiguration(),
        sessions = sessions,
        timer = countdownTimer,
        onPlayComplete = { state ->
            val successCount = state.results.count { it.success }
            val timeSpent = DURATION_MS - (state.remainingTimeMs ?: 0)
            onKnightPathComplete(KnightPathResult(score = successCount, timeSpentMillis = timeSpent))
        },
    )

    private var runJob = SequentialJob(viewModelScope)
    private val highScore = MutableStateFlow(0)

    val uiState: StateFlow<KnightPathUiState> = combine(
        playSession.state,
        highScore,
    ) { state, score -> adapter.toUiState(state, score) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = KnightPathUiState.Setup
        )

    init {
        playSession.state
            .toSoundEvents(DANGER_THRESHOLD)
            .onEach { sounds.trigger(it) }
            .launchIn(viewModelScope)
    }

    fun onNavigateBackPressed(): Boolean {
        if (uiState.value is KnightPathUiState.Playing) {
            playSession.accept(intent = PlayIntent.RequestAbandon)
        }
        return uiState.value is KnightPathUiState.Playing
    }

    fun onPlayClicked() {
        runJob.launch(dispatcher) {
            highScore.update { userRepository.get().highScores.knightPath }
            playSession.run()
        }
    }

    fun onSquareClicked(selection: Locus) = playSession.accept(intent = PlayIntent.SelectSquare(selection))
    fun onAbandonDismissed() = playSession.accept(intent = PlayIntent.DismissAbandon)
    fun onAbandonConfirmed() = playSession.accept(intent = PlayIntent.ConfirmAbandon)
    fun onPlayAgain() {
        if (uiState.value !is KnightPathUiState.GameOver) return
        playSession.reset()
    }
}
