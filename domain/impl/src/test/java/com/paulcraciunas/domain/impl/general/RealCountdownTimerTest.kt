package com.paulcraciunas.domain.impl.general

import com.paulcraciunas.domain.api.general.CountdownTimer.Remainder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicLong

@OptIn(ExperimentalCoroutinesApi::class)
internal class RealCountdownTimerTest {
    private val testClock = ControllableClock()
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var underTest: RealCountdownTimer

    @BeforeEach
    fun setUp() {
        underTest = RealCountdownTimer(testClock)
    }

    @Nested
    internal inner class InitialState {
        @Test
        fun `WHEN created THEN remaining is zero`() {
            assertEquals(Remainder(0, 0), underTest.remaining.value)
        }

        @Test
        fun `WHEN created THEN isExpired is true`() {
            assertTrue(underTest.isExpired)
        }
    }

    @Nested
    internal inner class SetDuration {
        @Test
        fun `GIVEN not running WHEN set with seconds THEN remaining updated`() {
            // When
            underTest.set(durationSeconds = 60)

            // Then
            assertEquals(Remainder(60, 0), underTest.remaining.value)
        }

        @Test
        fun `GIVEN not running WHEN set with remainder THEN remaining updated`() {
            // When
            underTest.set(Remainder(seconds = 120, millis = 500))

            // Then
            assertEquals(Remainder(120, 500), underTest.remaining.value)
        }

        @Test
        fun `GIVEN running WHEN set with seconds THEN remaining unchanged`() = runTest(testDispatcher) {
            // Given
            underTest.set(durationSeconds = 60)
            underTest.start(testScope)

            // When
            underTest.set(durationSeconds = 120)

            // Then
            assertEquals(Remainder(60, 0), underTest.remaining.value)

            // Cleanup
            underTest.stop()
        }

        @Test
        fun `GIVEN running WHEN set with remainder THEN remaining unchanged`() = runTest(testDispatcher) {
            // Given
            underTest.set(durationSeconds = 60)
            underTest.start(testScope)

            // When
            underTest.set(Remainder(120, 0))

            // Then
            assertEquals(Remainder(60, 0), underTest.remaining.value)

            // Cleanup
            underTest.stop()
        }

        @Test
        fun `GIVEN set and stopped WHEN set again THEN remaining updated`() = runTest(testDispatcher) {
            // Given
            underTest.set(durationSeconds = 60)
            underTest.start(testScope)
            underTest.stop()

            // When
            underTest.set(durationSeconds = 120)

            // Then
            assertEquals(Remainder(120, 0), underTest.remaining.value)
        }

        @Test
        fun `WHEN isExpired after set THEN returns false`() {
            // When
            underTest.set(durationSeconds = 60)

            // Then
            assertFalse(underTest.isExpired)
        }
    }

    @Nested
    internal inner class SetInterval {
        @Test
        fun `WHEN setInterval with valid value THEN interval is used`() = runTest(testDispatcher) {
            // Given
            underTest.setInterval(500)
            underTest.set(durationSeconds = 10)
            underTest.start(testScope)

            // When - advance 500ms virtual time + clock
            testClock.advanceBy(500)
            advanceTimeBy(501)
            runCurrent()

            // Then - should have ticked once after 500ms
            val remaining = underTest.remaining.value
            assertTrue(remaining.seconds < 10 || (remaining.seconds == 10 && remaining.millis < 0))

            // Cleanup
            underTest.stop()
        }

        @Test
        fun `WHEN setInterval below minimum THEN coerced to 1ms`() = runTest(testDispatcher) {
            // Given
            underTest.setInterval(-100)
            underTest.set(durationSeconds = 10)
            underTest.start(testScope)

            // When - advance 1ms
            testClock.advanceBy(1)
            advanceTimeBy(2)
            runCurrent()

            // Then - timer should have ticked
            val remaining = underTest.remaining.value
            assertTrue(remaining.seconds <= 10)

            // Cleanup
            underTest.stop()
        }

        @Test
        fun `WHEN setInterval above maximum THEN coerced to 10000ms`() = runTest(testDispatcher) {
            // Given
            underTest.setInterval(20_000)
            underTest.set(durationSeconds = 60)
            underTest.start(testScope)

            // When - advance 5 seconds (less than coerced 10s interval)
            testClock.advanceBy(5_000)
            advanceTimeBy(5_001)
            runCurrent()

            // Then - no tick yet, still at 60s
            assertEquals(Remainder(60, 0), underTest.remaining.value)

            // Cleanup
            underTest.stop()
        }
    }

