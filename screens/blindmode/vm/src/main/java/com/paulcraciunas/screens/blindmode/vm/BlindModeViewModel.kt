package com.paulcraciunas.screens.blindmode.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.blindmode.BlindModeGameResult
import com.paulcraciunas.domain.api.blindmode.OnBlindModeGameComplete
import com.paulcraciunas.domain.api.general.RandomFactory
import com.paulcraciunas.domain.api.general.Timer
import com.paulcraciunas.game.engine.api.EngineOrchestrator
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.global.qualifiers.DefaultDispatcher
import com.paulcraciunas.global.sounds.SoundCoordinator
import com.paulcraciunas.screens.data.SideSelection
import com.paulcraciunas.screens.data.engine.PlayIntent
import com.paulcraciunas.screens.data.engine.SinglePlaySession
import com.paulcraciunas.screens.data.engine.SingleSessionState
import com.paulcraciunas.screens.data.engine.toSoundEvents
import com.paulcraciunas.screens.data.toSide
import com.paulcraciunas.screens.data.utils.SequentialJob
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import com.paulcraciunas.user.api.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

internal data class VmState(
    val isTrainingMode: Boolean = true,
    val selectedSide: SideSelection = SideSelection.WHITE,
    val isRevealing: Boolean = false,
    val canReveal: Boolean = true,
)

@HiltViewModel
class BlindModeViewModel @Inject constructor(
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    private val timer: Timer,
    private val randomFactory: RandomFactory,
    private val onComplete: OnBlindModeGameComplete,
    sounds: SoundCoordinator,
    engineOrchestrator: EngineOrchestrator,
    appSettingsRepository: AppSettingsRepository,
    userRepository: UserRepository,
) : ViewModel() {
    private val vmState = MutableStateFlow(VmState())
    private val engine = EngineOrchestratorProxy(engineOrchestrator)
    private val session = BlindModeSession(userRepository, engine)
    private val playSession = SinglePlaySession(
        settingsRepository = appSettingsRepository,
        config = blindModeConfiguration(),
    )
    private val runJob = SequentialJob(viewModelScope)

    val uiState: StateFlow<BlindModeUiState> = combine(
        playSession.state,
        vmState,
        engine.isThinking,
    ) { state, vmState, isThinking -> state.toUiState(vmState, isThinking) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BlindModeUiState.Setup()
        )

    init {
        playSession.state
            .toSoundEvents()
            .onEach { sounds.trigger(it) }
            .launchIn(viewModelScope)
    }

    fun onStop() {
        timer.pause()
    }

    fun onStart() {
        timer.resume()
    }

    fun onTrainingModeToggled(enabled: Boolean) = vmState.update { it.copy(isTrainingMode = enabled) }
    fun onSideSelected(side: SideSelection) = vmState.update { it.copy(selectedSide = side) }
    fun onPlayClicked() {
        session.withStartingSide(vmState.value.selectedSide.toSide { randomFactory.nextInt(0, 2) })
        timer.start()
        runJob.launch(dispatcher) {
            playSession.run(session.createSession())
            reportGameComplete()
        }
    }

    fun onSquareClicked(selection: Locus) = accept(intent = PlayIntent.SelectSquare(selection))
    fun onPromote(to: Piece) = accept(intent = PlayIntent.Promote(to))
    fun onResign() = accept(intent = PlayIntent.RequestAbandon)
    fun onReveal() {
        if (!vmState.value.canReveal) return
        viewModelScope.launch {
            vmState.update { it.copy(isRevealing = true, canReveal = it.isTrainingMode) }
            delay(REVEAL_DURATION_MS)
            vmState.update { it.copy(isRevealing = false) }
        }
    }

    fun onPlayAgain() {
        runJob.cancel()
        playSession.reset()
        vmState.update { it.copy(isRevealing = false, canReveal = true) }
    }

    fun onBackPressed(): Boolean {
        val status = playSession.state.value.status
        val isInGame = status == SingleSessionState.Status.Playing || status == SingleSessionState.Status.Ready
        if (isInGame) {
            playSession.accept(intent = PlayIntent.RequestAbandon)
        }
        return isInGame
    }

    fun onAbandonConfirmed() = accept(intent = PlayIntent.ConfirmAbandon)
    fun onAbandonDismissed() = accept(intent = PlayIntent.DismissAbandon)

    private suspend fun reportGameComplete() {
        val finalState = playSession.state.value
        onComplete(
            BlindModeGameResult(
                isPlayerWin = finalState.boardState.won,
                movesPlayed = finalState.navigation?.completedMoves ?: 0,
                timeSpentMillis = timer.elapsed(),
                isTrainingMode = vmState.value.isTrainingMode,
                opponentElo = finalState.boardState.rating ?: 0,
            )
        )
    }

    private fun accept(intent: PlayIntent) {
        if (!vmState.value.isRevealing) {
            playSession.accept(intent)
        }
    }

    private fun SingleSessionState.toUiState(vmState: VmState, isThinking: Boolean): BlindModeUiState = when (status) {
        SingleSessionState.Status.Loading -> BlindModeUiState.Setup(
            isTrainingMode = vmState.isTrainingMode,
            selectedSide = vmState.selectedSide,
        )
        SingleSessionState.Status.Ready,
        SingleSessionState.Status.Playing -> BlindModeUiState.Playing(
            isTrainingMode = vmState.isTrainingMode,
            data = this.boardState,
            moveHistory = this.navigation?.algebraicHistory ?: "",
            isRevealAvailable = this.hintAvailable && vmState.canReveal,
            isAbandonDialogShown = this.abandonRequested,
            isRevealing = vmState.isRevealing,
            isThinking = isThinking,
        )
        SingleSessionState.Status.GameOver,
        SingleSessionState.Status.Failed -> BlindModeUiState.GameOver(
            isTrainingMode = vmState.isTrainingMode,
            data = this.boardState,
            moveHistory = this.navigation?.algebraicHistory ?: "",
        )
    }

    companion object {
        internal const val REVEAL_DURATION_MS = 3000L
    }
}
