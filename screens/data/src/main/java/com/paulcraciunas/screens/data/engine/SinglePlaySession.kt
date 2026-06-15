package com.paulcraciunas.screens.data.engine

import com.paulcraciunas.screens.data.AbstractBoardSession
import com.paulcraciunas.screens.data.BoardState
import com.paulcraciunas.screens.data.engine.PlayIntent.AnimationPhase
import com.paulcraciunas.screens.data.engine.SingleSessionConfiguration.GameOverBehavior
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SinglePlaySession(
    private val settingsRepository: AppSettingsRepository,
    private val config: SingleSessionConfiguration,
) {
    private val _state = MutableStateFlow(value = SingleSessionState())
    private val intentChannel = Channel<PlayIntent>(capacity = Channel.UNLIMITED)
    private var animationJob: Job? = null
    private var uiSettings = UiSettings()

    val state = _state.asStateFlow()

    fun accept(intent: PlayIntent) {
        intentChannel.trySend(intent)
    }

    fun reset() {
        _state.update { SingleSessionState() }
    }

    fun reportError(message: String?) {
        _state.update { it.copy(status = SingleSessionState.Status.Failed, errorMessage = message) }
    }

    suspend fun run(session: AbstractBoardSession) = coroutineScope {
        intentChannel.flush()
        animationJob?.cancel()
        animationJob = null

        try {
            startRun(session)
            while (isActive) {
                val intent = intentChannel.receive()
                if (_state.value.status == SingleSessionState.Status.Ready) {
                    _state.update { it.copy(status = SingleSessionState.Status.Playing) }
                }
                if (_state.value.status == SingleSessionState.Status.GameOver) {
                    if (!intent.isGameOverAllowed) continue
                }
                when (process(intent, session)) {
                    IntentOutcome.Continue -> continue
                    IntentOutcome.GameOver -> {
                        _state.update { it.copy(status = SingleSessionState.Status.GameOver) }
                        if (config.gameOverBehavior == GameOverBehavior.AllowNavigation) continue
                        session.close()
                        return@coroutineScope
                    }
                    IntentOutcome.End -> {
                        session.close()
                        _state.update {
                            it.copy(
                                status = SingleSessionState.Status.GameOver,
                                boardState = session.clear(),
                                isAnimating = false,
                            )
                        }
                        return@coroutineScope
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.update { it.copy(status = SingleSessionState.Status.Failed, isAnimating = false, errorMessage = e.message) }
        } finally {
            animationJob?.cancel()
            animationJob = null
        }
    }

    private suspend fun CoroutineScope.process(intent: PlayIntent, session: AbstractBoardSession): IntentOutcome {
        if (_state.value.isAnimating && intent.blockedByAnimation) {
            return IntentOutcome.Continue
        }
        return when (intent) {
            is PlayIntent.ExpireTime -> IntentOutcome.End
            is PlayIntent.SessionFinished -> IntentOutcome.GameOver
            is PlayIntent.SessionFailed -> IntentOutcome.GameOver
            is PlayIntent.SelectSquare -> onMove(session, session.onClick(intent.selection))
            is PlayIntent.Promote -> onMove(session, session.promoteIfPending(intent.to))
            is PlayIntent.Hint -> onHint(session)
            is PlayIntent.RequestAbandon -> onRequestAbandon()
            is PlayIntent.DismissAbandon -> onDismissAbandon()
            is PlayIntent.ConfirmAbandon -> onConfirmAbandon(session)
            is PlayIntent.Navigate -> onNavigate(session, intent.type)
            is PlayIntent.Resume -> IntentOutcome.Continue
            is PlayIntent.AnimationPhaseComplete -> onAnimationPhaseComplete(intent.phase, session)
        }
    }

    private suspend fun CoroutineScope.onAnimationPhaseComplete(phase: AnimationPhase, session: AbstractBoardSession): IntentOutcome =
        when (phase) {
            AnimationPhase.MoveAnimated -> onMoveAnimated(session)
            AnimationPhase.OpponentMoveAnimated -> onOpponentMoveAnimated()
            AnimationPhase.BoardSwapped -> onBoardSwapped(session)
            AnimationPhase.SolutionStepAnimated -> onSolutionStepAnimated(session)
        }

    private suspend fun CoroutineScope.onMoveAnimated(session: AbstractBoardSession): IntentOutcome {
        return if (_state.value.boardState.outcome != null) {
            IntentOutcome.GameOver
        } else {
            if (session.canPlayOpponentMove()) {
                playOpponentMove(session)
            } else {
                _state.update { it.copy(isAnimating = false) }
            }
            IntentOutcome.Continue
        }
    }

    private fun onOpponentMoveAnimated(): IntentOutcome {
        _state.update { it.copy(isAnimating = false) }
        if (_state.value.boardState.outcome != null) {
            return IntentOutcome.GameOver
        }
        return IntentOutcome.Continue
    }

    private suspend fun CoroutineScope.onBoardSwapped(session: AbstractBoardSession): IntentOutcome {
        if (session.canPlayOpponentMove()) {
            playOpponentMove(session)
        } else {
            _state.update { it.copy(isAnimating = false) }
        }
        return IntentOutcome.Continue
    }

    private suspend fun startRun(session: AbstractBoardSession) {
        val appSettings = settingsRepository.appSettings.first()
        uiSettings = UiSettings(
            autoPromote = appSettings.autoPromote,
            waitForAnimations = appSettings.enableAnimations,
        )
        _state.update {
            SingleSessionState(
                status = SingleSessionState.Status.Loading,
                boardState = BoardState.empty,
                hintAvailable = config.hints != null,
                navigation = null,
                isAnimating = false,
                errorMessage = null,
                abandonRequested = false,
            )
        }
        session.autoPromote(uiSettings.autoPromote)
        session.opponent.init()
        _state.update {
            it.copy(
                status = SingleSessionState.Status.Ready,
                boardState = session.current(),
                navigation = session.navigation(),
            )
        }
        playOpponentMoveImmediately(session)
    }

    private suspend fun CoroutineScope.onMove(session: AbstractBoardSession, nextBoard: BoardState): IntentOutcome {
        if (!nextBoard.movePlayed) {
            _state.update { it.copy(boardState = nextBoard) }
            return IntentOutcome.Continue
        }

        return if (nextBoard.outcome != null) {
            _state.update {
                it.copy(
                    boardState = nextBoard,
                    navigation = session.navigation(),
                    isAnimating = uiSettings.waitForAnimations,
                )
            }
            if (uiSettings.waitForAnimations) {
                launchAnimationDelay(config.moveAnimationMs, AnimationPhase.MoveAnimated)
                IntentOutcome.Continue
            } else {
                IntentOutcome.GameOver
            }
        } else {
            _state.update {
                it.copy(
                    boardState = nextBoard,
                    navigation = session.navigation(),
                    isAnimating = uiSettings.waitForAnimations,
                )
            }
            if (uiSettings.waitForAnimations) {
                launchAnimationDelay(config.moveAnimationMs, AnimationPhase.MoveAnimated)
            } else {
                playOpponentMoveImmediately(session)
            }
            IntentOutcome.Continue
        }
    }

    private fun onHint(session: AbstractBoardSession): IntentOutcome {
        _state.update {
            if (!it.hintAvailable) it
            else it.copy(boardState = session.hint(), hintAvailable = config.hints == SingleSessionConfiguration.HintMode.Unlimited)
        }
        return IntentOutcome.Continue
    }

    private fun onRequestAbandon(): IntentOutcome {
        _state.update { it.copy(abandonRequested = true) }
        return IntentOutcome.Continue
    }

    private fun onDismissAbandon(): IntentOutcome {
        _state.update { it.copy(abandonRequested = false) }
        return IntentOutcome.Continue
    }

    private fun CoroutineScope.onConfirmAbandon(session: AbstractBoardSession): IntentOutcome {
        _state.update { it.copy(abandonRequested = false) }
        if (uiSettings.waitForAnimations && session.hasSolutionMoves()) {
            _state.update { it.copy(boardState = session.current(), isAnimating = true) }
            if (session.playNextSolutionMove()) {
                _state.update { it.copy(boardState = session.current(), navigation = session.navigation()) }
                launchAnimationDelay(config.solutionStepDelayMs, AnimationPhase.SolutionStepAnimated)
            }
            return IntentOutcome.Continue
        }
        _state.update { it.copy(boardState = session.resign()) }
        return IntentOutcome.GameOver
    }

    private fun CoroutineScope.onSolutionStepAnimated(session: AbstractBoardSession): IntentOutcome {
        if (session.hasSolutionMoves()) {
            if (session.playNextSolutionMove()) {
                _state.update { it.copy(boardState = session.current(), navigation = session.navigation()) }
            }
            launchAnimationDelay(config.solutionStepDelayMs, AnimationPhase.SolutionStepAnimated)
            return IntentOutcome.Continue
        }
        _state.update { it.copy(isAnimating = false) }
        return IntentOutcome.GameOver
    }

    private fun onNavigate(session: AbstractBoardSession, type: PlayIntent.Navigation): IntentOutcome {
        val nextBoard = when (type) {
            PlayIntent.Navigation.ToStart -> session.undoAll()
            PlayIntent.Navigation.Back -> session.undoLast()
            PlayIntent.Navigation.Forward -> session.replayNext()
            PlayIntent.Navigation.ToEnd -> session.replayAll()
        }
        val status = if (nextBoard.outcome != null) {
            SingleSessionState.Status.GameOver
        } else {
            SingleSessionState.Status.Playing
        }
        _state.update {
            it.copy(
                boardState = nextBoard,
                navigation = session.navigation(),
                status = status,
            )
        }
        return IntentOutcome.Continue
    }

    private suspend fun CoroutineScope.playOpponentMove(session: AbstractBoardSession) {
        if (session.playOpponentMove()) {
            _state.update { it.copy(boardState = session.current(), navigation = session.navigation()) }
            launchAnimationDelay(config.moveAnimationMs, AnimationPhase.OpponentMoveAnimated)
        } else {
            _state.update { it.copy(isAnimating = false) }
        }
    }

    private suspend fun playOpponentMoveImmediately(session: AbstractBoardSession) {
        if (session.playOpponentMove()) {
            val boardState = session.current()
            _state.update { it.copy(boardState = boardState, navigation = session.navigation(), isAnimating = false) }
            if (boardState.outcome != null) {
                intentChannel.send(
                    if (boardState.won) PlayIntent.SessionFinished else PlayIntent.SessionFailed
                )
            }
        }
    }

    private fun CoroutineScope.launchAnimationDelay(durationMs: Long, phase: AnimationPhase) {
        animationJob?.cancel()
        animationJob = launch {
            delay(durationMs)
            intentChannel.send(PlayIntent.AnimationPhaseComplete(phase))
        }
    }

    private data class UiSettings(
        val autoPromote: Boolean = true,
        val waitForAnimations: Boolean = true,
    )

    private enum class IntentOutcome {
        Continue,
        GameOver,
        End,
    }
}

private fun Channel<*>.flush() {
    while (tryReceive().isSuccess) { /* discard stale intents */
    }
}

private val PlayIntent.isGameOverAllowed: Boolean
    get() = this is PlayIntent.Navigate || this is PlayIntent.AnimationPhaseComplete

private fun AbstractBoardSession.navigation(): SingleSessionState.Navigation? =
    if (canNavigate()) SingleSessionState.Navigation(
        canGoBack = canUndo(),
        canGoForward = canReplay(),
        completedMoves = completedMoves(),
        algebraicHistory = algebraicHistory(),
    ) else null