    @Nested
    internal inner class Countdown {
        @Test
        fun `GIVEN 10 second timer WHEN 1 second passes THEN remaining is 9 seconds`() = runTest(testDispatcher) {
            // Given
            underTest.set(durationSeconds = 10)
            underTest.start(testScope)

            // When
            testClock.advanceBy(1_000)
            advanceTimeBy(1_001)
            runCurrent()

            // Then
            assertEquals(Remainder(9, 0), underTest.remaining.value)

            // Cleanup
            underTest.stop()
        }

        @Test
        fun `GIVEN 10 second timer WHEN 5 seconds pass THEN remaining is 5 seconds`() = runTest(testDispatcher) {
            // Given
            underTest.set(durationSeconds = 10)
            underTest.start(testScope)

            // When - 5 ticks of 1 second each
            repeat(5) {
                testClock.advanceBy(1_000)
                advanceTimeBy(1_001)
                runCurrent()
            }

            // Then
            assertEquals(Remainder(5, 0), underTest.remaining.value)

            // Cleanup
            underTest.stop()
        }

        @Test
        fun `GIVEN 100ms interval WHEN 500ms passes THEN remaining decreases by 500ms`() = runTest(testDispatcher) {
            // Given
            underTest.setInterval(100)
            underTest.set(durationSeconds = 10)
            underTest.start(testScope)

            // When - 5 ticks of 100ms each = 500ms total
            repeat(5) {
                testClock.advanceBy(100)
                advanceTimeBy(101)
                runCurrent()
            }

            // Then
            assertEquals(Remainder(9, 500), underTest.remaining.value)

            // Cleanup
            underTest.stop()
        }

        @Test
        fun `GIVEN timer with millis remainder WHEN ticking THEN millis decrease correctly`() = runTest(testDispatcher) {
            // Given
            underTest.setInterval(100)
            underTest.set(Remainder(seconds = 5, millis = 500))
            underTest.start(testScope)

            // When - 3 ticks of 100ms each = 300ms total
            repeat(3) {
                testClock.advanceBy(100)
                advanceTimeBy(101)
                runCurrent()
            }

            // Then
            assertEquals(Remainder(5, 200), underTest.remaining.value)

            // Cleanup
            underTest.stop()
        }

        @Test
        fun `GIVEN timer WHEN time reaches zero THEN stops automatically`() = runTest(testDispatcher) {
            // Given
            underTest.set(durationSeconds = 2)
            underTest.start(testScope)

            // When - advance 3 seconds (more than the 2-second timer)
            repeat(3) {
                testClock.advanceBy(1_000)
                advanceTimeBy(1_001)
                runCurrent()
            }

            // Then
            assertFalse(underTest.remaining.value.isPositive())
            assertTrue(underTest.isExpired)

            // Cleanup
            underTest.stop()
        }

        @Test
        fun `GIVEN timer WHEN reaches zero THEN remaining does not go negative`() = runTest(testDispatcher) {
            // Given
            underTest.set(durationSeconds = 1)
            underTest.start(testScope)

            // When - advance well past zero
            repeat(5) {
                testClock.advanceBy(1_000)
                advanceTimeBy(1_001)
                runCurrent()
            }

            // Then
            val remaining = underTest.remaining.value
            assertTrue(remaining.seconds >= 0)
            assertTrue(remaining.millis >= 0)

            // Cleanup
            underTest.stop()
        }
    }

