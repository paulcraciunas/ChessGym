package com.paulcraciunas.screens.data.engine

import com.paulcraciunas.domain.api.general.CountdownTimer
import com.paulcraciunas.screens.data.BoardSession
import com.paulcraciunas.screens.data.BoardState
import com.paulcraciunas.screens.data.engine.PlayIntent.AnimationPhase
import com.paulcraciunas.screens.data.engine.PlaySessionConfiguration.EndMode
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.update

@OptIn(ExperimentalAtomicApi::class)
class PlaySession(
    private val settingsRepository: AppSettingsRepository,
    config: PlaySessionConfiguration,
    private val sessions: SessionsSource,
    private val timer: CountdownTimer? = null,
    private val onPlayComplete: OnComplete = {},
    private val onSessionComplete: OnComplete = {},
) {
    private var uiSettingsRef = AtomicReference(UiSettings())
    private val uiSettings: UiSettings
        get() = uiSettingsRef.load()
    private var configuration = AtomicReference(config)
    private val config: PlaySessionConfiguration
        get() = configuration.load()
    private val _state = MutableStateFlow(value = PlaySessionState())
    private val intentChannel = Channel<PlayIntent>(capacity = Channel.UNLIMITED)
    private var timerJob: Job? = null
    private var animationJob: Job? = null

    fun interface OnComplete {
        suspend operator fun invoke(onCompleteState: PlaySessionState)
    }

    fun interface SessionsSource {
        operator fun invoke(): Flow<BoardSession>
    }

    val state = _state.asStateFlow()

    fun reConfig(function: (PlaySessionConfiguration) -> PlaySessionConfiguration) {
        configuration.update { function(it) }
        uiSettingsRef.update { it.copy(autoNext = config.autoNextOverride ?: uiSettings.autoNext) }
    }

    fun accept(intent: PlayIntent) {
        intentChannel.trySend(intent)
    }

    suspend fun run() = coroutineScope {
        intentChannel.flush()
        animationJob?.cancel()
        animationJob = null

        try {
            startRun()
            sessions().collect { session ->
                onNewSession(session)

                while (isActive) {
                    val intent = intentChannel.receive()
                    if (_state.value.status == PlaySessionState.Status.Ready) {
                        _state.update { it.copy(status = PlaySessionState.Status.Playing) }
                    }
                    if (_state.value.status == PlaySessionState.Status.Paused && !intent.resumable) {
                        continue
                    }
                    var outcome = process(intent, session)
                    if (outcome == IntentOutcome.Defeat && config.endMode == EndMode.OnFirstFailure) {
                        outcome = IntentOutcome.End
                    }
                    when (outcome) {
                        IntentOutcome.Continue -> continue
                        IntentOutcome.Defeat,
                        IntentOutcome.Success,
                        -> break
                        IntentOutcome.End -> {
                            onGameOver(session)
                            throw GameEndSignal()
                        }
                    }
                }
                onSessionOver(session)
            }
            onSourceExhausted()
        } catch (_: GameEndSignal) {
            // Game ended normally (timer expired, first failure in rush mode, etc.)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    status = PlaySessionState.Status.Failed,
                    isAnimating = false,
                    errorMessage = e.message,
                )
            }
        } finally {
            timerJob?.cancel()
            timerJob = null
            animationJob?.cancel()
            animationJob = null
        }
        if (_state.value.status == PlaySessionState.Status.Ended) {
            onPlayComplete(_state.value)
        }
    }

    fun clearSummary() { _state.update { it.copy(showSummary = false) } }
    fun reset() { _state.update { PlaySessionState() } }

    private suspend fun CoroutineScope.process(intent: PlayIntent, session: BoardSession): IntentOutcome {
        if (_state.value.isAnimating && intent.blockedByAnimation) {
            return IntentOutcome.Continue
        }
        val outcome = when (intent) {
            is PlayIntent.ExpireTime -> IntentOutcome.End
            is PlayIntent.SessionFinished -> IntentOutcome.Success
            is PlayIntent.SessionFailed -> IntentOutcome.Defeat
            is PlayIntent.SelectSquare -> onMove(session, session.onClick(intent.selection))
            is PlayIntent.Promote -> onMove(session, session.promoteIfPending(intent.to))
            is PlayIntent.Hint -> onHint(session)
            is PlayIntent.RequestAbandon -> onRequestAbandon()
            is PlayIntent.DismissAbandon -> onDismissAbandon()
            is PlayIntent.ConfirmAbandon -> onConfirmAbandon(session)
            is PlayIntent.Navigate -> onNavigate(session, intent.type)
            is PlayIntent.Resume -> onResume(session)
            is PlayIntent.AnimationPhaseComplete -> onAnimationPhaseComplete(intent.phase, session)
        }
        if (outcome == IntentOutcome.End) {
            animationJob?.cancel()
            animationJob = null
        }
        return outcome
    }

    private suspend fun CoroutineScope.onAnimationPhaseComplete(phase: AnimationPhase, session: BoardSession): IntentOutcome =
        when (phase) {
            AnimationPhase.MoveAnimated -> onMoveAnimated(session)
            AnimationPhase.OpponentMoveAnimated -> onOpponentMoveAnimated(session)
            AnimationPhase.BoardSwapped -> onBoardSwapped(session)
            AnimationPhase.SolutionStepAnimated -> onSolutionStepAnimated(session)
        }

    private suspend fun CoroutineScope.onMoveAnimated(session: BoardSession): IntentOutcome {
        val currentBoard = _state.value.boardState
        return if (currentBoard.outcome != null) {
            if (currentBoard.won) IntentOutcome.Success else IntentOutcome.Defeat
        } else {
            if (session.canPlayOpponentMove()) {
                playOpponentMove(session)
            } else {
                _state.update { it.copy(isAnimating = false) }
            }
            IntentOutcome.Continue
        }
    }

    private fun onOpponentMoveAnimated(session: BoardSession): IntentOutcome {
        _state.update { it.copy(isAnimating = false) }
        val boardState = _state.value.boardState
        if (boardState.outcome != null) {
            _state.update { it.copy(results = it.results + session.result()) }
            return if (boardState.won) IntentOutcome.Success else IntentOutcome.Defeat
        }
        return IntentOutcome.Continue
    }

    private suspend fun CoroutineScope.onBoardSwapped(session: BoardSession): IntentOutcome {
        if (session.canPlayOpponentMove()) {
            playOpponentMove(session)
        } else {
            _state.update { it.copy(isAnimating = false) }
        }
        return IntentOutcome.Continue
    }

    private fun CoroutineScope.onSolutionStepAnimated(session: BoardSession): IntentOutcome {
        if (session.solution.hasSolutionMoves()) {
            if (session.playNextSolutionMove()) {
                _state.update { it.copy(boardState = session.boardState(), navigation = session.navigation()) }
            }
            launchAnimationDelay(config.solutionStepDelayMs, AnimationPhase.SolutionStepAnimated)
            return IntentOutcome.Continue
        }
        _state.update { it.copy(isAnimating = false) }
        return IntentOutcome.Defeat
    }

    private suspend fun CoroutineScope.onNewSession(session: BoardSession) {
        if (_state.value.status == PlaySessionState.Status.Paused) return

        session.opponent.init()
        val isFirstSession = _state.value.status == PlaySessionState.Status.Loading
        session.autoPromote = uiSettings.autoPromote
        val shouldAnimate = uiSettings.waitForAnimations && !isFirstSession
        _state.update {
            it.copy(
                status = if (isFirstSession) PlaySessionState.Status.Ready else it.status,
                boardState = session.boardState(),
                isAnimating = shouldAnimate,
                navigation = null,
                abandonRequested = false,
            )
        }
        if (shouldAnimate) {
            launchAnimationDelay(config.boardSwapAnimationMs, AnimationPhase.BoardSwapped)
        } else {
            playOpponentMoveImmediately(session)
        }
    }

    private suspend fun CoroutineScope.onResumeSession(session: BoardSession) {
        session.autoPromote = uiSettings.autoPromote
        val shouldAnimate = uiSettings.waitForAnimations
        _state.update {
            it.copy(
                status = PlaySessionState.Status.Playing,
                boardState = session.boardState(),
                isAnimating = shouldAnimate,
                navigation = null,
                abandonRequested = false,
            )
        }
        if (shouldAnimate) {
            launchAnimationDelay(config.boardSwapAnimationMs, AnimationPhase.BoardSwapped)
        } else {
            playOpponentMoveImmediately(session)
        }
    }

    private suspend fun onSessionOver(session: BoardSession) {
        session.close()
        onSessionComplete(_state.value)
        if (!uiSettings.autoNext) {
            _state.update { it.copy(status = PlaySessionState.Status.Paused) }
        }
    }

    private fun onSourceExhausted() {
        _state.update {
            it.copy(
                status = PlaySessionState.Status.Ended,
                isAnimating = false,
                showSummary = true,
            )
        }
    }

    private suspend fun onGameOver(lastSession: BoardSession) {
        animationJob?.cancel()
        animationJob = null
        lastSession.close()
        _state.update {
            it.copy(
                boardState = lastSession.clear(),
                status = PlaySessionState.Status.Ended,
                remainingTimeMs = 0L,
                isAnimating = false,
                showSummary = true,
            )
        }
    }

    private suspend fun CoroutineScope.startRun() {
        _state.update {
            PlaySessionState(
                status = PlaySessionState.Status.Loading,
                results = emptyList(),
                boardState = BoardState.empty,
                remainingTimeMs = config.timed?.durationInMs ?: 0L,
                hintAvailable = config.hints != null,
                navigation = null,
                isAnimating = false,
                errorMessage = null,
                abandonRequested = false,
                showSummary = false,
            )
        }
        val appSettings = settingsRepository.appSettings.first()
        uiSettingsRef.update {
            UiSettings(
                autoPromote = appSettings.autoPromote,
                waitForAnimations = appSettings.enableAnimations,
                autoNext = config.autoNextOverride ?: appSettings.autoNextPuzzle,
            )
        }
        if (config.timed?.mode == PlaySessionConfiguration.TimedMode.StartImmediately) {
            timerJob = startTimer()
        }
    }

    private suspend fun CoroutineScope.onMove(session: BoardSession, nextBoard: BoardState): IntentOutcome {
        if (!nextBoard.movePlayed) {
            _state.update { it.copy(boardState = nextBoard) }
            return IntentOutcome.Continue
        }

        if (timerJob == null && config.timed?.mode == PlaySessionConfiguration.TimedMode.StartOnClick) {
            timerJob = startTimer()
        }

        return if (nextBoard.outcome != null) {
            _state.update {
                it.copy(
                    results = it.results + session.result(),
                    boardState = nextBoard,
                    navigation = session.navigation(),
                    isAnimating = uiSettings.waitForAnimations,
                )
            }
            if (uiSettings.waitForAnimations) {
                launchAnimationDelay(config.moveAnimationMs, AnimationPhase.MoveAnimated)
                IntentOutcome.Continue
            } else {
                if (nextBoard.won) IntentOutcome.Success else IntentOutcome.Defeat
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

    private fun onHint(session: BoardSession): IntentOutcome {
        _state.update {
            it.copy(
                boardState = session.hint(),
                hintAvailable = config.hints == PlaySessionConfiguration.HintMode.Unlimited,
            )
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

    private fun CoroutineScope.onConfirmAbandon(session: BoardSession): IntentOutcome {
        _state.update {
            it.copy(
                boardState = session.boardState(),
                abandonRequested = false,
                isAnimating = uiSettings.waitForAnimations,
            )
        }
        return if (uiSettings.waitForAnimations && session.solution.hasSolutionMoves()) {
            if (session.playNextSolutionMove()) {
                _state.update { it.copy(boardState = session.boardState(), navigation = session.navigation()) }
                launchAnimationDelay(config.solutionStepDelayMs, AnimationPhase.SolutionStepAnimated)
            }
            IntentOutcome.Continue
        } else {
            IntentOutcome.Defeat
        }
    }

    private fun onNavigate(session: BoardSession, type: PlayIntent.Navigation): IntentOutcome {
        _state.update {
            it.copy(
                boardState = when (type) {
                    PlayIntent.Navigation.ToStart -> session.undoAll()
                    PlayIntent.Navigation.Back -> session.undoLast()
                    PlayIntent.Navigation.Forward -> session.replayNext()
                    PlayIntent.Navigation.ToEnd -> session.replayAll()
                },
                navigation = session.navigation(),
            )
        }
        return IntentOutcome.Continue
    }

    private suspend fun CoroutineScope.onResume(session: BoardSession): IntentOutcome {
        onResumeSession(session)
        return IntentOutcome.Continue
    }

    private suspend fun CoroutineScope.playOpponentMove(session: BoardSession) {
        if (session.playOpponentMove()) {
            _state.update { it.copy(boardState = session.boardState(), navigation = session.navigation()) }
            launchAnimationDelay(config.moveAnimationMs, AnimationPhase.OpponentMoveAnimated)
        } else {
            _state.update { it.copy(isAnimating = false) }
        }
    }

    private suspend fun playOpponentMoveImmediately(session: BoardSession) {
        if (session.playOpponentMove()) {
            val boardState = session.boardState()
            _state.update {
                it.copy(
                    boardState = boardState,
                    navigation = session.navigation(),
                    isAnimating = false,
                )
            }
            if (boardState.outcome != null) {
                _state.update { it.copy(results = it.results + session.result()) }
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

    private fun CoroutineScope.startTimer(): Job = launch {
        timer?.start(config.timed!!.durationInMs, intervalMillis = 100L)
            ?.collect { remainder ->
                if (!remainder.isPositive()) {
                    intentChannel.send(PlayIntent.ExpireTime)
                    return@collect
                } else {
                    val totalMs = (remainder.seconds * 1000L) + remainder.millis
                    _state.update { it.copy(remainingTimeMs = totalMs) }
                }
            }
    }

    private data class UiSettings(
        val autoPromote: Boolean = true,
        val autoNext: Boolean = true,
        val waitForAnimations: Boolean = true,
    )

    private enum class IntentOutcome {
        Continue,
        Success,
        Defeat,
        End,
    }
}

private fun Channel<*>.flush() {
    while (tryReceive().isSuccess) { /* discard stale intents */
    }
}

private class GameEndSignal : Exception()

private fun BoardSession.navigation(): PlaySessionState.Navigation? =
    if (canNavigate()) PlaySessionState.Navigation(
        canGoBack = canUndo(),
        canGoForward = canReplay(),
        completedMoves = completedMoves(),
        algebraicHistory = algebraicHistory(),
    ) else null
