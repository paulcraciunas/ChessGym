package com.paulcraciunas.screens.data.engine

import com.paulcraciunas.domain.api.general.CountdownTimerV2
import com.paulcraciunas.screens.data.BoardSession
import com.paulcraciunas.screens.data.BoardState
import com.paulcraciunas.screens.data.engine.PlayIntent.AnimationPhase
import com.paulcraciunas.screens.data.engine.PlaySessionConfiguration.EndMode
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
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
    private val defaultDispatcher: CoroutineDispatcher,
    private val settingsRepository: AppSettingsRepository,
    config: PlaySessionConfiguration,
    private val sessions: SessionsSource,
    private val timer: CountdownTimerV2? = null,
    private val onPlayComplete: OnComplete = {},
    private val onSessionComplete: OnComplete = {},
) {
    private var configuration = AtomicReference(config)
    private val config: PlaySessionConfiguration
        get() = configuration.load()
    private val _state = MutableStateFlow(value = PlaySessionState())

    private var intentChannel: Channel<PlayIntent>? = null // TODO Paul: perhaps this shouldn't be mutable after all
    private var runJob: Job? = null
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
    }

    fun accept(intent: PlayIntent) {
        intentChannel?.trySend(intent)
    }

    fun run(scope: CoroutineScope) {
        runJob?.cancel()
        animationJob?.cancel()
        animationJob = null

        val channel = Channel<PlayIntent>(capacity = Channel.UNLIMITED)
        intentChannel = channel

        runJob = scope.launch(defaultDispatcher) {
            try {
                startRun(scope)
                sessions().collect { session ->
                    onNewSession(scope, session, channel)

                    while (isActive) {
                        val intent = channel.receive()
                        if (_state.value.status == PlaySessionState.Status.Ready) {
                            _state.update { it.copy(status = PlaySessionState.Status.Playing) }
                        }
                        if (_state.value.status == PlaySessionState.Status.Paused && !intent.resumable) {
                            continue
                        }
                        var outcome = process(scope, intent, session, channel)
                        if (outcome == IntentOutcome.Defeat && config.endMode == EndMode.OnFirstFailure) {
                            outcome = IntentOutcome.End
                        }
                        when (outcome) {
                            IntentOutcome.Continue -> continue
                            IntentOutcome.Defeat,
                            IntentOutcome.Success -> break
                            IntentOutcome.End -> {
                                onGameOver(session)
                                return@collect
                            }
                        }
                    }
                    onSessionOver(session)
                }
                onSourceExhausted()
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
                channel.close()
                intentChannel = null
            }
            if (_state.value.status == PlaySessionState.Status.Ended) {
                onPlayComplete(_state.value)
            }
        }
    }

    fun clearSummary() {
       if (runJob == null) {
           _state.update { it.copy(showSummary = false) }
       }
    }

    fun reset() {
        if (runJob == null) {
            _state.update { PlaySessionState() }
        }
    }

    private suspend fun process(
        scope: CoroutineScope,
        intent: PlayIntent,
        session: BoardSession,
        channel: Channel<PlayIntent>,
    ): IntentOutcome {
        if (_state.value.isAnimating && intent.blockedByAnimation) {
            return IntentOutcome.Continue
        }
        val outcome = when (intent) {
            is PlayIntent.ExpireTime -> onExpireTime()
            is PlayIntent.SessionFinished -> IntentOutcome.Success
            is PlayIntent.SessionFailed -> IntentOutcome.Defeat
            is PlayIntent.SelectSquare -> onMove(scope, session, session.onClick(intent.selection), channel)
            is PlayIntent.Promote -> onMove(scope, session, session.promoteIfPending(intent.to), channel)
            is PlayIntent.Hint -> onHint(session)
            is PlayIntent.RequestAbandon -> onRequestAbandon()
            is PlayIntent.DismissAbandon -> onDismissAbandon()
            is PlayIntent.ConfirmAbandon -> onConfirmAbandon(scope, session, channel)
            is PlayIntent.Navigate -> onNavigate(session, intent.type)
            is PlayIntent.Resume -> onResume(scope, session, channel)
            is PlayIntent.AnimationPhaseComplete -> onAnimationPhaseComplete(scope, intent.phase, session, channel)
        }
        if (outcome == IntentOutcome.End) {
            animationJob?.cancel()
            animationJob = null
        }
        return outcome
    }

    private suspend fun onAnimationPhaseComplete(
        scope: CoroutineScope,
        phase: AnimationPhase,
        session: BoardSession,
        channel: Channel<PlayIntent>,
    ): IntentOutcome = when (phase) {
        AnimationPhase.MoveAnimated -> onMoveAnimated(scope, session, channel)
        AnimationPhase.OpponentMoveAnimated -> onOpponentMoveAnimated()
        AnimationPhase.BoardSwapped -> onBoardSwapped(scope, session, channel)
        AnimationPhase.SolutionStepAnimated -> onSolutionStepAnimated(scope, session, channel)
    }

    private suspend fun onMoveAnimated(
        scope: CoroutineScope,
        session: BoardSession,
        channel: Channel<PlayIntent>,
    ): IntentOutcome {
        val currentBoard = _state.value.boardState
        return if (currentBoard.outcome != null) {
            if (currentBoard.won) IntentOutcome.Success else IntentOutcome.Defeat
        } else {
            if (session.canPlayOpponentMove()) {
                playOpponentMove(scope, session, channel)
            } else {
                _state.update { it.copy(isAnimating = false) }
            }
            IntentOutcome.Continue
        }
    }

    private fun onOpponentMoveAnimated(): IntentOutcome {
        _state.update { it.copy(isAnimating = false) }
        return IntentOutcome.Continue
    }

    private suspend fun onBoardSwapped(
        scope: CoroutineScope,
        session: BoardSession,
        channel: Channel<PlayIntent>,
    ): IntentOutcome {
        if (session.canPlayOpponentMove()) {
            playOpponentMove(scope, session, channel)
        } else {
            _state.update { it.copy(isAnimating = false) }
        }
        return IntentOutcome.Continue
    }

    private fun onSolutionStepAnimated(
        scope: CoroutineScope,
        session: BoardSession,
        channel: Channel<PlayIntent>,
    ): IntentOutcome {
        if (session.solution.hasSolutionMoves()) {
            if (session.playNextSolutionMove()) {
                _state.update { it.copy(boardState = session.boardState(), navigation = session.navigation()) }
            }
            launchAnimationDelay(scope, config.solutionStepDelayMs, AnimationPhase.SolutionStepAnimated, channel)
            return IntentOutcome.Continue
        }
        _state.update { it.copy(isAnimating = false) }
        return IntentOutcome.Defeat
    }

    private suspend fun onNewSession(
        scope: CoroutineScope,
        session: BoardSession,
        channel: Channel<PlayIntent>,
    ) {
        if (_state.value.status == PlaySessionState.Status.Paused) return

        val isFirstSession = _state.value.status == PlaySessionState.Status.Loading
        session.autoPromote = config.autoPromote
        val shouldAnimate = config.waitForAnimations && !isFirstSession
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
            launchAnimationDelay(scope, config.boardSwapAnimationMs, AnimationPhase.BoardSwapped, channel)
        } else {
            playOpponentMoveImmediately(session)
        }
    }

    private suspend fun onResumeSession(
        scope: CoroutineScope,
        session: BoardSession,
        channel: Channel<PlayIntent>,
    ) {
        session.autoPromote = config.autoPromote
        val shouldAnimate = config.waitForAnimations
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
            launchAnimationDelay(scope, config.boardSwapAnimationMs, AnimationPhase.BoardSwapped, channel)
        } else {
            playOpponentMoveImmediately(session)
        }
    }

    private suspend fun onSessionOver(session: BoardSession) {
        session.close()
        onSessionComplete(_state.value)
        if (!config.autoNext) {
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

    private suspend fun startRun(scope: CoroutineScope) {
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
        configuration.update {
            config.copy(
                autoPromote = appSettings.autoPromote,
                waitForAnimations = appSettings.enableAnimations,
                autoNext = appSettings.autoNextPuzzle,
            )
        }
        if (config.timed?.mode == PlaySessionConfiguration.TimedMode.StartImmediately) {
            timerJob = startTimer(scope)
        }
    }

    private suspend fun onMove(
        scope: CoroutineScope,
        session: BoardSession,
        nextBoard: BoardState,
        channel: Channel<PlayIntent>,
    ): IntentOutcome {
        if (!nextBoard.movePlayed) {
            _state.update { it.copy(boardState = nextBoard) }
            return IntentOutcome.Continue
        }

        if (timerJob == null && config.timed?.mode == PlaySessionConfiguration.TimedMode.StartOnClick) {
            timerJob = startTimer(scope)
        }

        return if (nextBoard.outcome != null) {
            _state.update {
                it.copy(
                    results = it.results + session.result(),
                    boardState = nextBoard,
                    navigation = session.navigation(),
                    isAnimating = config.waitForAnimations,
                )
            }
            if (config.waitForAnimations) {
                launchAnimationDelay(scope, config.moveAnimationMs, AnimationPhase.MoveAnimated, channel)
                IntentOutcome.Continue
            } else {
                if (nextBoard.won) IntentOutcome.Success else IntentOutcome.Defeat
            }
        } else {
            _state.update {
                it.copy(
                    boardState = nextBoard,
                    navigation = session.navigation(),
                    isAnimating = config.waitForAnimations,
                )
            }
            if (config.waitForAnimations) {
                launchAnimationDelay(scope, config.moveAnimationMs, AnimationPhase.MoveAnimated, channel)
            } else {
                playOpponentMoveImmediately(session)
            }
            IntentOutcome.Continue
        }
    }

    private fun onExpireTime(): IntentOutcome = IntentOutcome.End

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

    private fun onConfirmAbandon(
        scope: CoroutineScope,
        session: BoardSession,
        channel: Channel<PlayIntent>,
    ): IntentOutcome {
        _state.update {
            it.copy(
                boardState = session.boardState(),
                abandonRequested = false,
                isAnimating = config.waitForAnimations,
            )
        }
        return if (config.waitForAnimations && session.solution.hasSolutionMoves()) {
            if (session.playNextSolutionMove()) {
                _state.update { it.copy(boardState = session.boardState(), navigation = session.navigation()) }
                launchAnimationDelay(scope, config.solutionStepDelayMs, AnimationPhase.SolutionStepAnimated, channel)
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

    private suspend fun onResume(scope: CoroutineScope, session: BoardSession, channel: Channel<PlayIntent>): IntentOutcome {
        onResumeSession(scope, session, channel)
        return IntentOutcome.Continue
    }

    private suspend fun playOpponentMove(
        scope: CoroutineScope,
        session: BoardSession,
        channel: Channel<PlayIntent>,
    ) {
        // TODO Paul: we need to signal here that we might be waiting for the opponent to move
        if (session.playOpponentMove()) {
            _state.update { it.copy(boardState = session.boardState(), navigation = session.navigation()) }
            launchAnimationDelay(scope, config.moveAnimationMs, AnimationPhase.OpponentMoveAnimated, channel)
        } else {
            _state.update { it.copy(isAnimating = false) }
        }
    }

    private suspend fun playOpponentMoveImmediately(session: BoardSession) {
        if (session.playOpponentMove()) {
            _state.update {
                it.copy(
                    boardState = session.boardState(),
                    navigation = session.navigation(),
                    isAnimating = false,
                )
            }
        }
    }

    private fun launchAnimationDelay(scope: CoroutineScope, durationMs: Long, phase: AnimationPhase, channel: Channel<PlayIntent>) {
        animationJob = scope.launch(defaultDispatcher) {
            delay(durationMs)
            channel.send(PlayIntent.AnimationPhaseComplete(phase))
        }
    }

    private fun startTimer(scope: CoroutineScope): Job = scope.launch {
        timer?.start(config.timed!!.durationInMs, intervalMillis = 100L)
            ?.collect { remainder ->
                if (!remainder.isPositive()) {
                    intentChannel?.send(PlayIntent.ExpireTime)
                    return@collect
                } else {
                    val totalMs = (remainder.seconds * 1000L) + remainder.millis
                    _state.update { it.copy(remainingTimeMs = totalMs) }
                }
            }
    }

    private enum class IntentOutcome {
        Continue,
        Success,
        Defeat,
        End,
    }
}

private fun BoardSession.navigation(): PlaySessionState.Navigation? =
    if (canNavigate()) PlaySessionState.Navigation(
        canGoBack = canUndo(),
        canGoForward = canReplay(),
        completedMoves = completedMoves(),
        algebraicHistory = algebraicHistory(),
    ) else null
