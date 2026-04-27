package com.paulcraciunas.screens.tools.clock.vm

import com.paulcraciunas.domain.api.general.CountdownTimer.Remainder
import com.paulcraciunas.domain.api.general.FakeCountdownTimer
import com.paulcraciunas.game.logic.api.Side
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ClockViewModelTest {
    private val whiteTimer = FakeCountdownTimer()
    private val blackTimer = FakeCountdownTimer()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var underTest: ClockViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        underTest = ClockViewModel(whiteTimer, blackTimer)
    }

    @AfterEach
    fun tearDown() {
        underTest.onStop()
        Dispatchers.resetMain()
    }

    @Nested
    internal inner class Initialization {
        @Test
        fun `WHEN viewModel initialized THEN uiState is Setup with defaults`() = runTest(testDispatcher) {
            // Then
            val state = underTest.uiState.value
            assertTrue(state is ClockUiState.Setup)
            val setup = state as ClockUiState.Setup
            assertEquals(ClockUiState.DEFAULT_MINUTES, setup.selectedMinutes)
            assertEquals(ClockUiState.DEFAULT_INCREMENT, setup.selectedIncrement)
        }

        @Test
        fun `WHEN viewModel initialized THEN timers have correct interval`() = runTest(testDispatcher) {
            // Then - both timers should have 100ms interval set
            val state = underTest.uiState.value as ClockUiState.Setup
            assertEquals(Remainder(ClockUiState.DEFAULT_MINUTES * 60, 0), state.whiteTime)
            assertEquals(Remainder(ClockUiState.DEFAULT_MINUTES * 60, 0), state.blackTime)
        }
    }

    @Nested
    internal inner class SetupConfiguration {
        @Test
        fun `GIVEN Setup state WHEN time selected THEN selectedMinutes updated`() = runTest(testDispatcher) {
            // When
            underTest.onTimeSelected(10)

            // Then
            val state = underTest.uiState.value as ClockUiState.Setup
            assertEquals(10, state.selectedMinutes)
            assertEquals(Remainder(10 * 60, 0), state.whiteTime)
            assertEquals(Remainder(10 * 60, 0), state.blackTime)
        }

        @Test
        fun `GIVEN Setup state WHEN increment selected THEN selectedIncrement updated`() = runTest(testDispatcher) {
            // When
            underTest.onIncrementSelected(5)

            // Then
            val state = underTest.uiState.value as ClockUiState.Setup
            assertEquals(5, state.selectedIncrement)
        }

        @Test
        fun `GIVEN Playing state WHEN time selected THEN state unchanged`() = runTest(testDispatcher) {
            // Given
            underTest.onWhiteTapped()
            runCurrent()

            // When
            underTest.onTimeSelected(10)

            // Then
            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals(Remainder(ClockUiState.DEFAULT_MINUTES * 60, 0), state.whiteTime)
        }

        @Test
        fun `GIVEN Playing state WHEN increment selected THEN state unchanged`() = runTest(testDispatcher) {
            // Given
            underTest.onWhiteTapped()
            runCurrent()

            // When
            underTest.onIncrementSelected(10)

            // Then
            assertTrue(underTest.uiState.value is ClockUiState.Playing)
        }
    }

    @Nested
    internal inner class StartingGame {
        @Test
        fun `GIVEN Setup state WHEN white tapped THEN game starts with white active`() = runTest(testDispatcher) {
            // When
            underTest.onWhiteTapped()
            runCurrent()

            // Then
            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals(Side.WHITE, state.activePlayer)
            assertEquals(Remainder(ClockUiState.DEFAULT_MINUTES * 60, 0), state.whiteTime)
            assertEquals(Remainder(ClockUiState.DEFAULT_MINUTES * 60, 0), state.blackTime)
            assertTrue(whiteTimer.isRunning)
            assertFalse(blackTimer.isRunning)
        }

        @Test
        fun `GIVEN Setup state WHEN black tapped THEN game starts with black active`() = runTest(testDispatcher) {
            // When
            underTest.onBlackTapped()
            runCurrent()

            // Then
            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals(Side.BLACK, state.activePlayer)
            assertFalse(whiteTimer.isRunning)
            assertTrue(blackTimer.isRunning)
        }

        @Test
        fun `GIVEN custom time selected WHEN game starts THEN uses selected time`() = runTest(testDispatcher) {
            // Given
            underTest.onTimeSelected(10)

            // When
            underTest.onWhiteTapped()
            runCurrent()

            // Then
            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals(Remainder(10 * 60, 0), state.whiteTime)
            assertEquals(Remainder(10 * 60, 0), state.blackTime)
        }
    }

    @Nested
    internal inner class SwitchingPlayers {
        @Test
        fun `GIVEN white active WHEN white tapped THEN switches to black`() = runTest(testDispatcher) {
            // Given
            underTest.onWhiteTapped()
            runCurrent()

            // When
            underTest.onWhiteTapped()
            runCurrent()

            // Then
            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals(Side.BLACK, state.activePlayer)
            assertFalse(whiteTimer.isRunning)
            assertTrue(blackTimer.isRunning)
        }

        @Test
        fun `GIVEN white active WHEN black tapped THEN ignored`() = runTest(testDispatcher) {
            // Given
            underTest.onWhiteTapped()
            runCurrent()

            // When
            underTest.onBlackTapped()
            runCurrent()

            // Then
            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals(Side.WHITE, state.activePlayer)
        }

        @Test
        fun `GIVEN black active WHEN black tapped THEN switches to white`() = runTest(testDispatcher) {
            // Given
            underTest.onBlackTapped()
            runCurrent()

            // When
            underTest.onBlackTapped()
            runCurrent()

            // Then
            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals(Side.WHITE, state.activePlayer)
            assertTrue(whiteTimer.isRunning)
            assertFalse(blackTimer.isRunning)
        }

        @Test
        fun `GIVEN black active WHEN white tapped THEN ignored`() = runTest(testDispatcher) {
            // Given
            underTest.onBlackTapped()
            runCurrent()

            // When
            underTest.onWhiteTapped()
            runCurrent()

            // Then
            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals(Side.BLACK, state.activePlayer)
        }
    }

    @Nested
    internal inner class IncrementBehavior {
        @Test
        fun `GIVEN increment configured WHEN white switches THEN increment added to white`() = runTest(testDispatcher) {
            // Given
            underTest.onIncrementSelected(5)
            underTest.onWhiteTapped()
            runCurrent()

            // When
            underTest.onWhiteTapped()
            runCurrent()

            // Then
            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals(Remainder(ClockUiState.DEFAULT_MINUTES * 60 + 5, 0), state.whiteTime)
            assertEquals(Remainder(ClockUiState.DEFAULT_MINUTES * 60, 0), state.blackTime)
        }

        @Test
        fun `GIVEN increment configured WHEN black switches THEN increment added to black`() = runTest(testDispatcher) {
            // Given
            underTest.onIncrementSelected(3)
            underTest.onBlackTapped()
            runCurrent()

            // When
            underTest.onBlackTapped()
            runCurrent()

            // Then
            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals(Remainder(ClockUiState.DEFAULT_MINUTES * 60, 0), state.whiteTime)
            assertEquals(Remainder(ClockUiState.DEFAULT_MINUTES * 60 + 3, 0), state.blackTime)
        }

        @Test
        fun `GIVEN zero increment WHEN player switches THEN no time added`() = runTest(testDispatcher) {
            // Given
            underTest.onIncrementSelected(0)
            underTest.onWhiteTapped()
            runCurrent()

            // When
            underTest.onWhiteTapped()
            runCurrent()

            // Then
            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals(Remainder(ClockUiState.DEFAULT_MINUTES * 60, 0), state.whiteTime)
        }
    }

    @Nested
    internal inner class TimerUpdates {
        @Test
        fun `GIVEN white active WHEN timer ticks THEN white time decreases`() = runTest(testDispatcher) {
            // Given
            underTest.onWhiteTapped()
            runCurrent()

            // When
            whiteTimer.advanceTimeBy(seconds = 1)
            runCurrent()

            // Then
            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals(Remainder(ClockUiState.DEFAULT_MINUTES * 60 - 1, 0), state.whiteTime)
            assertEquals(Remainder(ClockUiState.DEFAULT_MINUTES * 60, 0), state.blackTime)
        }

        @Test
        fun `GIVEN black active WHEN timer ticks THEN black time decreases`() = runTest(testDispatcher) {
            // Given
            underTest.onBlackTapped()
            runCurrent()

            // When
            blackTimer.advanceTimeBy(seconds = 5)
            runCurrent()

            // Then
            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals(Remainder(ClockUiState.DEFAULT_MINUTES * 60, 0), state.whiteTime)
            assertEquals(Remainder(ClockUiState.DEFAULT_MINUTES * 60 - 5, 0), state.blackTime)
        }

        @Test
        fun `GIVEN white active WHEN timer ticks with millis THEN white time decreases precisely`() = runTest(testDispatcher) {
            // Given
            underTest.onWhiteTapped()
            runCurrent()

            // When
            whiteTimer.advanceTimeBy(seconds = 0, millis = 500)
            runCurrent()

            // Then
            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals(Remainder(ClockUiState.DEFAULT_MINUTES * 60 - 1, 500), state.whiteTime)
        }
    }

    @Nested
    internal inner class GameFinish {
        @Test
        fun `GIVEN white active WHEN white time reaches zero THEN finished with white as loser`() = runTest(testDispatcher) {
            // Given
            underTest.onTimeSelected(1)
            underTest.onWhiteTapped()
            runCurrent()

            // When
            whiteTimer.advanceUntilIdle()
            runCurrent()

            // Then
            val state = underTest.uiState.value as ClockUiState.Finished
            assertEquals(Side.WHITE, state.loser)
            assertFalse(state.whiteTime.isPositive())
            assertFalse(whiteTimer.isRunning)
            assertFalse(blackTimer.isRunning)
        }

        @Test
        fun `GIVEN black active WHEN black time reaches zero THEN finished with black as loser`() = runTest(testDispatcher) {
            // Given
            underTest.onTimeSelected(1)
            underTest.onBlackTapped()
            runCurrent()

            // When
            blackTimer.advanceUntilIdle()
            runCurrent()

            // Then
            val state = underTest.uiState.value as ClockUiState.Finished
            assertEquals(Side.BLACK, state.loser)
            assertFalse(state.blackTime.isPositive())
        }

        @Test
        fun `GIVEN Finished state WHEN white tapped THEN state unchanged`() = runTest(testDispatcher) {
            // Given
            underTest.onTimeSelected(1)
            underTest.onWhiteTapped()
            runCurrent()
            whiteTimer.advanceUntilIdle()
            runCurrent()
            assertTrue(underTest.uiState.value is ClockUiState.Finished)

            // When
            underTest.onWhiteTapped()
            runCurrent()

            // Then
            assertTrue(underTest.uiState.value is ClockUiState.Finished)
        }

        @Test
        fun `GIVEN Finished state WHEN black tapped THEN state unchanged`() = runTest(testDispatcher) {
            // Given
            underTest.onTimeSelected(1)
            underTest.onWhiteTapped()
            runCurrent()
            whiteTimer.advanceUntilIdle()
            runCurrent()
            assertTrue(underTest.uiState.value is ClockUiState.Finished)

            // When
            underTest.onBlackTapped()
            runCurrent()

            // Then
            assertTrue(underTest.uiState.value is ClockUiState.Finished)
        }
    }

    @Nested
    internal inner class StopAndNewGame {
        @Test
        fun `GIVEN Playing state WHEN stop pressed THEN returns to Setup`() = runTest(testDispatcher) {
            // Given
            underTest.onWhiteTapped()
            runCurrent()
            assertTrue(underTest.uiState.value is ClockUiState.Playing)

            // When
            underTest.onStop()
            runCurrent()

            // Then
            val state = underTest.uiState.value as ClockUiState.Setup
            assertEquals(ClockUiState.DEFAULT_MINUTES, state.selectedMinutes)
            assertEquals(ClockUiState.DEFAULT_INCREMENT, state.selectedIncrement)
            assertFalse(whiteTimer.isRunning)
            assertFalse(blackTimer.isRunning)
        }

        @Test
        fun `GIVEN Playing state WHEN stop pressed THEN preserves time selections`() = runTest(testDispatcher) {
            // Given
            underTest.onTimeSelected(10)
            underTest.onIncrementSelected(5)
            underTest.onWhiteTapped()
            runCurrent()

            // When
            underTest.onStop()
            runCurrent()

            // Then
            val state = underTest.uiState.value as ClockUiState.Setup
            assertEquals(10, state.selectedMinutes)
            assertEquals(5, state.selectedIncrement)
        }

        @Test
        fun `GIVEN Finished state WHEN new game pressed THEN returns to Setup`() = runTest(testDispatcher) {
            // Given
            underTest.onTimeSelected(1)
            underTest.onWhiteTapped()
            runCurrent()
            whiteTimer.advanceUntilIdle()
            runCurrent()
            assertTrue(underTest.uiState.value is ClockUiState.Finished)

            // When
            underTest.onNewGame()
            runCurrent()

            // Then
            val state = underTest.uiState.value as ClockUiState.Setup
            assertEquals(1, state.selectedMinutes)
        }

        @Test
        fun `GIVEN Finished state WHEN new game pressed THEN preserves time selections`() = runTest(testDispatcher) {
            // Given
            underTest.onTimeSelected(3)
            underTest.onIncrementSelected(10)
            underTest.onWhiteTapped()
            runCurrent()
            whiteTimer.advanceUntilIdle()
            runCurrent()

            // When
            underTest.onNewGame()
            runCurrent()

            // Then
            val state = underTest.uiState.value as ClockUiState.Setup
            assertEquals(3, state.selectedMinutes)
            assertEquals(10, state.selectedIncrement)
        }
    }

    @Nested
    internal inner class AlternatingPlay {
        @Test
        fun `WHEN players alternate taps THEN times adjust correctly`() = runTest(testDispatcher) {
            // Given
            underTest.onTimeSelected(1)
            underTest.onIncrementSelected(3)
            underTest.onWhiteTapped()
            runCurrent()

            // When - white plays for 5 seconds, then taps
            whiteTimer.advanceTimeBy(seconds = 5)
            runCurrent()
            underTest.onWhiteTapped()
            runCurrent()

            // Then - white: 60 - 5 + 3 = 58s, black: 60s, active: black
            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals(Side.BLACK, state.activePlayer)
            assertEquals(Remainder(58, 0), state.whiteTime)
            assertEquals(Remainder(60, 0), state.blackTime)
        }

        @Test
        fun `WHEN multiple alternations THEN all times track correctly`() = runTest(testDispatcher) {
            // Given - 1 minute game, 0 increment
            underTest.onTimeSelected(1)
            underTest.onIncrementSelected(0)
            underTest.onWhiteTapped()
            runCurrent()

            // White plays 10s
            whiteTimer.advanceTimeBy(seconds = 10)
            runCurrent()
            underTest.onWhiteTapped()
            runCurrent()

            // Black plays 15s
            blackTimer.advanceTimeBy(seconds = 15)
            runCurrent()
            underTest.onBlackTapped()
            runCurrent()

            // Then - white: 60-10 = 50s, black: 60-15 = 45s, active: white
            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals(Side.WHITE, state.activePlayer)
            assertEquals(Remainder(50, 0), state.whiteTime)
            assertEquals(Remainder(45, 0), state.blackTime)
        }
    }
}
