package com.paulcraciunas.screens.tools.clock.vm

import com.paulcraciunas.domain.api.general.FakePulseTimer
import com.paulcraciunas.game.logic.api.Side
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ClockViewModelTest {
    private val pulseTimer = FakePulseTimer()
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var underTest: ClockViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        underTest = ClockViewModel(dispatcher = testDispatcher, pulseTimer = pulseTimer)
    }

    @AfterEach
    fun tearDown() {
        underTest.onStop()
        Dispatchers.resetMain()
    }

    @Nested
    internal inner class Initialization {
        @Test
        fun `WHEN viewModel initialized THEN uiState is Setup with defaults`() = clockTest {
            val state = underTest.uiState.value as ClockUiState.Setup
            assertEquals(5, state.selectedMinutes)
            assertEquals(1, state.selectedIncrement)
        }

        @Test
        fun `WHEN viewModel initialized THEN times show default duration`() = clockTest {
            val state = underTest.uiState.value as ClockUiState.Setup
            assertEquals("5:00", state.whiteTime)
            assertEquals("5:00", state.blackTime)
        }
    }

    @Nested
    internal inner class SetupConfiguration {
        @Test
        fun `GIVEN Setup state WHEN time selected THEN selectedMinutes and display updated`() = clockTest {
            underTest.onTimeSelected(10)

            val state = underTest.uiState.value as ClockUiState.Setup
            assertEquals(10, state.selectedMinutes)
            assertEquals("10:00", state.whiteTime)
            assertEquals("10:00", state.blackTime)
        }

        @Test
        fun `GIVEN Setup state WHEN increment selected THEN selectedIncrement updated`() = clockTest {
            underTest.onIncrementSelected(5)

            val state = underTest.uiState.value as ClockUiState.Setup
            assertEquals(5, state.selectedIncrement)
        }

        @Test
        fun `GIVEN Playing state WHEN time selected THEN state unchanged`() = clockTest {
            underTest.onWhiteTapped()

            underTest.onTimeSelected(10)

            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals("5:00", state.whiteTime)
        }

        @Test
        fun `GIVEN Playing state WHEN increment selected THEN state unchanged`() = clockTest {
            underTest.onWhiteTapped()

            underTest.onIncrementSelected(10)

            assertTrue(underTest.uiState.value is ClockUiState.Playing)
        }
    }

    @Nested
    internal inner class StartingGame {
        @Test
        fun `GIVEN Setup state WHEN white tapped THEN game starts with black active`() = clockTest {
            underTest.onWhiteTapped()

            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals(Side.BLACK, state.activePlayer)
            assertEquals("5:00", state.whiteTime)
            assertEquals("5:00", state.blackTime)
        }

        @Test
        fun `GIVEN Setup state WHEN white tapped THEN pulse timer started`() = clockTest {
            underTest.onWhiteTapped()

            assertEquals(1, pulseTimer.startCount)
            assertEquals(ClockViewModel.TICK_INTERVAL_MS, pulseTimer.lastIntervalMillis)
        }

        @Test
        fun `GIVEN Setup state WHEN black tapped THEN state unchanged`() = clockTest {
            underTest.onBlackTapped()

            assertTrue(underTest.uiState.value is ClockUiState.Setup)
            assertEquals(0, pulseTimer.startCount)
        }

        @Test
        fun `GIVEN custom time selected WHEN game starts THEN uses selected time`() = clockTest {
            underTest.onTimeSelected(10)
            underTest.onWhiteTapped()

            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals("10:00", state.whiteTime)
            assertEquals("10:00", state.blackTime)
        }
    }

    @Nested
    internal inner class SwitchingPlayers {
        @Test
        fun `GIVEN black active WHEN black tapped THEN switches to white`() = clockTest {
            underTest.onWhiteTapped()
            underTest.onBlackTapped()

            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals(Side.WHITE, state.activePlayer)
        }

        @Test
        fun `GIVEN black active WHEN white tapped THEN ignored`() = clockTest {
            underTest.onWhiteTapped()
            underTest.onWhiteTapped()

            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals(Side.BLACK, state.activePlayer)
        }

        @Test
        fun `GIVEN white active WHEN white tapped THEN switches to black`() = clockTest {
            underTest.onWhiteTapped()
            underTest.onBlackTapped()
            underTest.onWhiteTapped()

            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals(Side.BLACK, state.activePlayer)
        }

        @Test
        fun `GIVEN white active WHEN black tapped THEN ignored`() = clockTest {
            underTest.onWhiteTapped()
            underTest.onBlackTapped()
            underTest.onBlackTapped()

            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals(Side.WHITE, state.activePlayer)
        }
    }

    @Nested
    internal inner class IncrementBehavior {
        @Test
        fun `GIVEN increment configured WHEN black switches THEN increment added to black`() = clockTest {
            underTest.onIncrementSelected(5)
            underTest.onWhiteTapped()
            underTest.onBlackTapped()

            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals("5:00", state.whiteTime)
            assertEquals("5:05", state.blackTime)
        }

        @Test
        fun `GIVEN increment configured WHEN white switches THEN increment added to white`() = clockTest {
            underTest.onIncrementSelected(3)
            underTest.onWhiteTapped()
            underTest.onBlackTapped()
            underTest.onWhiteTapped()

            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals("5:03", state.whiteTime)
        }

        @Test
        fun `GIVEN zero increment WHEN player switches THEN no time added`() = clockTest {
            underTest.onIncrementSelected(0)
            underTest.onWhiteTapped()
            underTest.onBlackTapped()

            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals("5:00", state.blackTime)
        }
    }

    @Nested
    internal inner class TimerUpdates {
        @Test
        fun `GIVEN black active WHEN timer ticks THEN black time decreases`() = clockTest {
            underTest.onWhiteTapped()

            pulseTimer.emit(1000L)

            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals("5:00", state.whiteTime)
            assertEquals("4:59", state.blackTime)
        }

        @Test
        fun `GIVEN white active WHEN timer ticks THEN white time decreases`() = clockTest {
            underTest.onIncrementSelected(0)
            underTest.onWhiteTapped()
            underTest.onBlackTapped()

            pulseTimer.emit(5000L)

            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals("4:55", state.whiteTime)
            assertEquals("5:00", state.blackTime)
        }

        @Test
        fun `GIVEN black active WHEN timer ticks sub-minute THEN shows tenths`() = clockTest {
            underTest.onTimeSelected(1)
            underTest.onWhiteTapped()

            pulseTimer.emit(55_000L)
            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals("5.0", state.blackTime)

            pulseTimer.emit(4_500L)
            val updated = underTest.uiState.value as ClockUiState.Playing
            assertEquals("0.5", updated.blackTime)
        }
    }

    @Nested
    internal inner class GameFinish {
        @Test
        fun `GIVEN black active WHEN black time reaches zero THEN finished with black as loser`() = clockTest {
            underTest.onTimeSelected(1)
            underTest.onWhiteTapped()

            pulseTimer.emit(60_000L)

            val state = underTest.uiState.value as ClockUiState.Finished
            assertEquals(Side.BLACK, state.loser)
            assertEquals("0.0", state.blackTime)
        }

        @Test
        fun `GIVEN white active WHEN white time reaches zero THEN finished with white as loser`() = clockTest {
            underTest.onTimeSelected(1)
            underTest.onIncrementSelected(0)
            underTest.onWhiteTapped()
            underTest.onBlackTapped()

            pulseTimer.emit(60_000L)

            val state = underTest.uiState.value as ClockUiState.Finished
            assertEquals(Side.WHITE, state.loser)
            assertEquals("0.0", state.whiteTime)
        }

        @Test
        fun `GIVEN Finished state WHEN white tapped THEN state unchanged`() = clockTest {
            underTest.onTimeSelected(1)
            underTest.onWhiteTapped()
            pulseTimer.emit(60_000L)
            assertTrue(underTest.uiState.value is ClockUiState.Finished)

            underTest.onWhiteTapped()

            assertTrue(underTest.uiState.value is ClockUiState.Finished)
        }

        @Test
        fun `GIVEN Finished state WHEN black tapped THEN state unchanged`() = clockTest {
            underTest.onTimeSelected(1)
            underTest.onWhiteTapped()
            pulseTimer.emit(60_000L)
            assertTrue(underTest.uiState.value is ClockUiState.Finished)

            underTest.onBlackTapped()

            assertTrue(underTest.uiState.value is ClockUiState.Finished)
        }
    }

    @Nested
    internal inner class StopAndNewGame {
        @Test
        fun `GIVEN Playing state WHEN stop pressed THEN returns to Setup`() = clockTest {
            underTest.onWhiteTapped()
            assertTrue(underTest.uiState.value is ClockUiState.Playing)

            underTest.onStop()

            val state = underTest.uiState.value as ClockUiState.Setup
            assertEquals(5, state.selectedMinutes)
            assertEquals(1, state.selectedIncrement)
        }

        @Test
        fun `GIVEN Playing state WHEN stop pressed THEN preserves time selections`() = clockTest {
            underTest.onTimeSelected(10)
            underTest.onIncrementSelected(5)
            underTest.onWhiteTapped()

            underTest.onStop()

            val state = underTest.uiState.value as ClockUiState.Setup
            assertEquals(10, state.selectedMinutes)
            assertEquals(5, state.selectedIncrement)
        }

        @Test
        fun `GIVEN Playing state WHEN stop pressed THEN time resets to selected`() = clockTest {
            underTest.onTimeSelected(10)
            underTest.onWhiteTapped()
            pulseTimer.emit(5000L)

            underTest.onStop()

            val state = underTest.uiState.value as ClockUiState.Setup
            assertEquals("10:00", state.whiteTime)
            assertEquals("10:00", state.blackTime)
        }

        @Test
        fun `GIVEN Finished state WHEN new game pressed THEN returns to Setup`() = clockTest {
            underTest.onTimeSelected(1)
            underTest.onWhiteTapped()
            pulseTimer.emit(60_000L)
            assertTrue(underTest.uiState.value is ClockUiState.Finished)

            underTest.onNewGame()

            val state = underTest.uiState.value as ClockUiState.Setup
            assertEquals(1, state.selectedMinutes)
        }

        @Test
        fun `GIVEN Finished state WHEN new game pressed THEN preserves time selections`() = clockTest {
            underTest.onTimeSelected(3)
            underTest.onIncrementSelected(10)
            underTest.onWhiteTapped()
            pulseTimer.emit(180_000L)

            underTest.onNewGame()

            val state = underTest.uiState.value as ClockUiState.Setup
            assertEquals(3, state.selectedMinutes)
            assertEquals(10, state.selectedIncrement)
        }
    }

    @Nested
    internal inner class AlternatingPlay {
        @Test
        fun `WHEN players alternate taps THEN times adjust correctly`() = clockTest {
            underTest.onTimeSelected(1)
            underTest.onIncrementSelected(3)
            underTest.onWhiteTapped()

            pulseTimer.emit(5000L)
            underTest.onBlackTapped()

            // white: 60s (1:00), black: 60 - 5 + 3 = 58s (sub-minute format: 58.0)
            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals(Side.WHITE, state.activePlayer)
            assertEquals("1:00", state.whiteTime)
            assertEquals("58.0", state.blackTime)
        }

        @Test
        fun `WHEN multiple alternations THEN all times track correctly`() = clockTest {
            underTest.onTimeSelected(1)
            underTest.onIncrementSelected(0)
            underTest.onWhiteTapped()

            pulseTimer.emit(10_000L)
            underTest.onBlackTapped()

            pulseTimer.emit(15_000L)
            underTest.onWhiteTapped()

            // white: 60-15 = 45s, black: 60-10 = 50s (sub-minute = tenths format)
            val state = underTest.uiState.value as ClockUiState.Playing
            assertEquals(Side.BLACK, state.activePlayer)
            assertEquals("45.0", state.whiteTime)
            assertEquals("50.0", state.blackTime)
        }
    }

    private fun clockTest(block: suspend TestScope.() -> Unit) = runTest(testDispatcher) {
        backgroundScope.launch(testDispatcher) { underTest.uiState.collect {} }
        block()
    }
}
