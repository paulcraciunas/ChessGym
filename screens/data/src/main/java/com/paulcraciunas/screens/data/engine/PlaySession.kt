package com.paulcraciunas.screens.data.engine

import com.paulcraciunas.domain.api.general.CountdownTimer
import com.paulcraciunas.screens.data.AbstractBoardSession
import com.paulcraciunas.screens.data.Outcome
import com.paulcraciunas.screens.data.SessionResult
import com.paulcraciunas.screens.data.engine.PlaySessionConfiguration.EndMode
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PlaySession(
    private val settingsRepository: AppSettingsRepository,
    private val config: PlaySessionConfiguration,
    private val sessions: SessionsSource,
    private val timer: CountdownTimer? = null,
    private val onPlayComplete: OnComplete = {},
    private val onSessionComplete: OnComplete = {},
) {
    private val currentSession = createSingleSession()
    private val orchestrator = MutableStateFlow(OrchestratorState())
    private val resumeChannel = Channel<Unit>(capacity = Channel.CONFLATED)

    fun interface OnComplete {
        suspend operator fun invoke(onCompleteState: PlaySessionState)
    }

    fun interface SessionsSource {
        operator fun invoke(): Flow<AbstractBoardSession>
    }

    val state: Flow<PlaySessionState> = currentSession.state
        .combine(orchestrator) { sessionState, orchestratorState -> compose(sessionState, orchestratorState) }

    val stateValue: PlaySessionState
        get() = currentPlaySessionState()

    fun accept(intent: PlayIntent) {
        if (intent is PlayIntent.Resume) {
            resumeChannel.trySend(Unit)
        } else {
            currentSession.accept(intent)
        }
    }

    suspend fun run() {
        try {
            coroutineScope { runSessions() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            orchestrator.update { it.copy(status = PlaySessionState.Status.Failed, errorMessage = e.message) }
        }
        if (orchestrator.value.status == PlaySessionState.Status.Ended) {
            onPlayComplete(currentPlaySessionState())
        }
    }

    fun reset() {
        if (orchestrator.value.status == PlaySessionState.Status.Ended) {
            orchestrator.update { OrchestratorState() }
        }
    }

    private suspend fun CoroutineScope.runSessions() {
        startRun()
        val sessionJob = launch {
            try {
                sessions().collect { boardSession ->
                    if (orchestrator.value.status != PlaySessionState.Status.Playing) {
                        orchestrator.update { it.copy(status = PlaySessionState.Status.Playing) }
                    }

                    currentSession.run(boardSession)

                    onSessionOver(boardSession)
                    if (shouldEndRun(currentSession.state.value.boardState.outcome)) {
                        throw EndSessions()
                    }

                    val autoNext = config.autoNextOverride ?: settingsRepository.appSettings.first().autoNextPuzzle
                    if (!autoNext) {
                        orchestrator.update { it.copy(status = PlaySessionState.Status.Paused) }
                        resumeChannel.receive()
                    }
                }
            } catch (_: EndSessions) {} // No-Op. We set status to Ended anyway
        }
        val timerJob = launchTimer(sessionJob)
        sessionJob.join()
        timerJob.cancel()
        if (orchestrator.value.status != PlaySessionState.Status.Ended) {
            orchestrator.update { it.copy(status = PlaySessionState.Status.Ended, showSummary = true) }
        }
    }

    fun clearSummary() {
        orchestrator.update { it.copy(showSummary = false) }
    }

    private fun startRun() {
        currentSession.reset()
        orchestrator.value = OrchestratorState(
            status = PlaySessionState.Status.Loading,
            showSummary = false,
            remainingTimeMs = config.timed?.durationInMs ?: 0L,
            errorMessage = null,
        )
    }

    private fun createSingleSession(): SinglePlaySession = SinglePlaySession(
        settingsRepository = settingsRepository,
        config = SingleSessionConfiguration(
            gameOverBehavior = SingleSessionConfiguration.GameOverBehavior.Terminate,
            hints = config.hints?.let {
                when (it) {
                    PlaySessionConfiguration.HintMode.Unlimited -> SingleSessionConfiguration.HintMode.Unlimited
                    PlaySessionConfiguration.HintMode.Single -> SingleSessionConfiguration.HintMode.Single
                }
            },
            solutionStepDelayMs = config.solutionStepDelayMs,
            moveAnimationMs = config.moveAnimationMs,
            boardSwapAnimationMs = config.boardSwapAnimationMs,
        ),
    )

    private fun CoroutineScope.launchTimer(sessionJob: Job): Job = launch {
        val timed = config.timed ?: return@launch
        if (timed.mode == PlaySessionConfiguration.TimedMode.StartOnClick) {
            currentSession.state.first { it.status == SingleSessionState.Status.Playing }
        }
        timer?.start(timed.durationInMs, intervalMillis = 100L)
            ?.collect { remainder ->
                val totalMs = (remainder.seconds * 1000L) + remainder.millis
                orchestrator.update { it.copy(remainingTimeMs = totalMs) }
                if (!remainder.isPositive) {
                    sessionJob.cancel()
                }
            }
    }

    private suspend fun onSessionOver(boardSession: AbstractBoardSession) {
        orchestrator.update { it.copy(results = it.results + boardSession.result()) }
        onSessionComplete(currentPlaySessionState())
    }

    private fun shouldEndRun(outcome: Outcome?): Boolean {
        if (outcome == null) return false
        return when (config.endMode) {
            EndMode.OnFirstFailure -> outcome != Outcome.Won
            EndMode.OnSourceExhausted -> false
        }
    }

    private fun currentPlaySessionState(): PlaySessionState =
        compose(currentSession.state.value, orchestrator.value)

    private fun compose(sessionState: SingleSessionState, orchestratorState: OrchestratorState): PlaySessionState =
        PlaySessionState(
            boardState = sessionState.boardState,
            navigation = sessionState.navigation?.toPlayNavigation(),
            isAnimating = sessionState.isAnimating,
            hintAvailable = sessionState.hintAvailable,
            abandonRequested = sessionState.abandonRequested,
            remainingTimeMs = orchestratorState.remainingTimeMs,
            errorMessage = sessionState.errorMessage ?: orchestratorState.errorMessage,
            status = deriveStatus(sessionState.status, orchestratorState),
            results = orchestratorState.results,
            showSummary = orchestratorState.showSummary,
        )

    private fun deriveStatus(sessionStatus: SingleSessionState.Status, orchestratorState: OrchestratorState): PlaySessionState.Status =
        when {
            orchestratorState.status != PlaySessionState.Status.Playing -> orchestratorState.status
            sessionStatus == SingleSessionState.Status.Ready && orchestratorState.results.isEmpty() -> PlaySessionState.Status.Ready
            else -> PlaySessionState.Status.Playing
        }

    private data class OrchestratorState(
        val status: PlaySessionState.Status = PlaySessionState.Status.Loading,
        val results: List<SessionResult> = emptyList(),
        val showSummary: Boolean = false,
        val remainingTimeMs: Long = 0L,
        val errorMessage: String? = null,
    )

    private class EndSessions : Throwable()
}

private fun SingleSessionState.Navigation.toPlayNavigation(): PlaySessionState.Navigation =
    PlaySessionState.Navigation(
        canGoBack = canGoBack,
        canGoForward = canGoForward,
        completedMoves = completedMoves,
        algebraicHistory = algebraicHistory,
    )
