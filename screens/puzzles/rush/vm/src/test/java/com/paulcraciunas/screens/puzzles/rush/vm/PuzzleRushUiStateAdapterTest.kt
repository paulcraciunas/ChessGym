package com.paulcraciunas.screens.puzzles.rush.vm

import com.paulcraciunas.screens.data.BoardState
import com.paulcraciunas.screens.data.Outcome
import com.paulcraciunas.screens.data.SessionResult
import com.paulcraciunas.screens.data.engine.PlaySessionState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PuzzleRushUiStateAdapterTest {
    private val underTest = PuzzleRushUiStateAdapter()

    @Nested
    internal inner class StatusMapping {
        @Test
        fun `GIVEN Loading status WHEN toUiState THEN returns Loading`() {
            // Given
            val state = PlaySessionState(status = PlaySessionState.Status.Loading)

            // When
            val result = underTest.toUiState(state, highScore = 0)

            // Then
            assertInstanceOf(PuzzleRushUiState.Loading::class.java, result)
        }

        @Test
        fun `GIVEN Failed status WHEN toUiState THEN returns Failed`() {
            // Given
            val state = PlaySessionState(status = PlaySessionState.Status.Failed)

            // When
            val result = underTest.toUiState(state, highScore = 0)

            // Then
            assertInstanceOf(PuzzleRushUiState.Failed::class.java, result)
        }

        @Test
        fun `GIVEN Ready status WHEN toUiState THEN returns Ready with board and results`() {
            // Given
            val results = listOf(wonResult(rating = 1200))
            val state = PlaySessionState(
                status = PlaySessionState.Status.Ready,
                boardState = BoardState.empty,
                results = results,
                remainingTimeMs = 180_000L,
            )

            // When
            val result = underTest.toUiState(state, highScore = 0)

            // Then
            val ready = assertInstanceOf(PuzzleRushUiState.Ready::class.java, result)
            assertEquals(BoardState.empty, ready.data)
            assertEquals(results, ready.results)
        }

        @Test
        fun `GIVEN Playing status WHEN toUiState THEN returns Playing`() {
            // Given
            val state = PlaySessionState(
                status = PlaySessionState.Status.Playing,
                boardState = BoardState.empty,
                remainingTimeMs = 120_000L,
            )

            // When
            val result = underTest.toUiState(state, highScore = 0)

            // Then
            assertInstanceOf(PuzzleRushUiState.Playing::class.java, result)
        }

        @Test
        fun `GIVEN Paused status WHEN toUiState THEN returns Playing`() {
            // Given
            val state = PlaySessionState(
                status = PlaySessionState.Status.Paused,
                boardState = BoardState.empty,
                remainingTimeMs = 90_000L,
            )

            // When
            val result = underTest.toUiState(state, highScore = 0)

            // Then
            assertInstanceOf(PuzzleRushUiState.Playing::class.java, result)
        }

        @Test
        fun `GIVEN Ended status WHEN toUiState THEN returns Finished`() {
            // Given
            val state = PlaySessionState(
                status = PlaySessionState.Status.Ended,
                boardState = BoardState.empty,
                remainingTimeMs = 0L,
            )

            // When
            val result = underTest.toUiState(state, highScore = 0)

            // Then
            assertInstanceOf(PuzzleRushUiState.Finished::class.java, result)
        }
    }

    @Nested
    internal inner class TimeFormatting {
        @Test
        fun `GIVEN 180000ms WHEN toRemainingTime THEN returns 03 colon 00`() {
            // Given / When
            val result = 180_000L.toRemainingTime()

            // Then
            assertEquals("03:00", result.value)
            assertFalse(result.danger)
        }

        @Test
        fun `GIVEN 60000ms WHEN toRemainingTime THEN returns 01 colon 00`() {
            // Given / When
            val result = 60_000L.toRemainingTime()

            // Then
            assertEquals("01:00", result.value)
            assertFalse(result.danger)
        }

        @Test
        fun `GIVEN 21000ms WHEN toRemainingTime THEN returns 00 colon 21`() {
            // Given / When
            val result = 21_000L.toRemainingTime()

            // Then
            assertEquals("00:21", result.value)
            assertFalse(result.danger)
        }

        @Test
        fun `GIVEN 90500ms WHEN toRemainingTime THEN returns 01 colon 30`() {
            // Given / When
            val result = 90_500L.toRemainingTime()

            // Then
            assertEquals("01:30", result.value)
            assertFalse(result.danger)
        }
    }

    @Nested
    internal inner class DangerThreshold {
        @Test
        fun `GIVEN 20000ms WHEN toRemainingTime THEN returns danger format with tenths`() {
            // Given / When
            val result = 20_000L.toRemainingTime()

            // Then
            assertEquals("20.0", result.value)
            assertTrue(result.danger)
        }

        @Test
        fun `GIVEN 15500ms WHEN toRemainingTime THEN returns 15 dot 5`() {
            // Given / When
            val result = 15_500L.toRemainingTime()

            // Then
            assertEquals("15.5", result.value)
            assertTrue(result.danger)
        }

        @Test
        fun `GIVEN 1200ms WHEN toRemainingTime THEN returns 01 dot 2`() {
            // Given / When
            val result = 1_200L.toRemainingTime()

            // Then
            assertEquals("01.2", result.value)
            assertTrue(result.danger)
        }

        @Test
        fun `GIVEN 0ms WHEN toRemainingTime THEN returns 00 dot 0`() {
            // Given / When
            val result = 0L.toRemainingTime()

            // Then
            assertEquals("00.0", result.value)
            assertTrue(result.danger)
        }

        @Test
        fun `GIVEN 20001ms WHEN toRemainingTime THEN danger is false`() {
            // Given / When
            val result = 20_001L.toRemainingTime()

            // Then
            assertFalse(result.danger)
        }

        @Test
        fun `GIVEN 19999ms WHEN toRemainingTime THEN danger is true`() {
            // Given / When
            val result = 19_999L.toRemainingTime()

            // Then
            assertTrue(result.danger)
        }
    }

    @Nested
    internal inner class NullRemainingTime {
        @Test
        fun `GIVEN null remainingTimeMs WHEN toUiState for Ready THEN defaults to 0`() {
            // Given
            val state = PlaySessionState(
                status = PlaySessionState.Status.Ready,
                boardState = BoardState.empty,
                remainingTimeMs = null,
            )

            // When
            val result = underTest.toUiState(state, highScore = 0)

            // Then
            val ready = assertInstanceOf(PuzzleRushUiState.Ready::class.java, result)
            assertEquals("00.0", ready.time.value)
            assertTrue(ready.time.danger)
        }

        @Test
        fun `GIVEN null remainingTimeMs WHEN toUiState for Playing THEN defaults to 0`() {
            // Given
            val state = PlaySessionState(
                status = PlaySessionState.Status.Playing,
                boardState = BoardState.empty,
                remainingTimeMs = null,
            )

            // When
            val result = underTest.toUiState(state, highScore = 0)

            // Then
            val playing = assertInstanceOf(PuzzleRushUiState.Playing::class.java, result)
            assertEquals("00.0", playing.time.value)
            assertTrue(playing.time.danger)
        }
    }

    @Nested
    internal inner class IsNewHighScore {
        @Test
        fun `GIVEN success count greater than highScore WHEN toUiState Ended THEN isNewHighScore is true`() {
            // Given
            val results = listOf(
                wonResult(rating = 1000),
                wonResult(rating = 1100),
            )
            val state = PlaySessionState(
                status = PlaySessionState.Status.Ended,
                boardState = BoardState.empty,
                results = results,
                remainingTimeMs = 0L,
            )

            // When
            val result = underTest.toUiState(state, highScore = 1)

            // Then
            val finished = assertInstanceOf(PuzzleRushUiState.Finished::class.java, result)
            assertTrue(finished.isNewHighScore)
        }

        @Test
        fun `GIVEN success count equal to highScore WHEN toUiState Ended THEN isNewHighScore is false`() {
            // Given
            val results = listOf(
                wonResult(rating = 1000),
                wonResult(rating = 1100),
            )
            val state = PlaySessionState(
                status = PlaySessionState.Status.Ended,
                boardState = BoardState.empty,
                results = results,
                remainingTimeMs = 0L,
            )

            // When
            val result = underTest.toUiState(state, highScore = 2)

            // Then
            val finished = assertInstanceOf(PuzzleRushUiState.Finished::class.java, result)
            assertFalse(finished.isNewHighScore)
        }

        @Test
        fun `GIVEN success count less than highScore WHEN toUiState Ended THEN isNewHighScore is false`() {
            // Given
            val results = listOf(wonResult(rating = 1000))
            val state = PlaySessionState(
                status = PlaySessionState.Status.Ended,
                boardState = BoardState.empty,
                results = results,
                remainingTimeMs = 0L,
            )

            // When
            val result = underTest.toUiState(state, highScore = 5)

            // Then
            val finished = assertInstanceOf(PuzzleRushUiState.Finished::class.java, result)
            assertFalse(finished.isNewHighScore)
        }

        @Test
        fun `GIVEN mixed results WHEN toUiState Ended THEN only won results count`() {
            // Given
            val results = listOf(
                wonResult(rating = 1000),
                lostResult(rating = 1100),
                wonResult(rating = 1200),
                drewResult(rating = 1300),
            )
            val state = PlaySessionState(
                status = PlaySessionState.Status.Ended,
                boardState = BoardState.empty,
                results = results,
                remainingTimeMs = 0L,
            )

            // When
            val result = underTest.toUiState(state, highScore = 1)

            // Then
            val finished = assertInstanceOf(PuzzleRushUiState.Finished::class.java, result)
            assertTrue(finished.isNewHighScore)
        }

        @Test
        fun `GIVEN no results WHEN toUiState Ended with highScore 0 THEN isNewHighScore is false`() {
            // Given
            val state = PlaySessionState(
                status = PlaySessionState.Status.Ended,
                boardState = BoardState.empty,
                remainingTimeMs = 0L,
            )

            // When
            val result = underTest.toUiState(state, highScore = 0)

            // Then
            val finished = assertInstanceOf(PuzzleRushUiState.Finished::class.java, result)
            assertFalse(finished.isNewHighScore)
        }
    }

    @Nested
    internal inner class ShowAbandonDialog {
        @Test
        fun `GIVEN abandonRequested true WHEN toUiState Playing THEN showAbandonDialog is true`() {
            // Given
            val state = PlaySessionState(
                status = PlaySessionState.Status.Playing,
                boardState = BoardState.empty,
                remainingTimeMs = 60_000L,
                abandonRequested = true,
            )

            // When
            val result = underTest.toUiState(state, highScore = 0)

            // Then
            val playing = assertInstanceOf(PuzzleRushUiState.Playing::class.java, result)
            assertTrue(playing.showAbandonDialog)
        }

        @Test
        fun `GIVEN abandonRequested false WHEN toUiState Playing THEN showAbandonDialog is false`() {
            // Given
            val state = PlaySessionState(
                status = PlaySessionState.Status.Playing,
                boardState = BoardState.empty,
                remainingTimeMs = 60_000L,
                abandonRequested = false,
            )

            // When
            val result = underTest.toUiState(state, highScore = 0)

            // Then
            val playing = assertInstanceOf(PuzzleRushUiState.Playing::class.java, result)
            assertFalse(playing.showAbandonDialog)
        }

        @Test
        fun `GIVEN abandonRequested true for Paused WHEN toUiState THEN showAbandonDialog is true`() {
            // Given
            val state = PlaySessionState(
                status = PlaySessionState.Status.Paused,
                boardState = BoardState.empty,
                remainingTimeMs = 60_000L,
                abandonRequested = true,
            )

            // When
            val result = underTest.toUiState(state, highScore = 0)

            // Then
            val playing = assertInstanceOf(PuzzleRushUiState.Playing::class.java, result)
            assertTrue(playing.showAbandonDialog)
        }
    }

    @Nested
    internal inner class ShowSummaryDialog {
        @Test
        fun `GIVEN showSummary true WHEN toUiState Ended THEN showSummaryDialog is true`() {
            // Given
            val state = PlaySessionState(
                status = PlaySessionState.Status.Ended,
                boardState = BoardState.empty,
                remainingTimeMs = 0L,
                showSummary = true,
            )

            // When
            val result = underTest.toUiState(state, highScore = 0)

            // Then
            val finished = assertInstanceOf(PuzzleRushUiState.Finished::class.java, result)
            assertTrue(finished.showSummaryDialog)
        }

        @Test
        fun `GIVEN showSummary false WHEN toUiState Ended THEN showSummaryDialog is false`() {
            // Given
            val state = PlaySessionState(
                status = PlaySessionState.Status.Ended,
                boardState = BoardState.empty,
                remainingTimeMs = 0L,
                showSummary = false,
            )

            // When
            val result = underTest.toUiState(state, highScore = 0)

            // Then
            val finished = assertInstanceOf(PuzzleRushUiState.Finished::class.java, result)
            assertFalse(finished.showSummaryDialog)
        }
    }

    private companion object {
        fun wonResult(rating: Int, id: Int? = null): SessionResult = SessionResult(id = id, rating = rating, outcome = Outcome.Won)
        fun lostResult(rating: Int, id: Int? = null): SessionResult = SessionResult(id = id, rating = rating, outcome = Outcome.Lost)
        fun drewResult(rating: Int, id: Int? = null): SessionResult = SessionResult(id = id, rating = rating, outcome = Outcome.Drew)
    }
}
