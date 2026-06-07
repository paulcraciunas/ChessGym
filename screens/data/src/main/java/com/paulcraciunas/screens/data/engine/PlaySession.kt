package com.paulcraciunas.screens.data.engine

import com.paulcraciunas.domain.api.general.CountdownTimerV2
import com.paulcraciunas.screens.data.BOARD_ANIMATION_DURATION_MS
import com.paulcraciunas.screens.data.BoardSession
import com.paulcraciunas.screens.data.BoardState
import com.paulcraciunas.screens.data.PIECE_MOVE_ANIMATION_DURATION_MS
import com.paulcraciunas.screens.data.engine.PlaySessionConfiguration.EndMode
import com.paulcraciunas.settings.application.api.AppSettingsRepository
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
    private var intentChannel: Channel<PlayIntent>? = null
    private var timerJob: Job? = null
    private var animationJob: Job? = null

    fun interface OnComplete {
        suspend operator fun invoke(result: PlaySessionState)
    }

    fun interface SessionsSource {
        operator fun invoke(): Flow<BoardSession>
    }

    val state = _state.asStateFlow()
    fun reConfig(function: (PlaySessionConfiguration) -> PlaySessionConfiguration) = configuration.update { function(it) }
    fun accept(action: PlayIntent) {
        intentChannel?.trySend(action)
    }

    fun run(scope: CoroutineScope) {
        intentChannel = Channel(capacity = Channel.UNLIMITED)
        scope.launch(defaultDispatcher) {
            startRun(scope)

            try {
                // all sessions loop
                sessions().collect { session ->
                    onNewSession(scope, session)

                    // current session loop
                    while (isActive) {
                        val intent = intentChannel!!.receive()
                        if (_state.value.status == PlaySessionState.Status.Ready) {
                            _state.update { it.copy(status = PlaySessionState.Status.Playing) }
                        }
                        if (_state.value.status == PlaySessionState.Status.Paused && !intent.resumable) {
                            continue
                        }
                        var outcome = process(scope, intent, session)
                        // Check if we're configured to stop on first failure
                        if (outcome == IntentOutcome.Defeat && config.endMode == EndMode.OnFirstFailure) outcome = IntentOutcome.End
                        when (outcome) {
                            IntentOutcome.Continue -> continue
                            IntentOutcome.Defeat, // Otherwise, we stop when collect ends - EndMode.OnSourceExhausted
                            IntentOutcome.Success -> break
                            IntentOutcome.End -> {
                                onGameOver(session)
                                return@collect
                            }
                        }
                    }
                    onSessionOver(session)
                }
            } catch (e: Exception) {
                _state.update { it.copy(status = PlaySessionState.Status.Failed, isAnimating = false, errorMessage = e.message) }
            } finally {
                timerJob?.cancel()
                timerJob = null
            }
            // The game loop has fully terminated, the timer is dead
            with(_state.value) {
                if (status == PlaySessionState.Status.Ended) {
                    onPlayComplete(this)
                }
            }
        }
        // Only update the channel outside the coroutine, so we don't have to worry about synchronization
        intentChannel?.close()
        intentChannel = null
    }

    fun clearSummary() = _state.update { it.copy(showSummary = false) }

    private suspend fun process(scope: CoroutineScope, intent: PlayIntent, session: BoardSession): IntentOutcome =
        if (_state.value.isAnimating && intent.blockedByAnimation) { // ignore intents while animations are on
            IntentOutcome.Continue
        } else when (intent) { // exhaustive when
            is PlayIntent.ExpireTime -> IntentOutcome.End
            is PlayIntent.SessionFinished -> IntentOutcome.Success
            is PlayIntent.SessionFailed -> IntentOutcome.Defeat
            is PlayIntent.SelectSquare -> onMove(scope, session, session.onClick(intent.selection))
            is PlayIntent.Promote -> onMove(scope, session, session.promoteIfPending(intent.to))
            is PlayIntent.Hint -> onHint(session)
            is PlayIntent.RequestAbandon -> requestAbandon()
            is PlayIntent.DismissAbandon -> dismissAbandon()
            is PlayIntent.ConfirmAbandon -> confirmAbandon(scope, session)
            is PlayIntent.Navigate -> navigate(session, intent.type)
            is PlayIntent.Resume -> onResume(scope, session)
        }

    private suspend fun onNewSession(scope: CoroutineScope, session: BoardSession, resume: Boolean = false) {
        if (!resume && (_state.value.status == PlaySessionState.Status.Paused)) {
            return
        }
        val isFirstBoard = _state.value.status == PlaySessionState.Status.Loading
        session.autoPromote = config.autoPromote
        // If this is not the first session, wait for boards to swap
        val animateTransition = config.waitForAnimations && !isFirstBoard
        _state.update {
            PlaySessionState(
                status = when {
                    isFirstBoard -> PlaySessionState.Status.Ready
                    resume -> PlaySessionState.Status.Playing
                    else -> it.status
                },
                boardState = session.boardState(),
                isAnimating = animateTransition,
            )
        }
        if (animateTransition) {
            animationJob = scope.launch {
                waitForAnimation(AnimationType.BoardSwap)
                if (session.playOpponentMove()) {
                    _state.update { it.copy(boardState = session.boardState(), navigation = session.navigation()) }
                    waitForAnimation(AnimationType.Move)
                }
                _state.update { it.copy(isAnimating = false) }
            }
        } else {
            if (session.playOpponentMove()) {
                _state.update { it.copy(boardState = session.boardState(), navigation = session.navigation(), isAnimating = false) }
            }
        }
    }

    private suspend fun onSessionOver(session: BoardSession) {
        session.close()
        onSessionComplete(_state.value)
        if (!config.autoNext) {
            _state.update { it.copy(status = PlaySessionState.Status.Paused) }
        }
    }

    private suspend fun startRun(scope: CoroutineScope) {
        _state.update { // reset state in case this isn't the first run
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

    private suspend fun onMove(scope: CoroutineScope, session: BoardSession, nextBoard: BoardState): IntentOutcome {
        if (timerJob == null && config.timed?.mode == PlaySessionConfiguration.TimedMode.StartOnClick) {
            timerJob = startTimer(scope)
        }

        // If a move was played, we need to take care of:
        // 1. Session might be over
        // 2. Opponent's move
        // 3. Wait for animations to complete
        return if (nextBoard.movePlayed) {
            if (nextBoard.outcome != null) { // 1. Session is over
                _state.update { // Update results first, in case the timer expires during animation
                    it.copy(
                        results = it.results + session.result(),
                        boardState = nextBoard,
                        navigation = session.navigation(),
                        isAnimating = config.waitForAnimations, // If this is true, we can safely return Continue
                    )
                }
                if (config.waitForAnimations) { // 3. Wait for the move animation and complete
                    animationJob = scope.launch {
                        waitForAnimation(AnimationType.Move)
                        intentChannel?.send(if (nextBoard.won) PlayIntent.SessionFinished else PlayIntent.SessionFailed)
                    }
                    IntentOutcome.Continue // This will end when the above SessionFinished/SessionFailed is processed
                } else {
                    if (nextBoard.won) {
                        IntentOutcome.Success
                    } else {
                        IntentOutcome.Defeat
                    }
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
                    animationJob = scope.launch {
                        waitForAnimation(AnimationType.Move)
                        if (session.playOpponentMove()) {
                            _state.update { it.copy(boardState = session.boardState(), navigation = session.navigation()) }
                            waitForAnimation(AnimationType.Move)
                        }
                        _state.update { it.copy(isAnimating = false) }
                    }
                } else {
                    if (session.playOpponentMove()) {
                        _state.update { it.copy(boardState = session.boardState(), navigation = session.navigation(), isAnimating = false) }
                    }
                }
                IntentOutcome.Continue
            }
        } else IntentOutcome.Continue
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

    private fun requestAbandon(): IntentOutcome {
        _state.update { it.copy(abandonRequested = true) }
        return IntentOutcome.Continue
    }

    private fun dismissAbandon(): IntentOutcome {
        _state.update { it.copy(abandonRequested = false) }
        return IntentOutcome.Continue
    }

    private fun confirmAbandon(scope: CoroutineScope, session: BoardSession): IntentOutcome {
        _state.update { it.copy(boardState = session.boardState(), abandonRequested = false, isAnimating = config.waitForAnimations) }
        return if (config.waitForAnimations && session.solution.hasSolutionMoves()) {
            animationJob = scope.launch {
                while (session.solution.hasSolutionMoves()) {
                    if (session.playNextSolutionMove()) {
                        _state.update { it.copy(boardState = session.boardState(), navigation = session.navigation()) }
                        waitForAnimation(AnimationType.Solution)
                    }
                }
                intentChannel?.send(PlayIntent.SessionFailed)
            }
            IntentOutcome.Continue
        } else {
            IntentOutcome.Defeat
        }
    }

    private fun navigate(session: BoardSession, type: PlayIntent.Navigation): IntentOutcome {
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

    private suspend fun onResume(scope: CoroutineScope, session: BoardSession): IntentOutcome {
        onNewSession(scope = scope, session = session, resume = true)
        return IntentOutcome.Continue
    }

    private suspend fun onGameOver(lastSession: BoardSession) {
        lastSession.close()
        _state.update {
            it.copy(
                boardState = lastSession.clear(), // clear any selection as it would look broken
                status = PlaySessionState.Status.Ended,
                results = it.results + lastSession.result(),
                remainingTimeMs = 0L,
                isAnimating = false,
                showSummary = true,
            )
        }
    }

    private fun startTimer(scope: CoroutineScope): Job = scope.launch {
        timer?.start(config.timed!!.durationInMs, intervalMillis = 100L)
            ?.collect { remainder ->
                if (!remainder.isPositive()) {
                    intentChannel?.send(PlayIntent.ExpireTime)
                    return@collect // finish the flow
                } else {
                    val totalMs = (remainder.seconds * 1000L) + remainder.millis
                    _state.update { it.copy(remainingTimeMs = totalMs) }
                }
            }
    }

    private suspend fun waitForAnimation(animation: AnimationType) {
        when (animation) {
            AnimationType.Move -> delay(PIECE_MOVE_ANIMATION_DURATION_MS.toLong())
            AnimationType.Solution -> delay(SOLUTION_MOVE_DELAY_MS)
            AnimationType.BoardSwap -> delay(BOARD_ANIMATION_DURATION_MS.toLong())
        }
    }

    private enum class AnimationType {
        Move,
        BoardSwap,
        Solution,
    }

    private enum class IntentOutcome {
        Continue,
        Success,
        Defeat,
        End,
    }

    companion object {
        private const val SOLUTION_MOVE_DELAY_MS = 750L // Give the player a chance to process the information
    }
}

private fun BoardSession.navigation(): PlaySessionState.Navigation? = if (canNavigate()) PlaySessionState.Navigation(
    canGoBack = canUndo(),
    canGoForward = canReplay(),
    completedMoves = completedMoves(),
    algebraicHistory = algebraicHistory(),
) else null
