package com.paulcraciunas.screens.puzzles.rush.vm

import com.paulcraciunas.domain.api.puzzles.OnPuzzleRushComplete
import com.paulcraciunas.domain.api.puzzles.PuzzleRushResult
import com.paulcraciunas.screens.data.BoardState
import com.paulcraciunas.screens.data.Outcome
import com.paulcraciunas.screens.data.SessionResult
import com.paulcraciunas.screens.data.engine.PlaySessionState
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PuzzleRushOnCompleteTest {
    private val fakeOnComplete = FakeOnPuzzleRushComplete()
    private val underTest = PuzzleRushOnComplete(onPuzzleRushComplete = fakeOnComplete)

    @Nested
    internal inner class PuzzleCounting {
        @Test
        fun `GIVEN all puzzles won WHEN invoked THEN reports correct solved count and no failures`() = runTest {
            // Given
            val state = buildState(
                results = List(5) { SessionResult(id = it, rating = 1200, outcome = Outcome.Won) },
            )

            // When
            underTest.invoke(state)

            // Then
            val result = fakeOnComplete.lastResult!!
            assertEquals(5, result.puzzlesSolved)
            assertEquals(0, result.puzzlesFailed)
            assertEquals(emptyList<Int>(), result.failedPuzzleIds)
        }

        @Test
        fun `GIVEN mix of wins and losses WHEN invoked THEN reports correct counts and failed IDs`() = runTest {
            // Given
            val state = buildState(
                results = listOf(
                    SessionResult(id = 1, rating = 1200, outcome = Outcome.Won),
                    SessionResult(id = 2, rating = 1300, outcome = Outcome.Lost),
                    SessionResult(id = 3, rating = 1100, outcome = Outcome.Won),
                    SessionResult(id = 4, rating = 1400, outcome = Outcome.Drew),
                    SessionResult(id = 5, rating = 1250, outcome = Outcome.Lost),
                ),
            )

            // When
            underTest.invoke(state)

            // Then
            val result = fakeOnComplete.lastResult!!
            assertEquals(2, result.puzzlesSolved)
            assertEquals(3, result.puzzlesFailed)
            assertEquals(listOf(2, 4, 5), result.failedPuzzleIds)
        }

        @Test
        fun `GIVEN all puzzles failed WHEN invoked THEN reports zero solved and all IDs as failed`() = runTest {
            // Given
            val state = buildState(
                results = listOf(
                    SessionResult(id = 10, rating = 1200, outcome = Outcome.Lost),
                    SessionResult(id = 20, rating = 1300, outcome = Outcome.Drew),
                    SessionResult(id = 30, rating = 1100, outcome = Outcome.Lost),
                ),
            )

            // When
            underTest.invoke(state)

            // Then
            val result = fakeOnComplete.lastResult!!
            assertEquals(0, result.puzzlesSolved)
            assertEquals(3, result.puzzlesFailed)
            assertEquals(listOf(10, 20, 30), result.failedPuzzleIds)
        }

        @Test
        fun `GIVEN no puzzles played WHEN invoked THEN reports zero counts and empty list`() = runTest {
            // Given
            val state = buildState(results = emptyList())

            // When
            underTest.invoke(state)

            // Then
            val result = fakeOnComplete.lastResult!!
            assertEquals(0, result.puzzlesSolved)
            assertEquals(0, result.puzzlesFailed)
            assertEquals(emptyList<Int>(), result.failedPuzzleIds)
        }

        @Test
        fun `GIVEN failures with null IDs WHEN invoked THEN null IDs are excluded from failedPuzzleIds`() = runTest {
            // Given
            val state = buildState(
                results = listOf(
                    SessionResult(id = null, rating = 1200, outcome = Outcome.Lost),
                    SessionResult(id = 7, rating = 1300, outcome = Outcome.Lost),
                    SessionResult(id = null, rating = 1100, outcome = Outcome.Drew),
                ),
            )

            // When
            underTest.invoke(state)

            // Then
            val result = fakeOnComplete.lastResult!!
            assertEquals(0, result.puzzlesSolved)
            assertEquals(1, result.puzzlesFailed)
            assertEquals(listOf(7), result.failedPuzzleIds)
        }
    }

    @Nested
    internal inner class TimeCalculation {
        @Test
        fun `GIVEN remaining time WHEN invoked THEN timeSpentMillis is duration minus remaining`() = runTest {
            // Given
            val state = buildState(remainingTimeMs = 60_000L)

            // When
            underTest.invoke(state)

            // Then
            assertEquals(120_000L, fakeOnComplete.lastResult!!.timeSpentMillis)
        }

        @Test
        fun `GIVEN null remaining time WHEN invoked THEN timeSpentMillis equals full duration`() = runTest {
            // Given
            val state = buildState(remainingTimeMs = null)

            // When
            underTest.invoke(state)

            // Then
            assertEquals(RUSH_DURATION_MS, fakeOnComplete.lastResult!!.timeSpentMillis)
        }
    }

    private fun buildState(results: List<SessionResult> = emptyList(), remainingTimeMs: Long? = 0L): PlaySessionState = PlaySessionState(
        results = results,
        boardState = BoardState.empty,
        remainingTimeMs = remainingTimeMs,
    )
}

private class FakeOnPuzzleRushComplete : OnPuzzleRushComplete {
    var lastResult: PuzzleRushResult? = null
        private set

    override suspend fun invoke(result: PuzzleRushResult) {
        lastResult = result
    }
}