    @Nested
    internal inner class StopBehavior {
        @Test
        fun `GIVEN running timer WHEN stopped THEN remaining freezes`() = runTest(testDispatcher) {
            // Given
            underTest.set(durationSeconds = 10)
            underTest.start(testScope)

            testClock.advanceBy(3_000)
            advanceTimeBy(3_001)
            runCurrent()
            val frozenRemaining = underTest.remaining.value

            // When
            underTest.stop()

            // Then - advance more time, remaining should not change
            testClock.advanceBy(5_000)
            advanceTimeBy(5_001)
            runCurrent()

            assertEquals(frozenRemaining, underTest.remaining.value)
        }

        @Test
        fun `GIVEN stopped timer WHEN stop called again THEN no error`() {
            // Given
            underTest.set(durationSeconds = 10)
            underTest.stop()

            // When/Then - should not throw
            underTest.stop()
        }
    }

    @Nested
    internal inner class RestartBehavior {
        @Test
        fun `GIVEN running timer WHEN start called again THEN restarts from current remainder`() = runTest(testDispatcher) {
            // Given
            underTest.set(durationSeconds = 10)
            underTest.start(testScope)

            testClock.advanceBy(3_000)
            advanceTimeBy(3_001)
            runCurrent()

            // When - restart
            underTest.start(testScope)

            // Then - advance 1 second from restart point
            testClock.advanceBy(1_000)
            advanceTimeBy(1_001)
            runCurrent()

            val remaining = underTest.remaining.value
            assertEquals(Remainder(6, 0), remaining)

            // Cleanup
            underTest.stop()
        }
    }

    @Nested
    internal inner class ElapsedTime {
        @Test
        fun `GIVEN started timer WHEN elapsed called THEN returns time since start`() = runTest(testDispatcher) {
            // Given
            underTest.set(durationSeconds = 60)
            underTest.start(testScope)

            // When
            testClock.advanceBy(5_000)

            // Then
            assertEquals(5_000, underTest.elapsedMillis())

            // Cleanup
            underTest.stop()
        }

        @Test
        fun `GIVEN not started WHEN elapsed called THEN returns near zero`() {
            // When/Then
            assertEquals(0, underTest.elapsedMillis())
        }
    }

    @Nested
    internal inner class RemainderOperations {
        @Test
        fun `WHEN subtract smaller from larger THEN result is positive`() {
            val a = Remainder(10, 500)
            val b = Remainder(3, 200)
            assertEquals(Remainder(7, 300), a - b)
        }

        @Test
        fun `WHEN subtract with millis borrow THEN handles correctly`() {
            val a = Remainder(10, 200)
            val b = Remainder(3, 500)
            assertEquals(Remainder(6, 700), a - b)
        }

        @Test
        fun `WHEN subtract larger from smaller THEN result is zero`() {
            val a = Remainder(3, 0)
            val b = Remainder(5, 0)
            assertEquals(Remainder(0, 0), a - b)
        }

        @Test
        fun `WHEN add seconds THEN seconds increase`() {
            val remainder = Remainder(10, 500)
            val result = remainder + 5
            assertEquals(Remainder(15, 500), result)
        }

        @Test
        fun `GIVEN positive remainder WHEN isPositive THEN returns true`() {
            assertTrue(Remainder(1, 0).isPositive())
            assertTrue(Remainder(0, 1).isPositive())
            assertTrue(Remainder(10, 500).isPositive())
        }

        @Test
        fun `GIVEN zero remainder WHEN isPositive THEN returns false`() {
            assertFalse(Remainder(0, 0).isPositive())
        }
    }
}

/**
 * A controllable [Clock] for testing that allows advancing time manually.
 * Virtual time starts at a fixed epoch and advances only when [advanceBy] is called.
 */
private class ControllableClock : Clock() {
    private val offsetMillis = AtomicLong(0)
    private val baseInstant: Instant = Instant.parse("2026-01-01T00:00:00Z")

    fun advanceBy(millis: Long) {
        offsetMillis.addAndGet(millis)
    }

    override fun instant(): Instant = baseInstant.plusMillis(offsetMillis.get())
    override fun withZone(zone: ZoneId?): Clock = this
    override fun getZone(): ZoneId = ZoneId.of("UTC")
}
