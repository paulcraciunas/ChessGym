package com.paulcraciunas.screens.puzzles.rush.vm

import com.paulcraciunas.domain.api.puzzles.GetBufferedPuzzleSeries
import com.paulcraciunas.domain.api.puzzles.OnPuzzleRushComplete
import com.paulcraciunas.domain.api.puzzles.PuzzleRushResult
import com.paulcraciunas.screens.data.BoardSession
import com.paulcraciunas.screens.data.NoOpNavigation
import com.paulcraciunas.screens.data.NoOpSolution
import com.paulcraciunas.screens.data.PuzzlePlayableBoard
import com.paulcraciunas.screens.data.ScriptedOpponent
import com.paulcraciunas.screens.data.catchPuzzleExhausted
import com.paulcraciunas.screens.data.engine.PlaySession
import com.paulcraciunas.screens.data.engine.PlaySessionConfiguration
import com.paulcraciunas.screens.data.engine.PlaySessionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal const val RUSH_DURATION_MS = 180_000L

internal fun rushConfiguration(): PlaySessionConfiguration = PlaySessionConfiguration(
    endMode = PlaySessionConfiguration.EndMode.OnFirstFailure,
    timed = PlaySessionConfiguration.Timed(
        durationInMs = RUSH_DURATION_MS,
        mode = PlaySessionConfiguration.TimedMode.StartOnClick,
    ),
    hints = null,
    autoNextOverride = true,
)

internal class PuzzleRushSessions(
    private val getBufferedPuzzleSeries: GetBufferedPuzzleSeries,
) : PlaySession.SessionsSource {
    override fun invoke(): Flow<BoardSession> = getBufferedPuzzleSeries.execute()
        .map { puzzle ->
            BoardSession(
                navigation = NoOpNavigation,
                solution = NoOpSolution,
                opponent = ScriptedOpponent(puzzle),
            ).load(PuzzlePlayableBoard(puzzle))
        }
        .catchPuzzleExhausted("puzzle rush")
}

internal class PuzzleRushOnComplete(
    private val onPuzzleRushComplete: OnPuzzleRushComplete,
) : PlaySession.OnComplete {
    override suspend fun invoke(onCompleteState: PlaySessionState) {
        val failures = onCompleteState.results.filter { !it.success }.mapNotNull { it.id }
        onPuzzleRushComplete(
            PuzzleRushResult(
                puzzlesSolved = onCompleteState.results.count { it.success },
                puzzlesFailed = failures.size,
                failedPuzzleIds = failures,
                timeSpentMillis = RUSH_DURATION_MS - (onCompleteState.remainingTimeMs ?: 0),
            )
        )
    }
}
