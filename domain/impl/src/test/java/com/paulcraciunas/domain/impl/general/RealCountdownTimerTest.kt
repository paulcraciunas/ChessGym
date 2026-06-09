package com.paulcraciunas.domain.impl.general

import com.paulcraciunas.domain.api.general.CountdownTimer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class RealCountdownTimerTest {
    private val testClock = ControllableClock()
    private val underTest = RealCountdownTimer(testClock)

    @Nested
    internal inner class BasicEmissions {
        @Test
        fun `GIVEN 5s duration WHEN collected THEN emits decreasing remainders`() = runTest {
            val emissions = mutableListOf<CountdownTimer.Remainder>()

            val job = backgroundScope.launch {
                underTest.start(durationMs = 5000L, intervalMillis = 1000L).collect {
                    emissions.add(it)
                }
            }
            runCurrent() // Let flow start and emit first value

            assertEquals(1, emissions.size)
            assertEquals(CountdownTimer.Remainder(5, 0), emissions.first())

            repeat(5) {
                testClock.advanceBy(1000)
                advanceTimeBy(1001)
                runCurrent()
            }

            job.cancel()

            // Initial + 4 intermediate + final zero = 6
            assertTrue(emissions.size >= 5)
            assertEquals(5, emissions.first().seconds)
            assertEquals(CountdownTimer.Remainder(0, 0), emissions.last())
        }

        @Test
        fun `GIVEN 3s duration WHEN fully collected THEN flow completes naturally`() = runTest {
            val emissions = mutableListOf<CountdownTimer.Remainder>()

            val job = backgroundScope.launch {
                underTest.start(durationMs = 3000L, intervalMillis = 1000L).collect {
                    emissions.add(it)
                }
            }
            runCurrent()

            repeat(4) {
                testClock.advanceBy(1000)
                advanceTimeBy(1001)
                runCurrent()
            }

            assertEquals(CountdownTimer.Remainder(0, 0), emissions.last())
            job.cancel()
        }

        @Test
        fun `GIVEN timer WHEN first emission THEN shows full remaining time`() = runTest {
            var firstEmission: CountdownTimer.Remainder? = null

            val job = backgroundScope.launch {
                underTest.start(durationMs = 10_000L, intervalMillis = 1000L).collect {
                    if (firstEmission == null) firstEmission = it
                }
            }
            runCurrent()

            assertEquals(10, firstEmission?.seconds)
            assertEquals(0, firstEmission?.millis)
            job.cancel()
        }

        @Test
        fun `GIVEN timer WHEN final emission THEN is always zero remainder`() = runTest {
            val emissions = mutableListOf<CountdownTimer.Remainder>()

            val job = backgroundScope.launch {
                underTest.start(durationMs = 2000L, intervalMillis = 1000L).collect {
                    emissions.add(it)
                }
            }
            runCurrent()

            repeat(3) {
                testClock.advanceBy(1000)
                advanceTimeBy(1001)
                runCurrent()
            }

            job.cancel()
            assertEquals(CountdownTimer.Remainder(0, 0), emissions.last())
        }
    }

    @Nested
    internal inner class IntervalBehavior {
        @Test
        fun `GIVEN 100ms interval WHEN collected THEN emits more frequently`() = runTest {
            val emissions = mutableListOf<CountdownTimer.Remainder>()

            val job = backgroundScope.launch {
                underTest.start(durationMs = 1000L, intervalMillis = 100L).collect {
                    emissions.add(it)
                }
            }
            runCurrent()

            repeat(11) {
                testClock.advanceBy(100)
                advanceTimeBy(101)
                runCurrent()
            }

            job.cancel()
            assertTrue(emissions.size >= 10)
        }

        @Test
        fun `GIVEN interval below minimum WHEN start THEN coerces to 1ms`() = runTest {
            val emissions = mutableListOf<CountdownTimer.Remainder>()

            val job = backgroundScope.launch {
                underTest.start(durationMs = 100L, intervalMillis = -5L).collect {
                    emissions.add(it)
                }
            }
            runCurrent()

            testClock.advanceBy(200)
            advanceTimeBy(201)
            runCurrent()

            job.cancel()
            assertTrue(emissions.isNotEmpty())
        }

        @Test
        fun `GIVEN interval above maximum WHEN start THEN coerces to 10000ms`() = runTest {
            val emissions = mutableListOf<CountdownTimer.Remainder>()

            val job = backgroundScope.launch {
                underTest.start(durationMs = 25_000L, intervalMillis = 99_999L).collect {
                    emissions.add(it)
                }
            }
            runCurrent()

            // First emission happens immediately
            assertEquals(1, emissions.size)
            assertEquals(25, emissions.first().seconds)

            // After 5 seconds, no additional tick (coerced to 10s interval)
            testClock.advanceBy(5000)
            advanceTimeBy(5001)
            runCurrent()
            assertEquals(1, emissions.size)

            // After 10s total, should tick
            testClock.advanceBy(5000)
            advanceTimeBy(5001)
            runCurrent()
            assertTrue(emissions.size >= 2)

            job.cancel()
        }

        @Test
        fun `GIVEN 500ms interval on 2s timer WHEN collected THEN emits correct remainders`() = runTest {
            val emissions = mutableListOf<CountdownTimer.Remainder>()

            val job = backgroundScope.launch {
                underTest.start(durationMs = 2000L, intervalMillis = 500L).collect {
                    emissions.add(it)
                }
            }
            runCurrent()

            // First emission at t=0: Remainder(2, 0)
            assertEquals(CountdownTimer.Remainder(2, 0), emissions.first())

            repeat(4) {
                testClock.advanceBy(500)
                advanceTimeBy(501)
                runCurrent()
            }

            job.cancel()

            assertTrue(emissions.size >= 4)
            assertEquals(CountdownTimer.Remainder(0, 0), emissions.last())
        }
    }

    @Nested
    internal inner class WallClockAnchoring {
        @Test
        fun `GIVEN delay drift WHEN ticking THEN remaining is calculated from wall clock`() = runTest {
            val emissions = mutableListOf<CountdownTimer.Remainder>()

            val job = backgroundScope.launch {
                underTest.start(durationMs = 10_000L, intervalMillis = 1000L).collect {
                    emissions.add(it)
                }
            }
            runCurrent()

            // Simulate drift: clock advances 1200ms but delay was only 1000ms
            testClock.advanceBy(1200)
            advanceTimeBy(1001)
            runCurrent()

            // Despite delay waking up at 1000ms virtual, clock shows 1200ms elapsed
            val afterDrift = emissions.last()
            assertEquals(8, afterDrift.seconds)
            assertEquals(800, afterDrift.millis)

            job.cancel()
        }

        @Test
        fun `GIVEN consistent timing WHEN multiple ticks THEN remainders are accurate`() = runTest {
            val emissions = mutableListOf<CountdownTimer.Remainder>()

            val job = backgroundScope.launch {
                underTest.start(durationMs = 5000L, intervalMillis = 1000L).collect {
                    emissions.add(it)
                }
            }
            runCurrent()

            assertEquals(CountdownTimer.Remainder(5, 0), emissions.last())

            testClock.advanceBy(1000)
            advanceTimeBy(1001)
            runCurrent()
            assertEquals(CountdownTimer.Remainder(4, 0), emissions.last())

            testClock.advanceBy(1000)
            advanceTimeBy(1001)
            runCurrent()
            assertEquals(CountdownTimer.Remainder(3, 0), emissions.last())

            job.cancel()
        }
    }

    @Nested
    internal inner class Cancellation {
        @Test
        fun `GIVEN collecting flow WHEN job cancelled THEN no more emissions`() = runTest {
            val emissions = mutableListOf<CountdownTimer.Remainder>()

            val job = backgroundScope.launch {
                underTest.start(durationMs = 10_000L, intervalMillis = 1000L).collect {
                    emissions.add(it)
                }
            }
            runCurrent()

            testClock.advanceBy(2000)
            advanceTimeBy(2001)
            runCurrent()

            val countBeforeCancel = emissions.size
            job.cancel()

            testClock.advanceBy(5000)
            advanceTimeBy(5001)
            runCurrent()

            assertEquals(countBeforeCancel, emissions.size)
        }

        @Test
        fun `GIVEN timer not yet started WHEN start and immediately cancel THEN minimal emissions`() = runTest {
            val emissions = mutableListOf<CountdownTimer.Remainder>()

            val job = backgroundScope.launch {
                underTest.start(durationMs = 60_000L, intervalMillis = 1000L).collect {
                    emissions.add(it)
                }
            }
            runCurrent()
            job.cancel()

            // At most the initial emission
            assertTrue(emissions.size <= 1)
        }
    }

    @Nested
    internal inner class RemainderDataClass {
        @Test
        fun `GIVEN positive seconds WHEN isPositive THEN returns true`() {
            assertTrue(CountdownTimer.Remainder(1, 0).isPositive())
        }

        @Test
        fun `GIVEN zero seconds positive millis WHEN isPositive THEN returns true`() {
            assertTrue(CountdownTimer.Remainder(0, 500).isPositive())
        }

        @Test
        fun `GIVEN zero seconds zero millis WHEN isPositive THEN returns false`() {
            assertFalse(CountdownTimer.Remainder(0, 0).isPositive())
        }

        @Test
        fun `GIVEN large values WHEN isPositive THEN returns true`() {
            assertTrue(CountdownTimer.Remainder(3600, 999).isPositive())
        }
    }

    @Nested
    internal inner class EdgeCases {
        @Test
        fun `GIVEN zero duration WHEN start THEN emits final zero immediately`() = runTest {
            val emissions = mutableListOf<CountdownTimer.Remainder>()

            val job = backgroundScope.launch {
                underTest.start(durationMs = 0L, intervalMillis = 1000L).collect {
                    emissions.add(it)
                }
            }
            runCurrent()
            advanceTimeBy(100)
            runCurrent()

            job.cancel()

            assertTrue(emissions.isNotEmpty())
            assertEquals(CountdownTimer.Remainder(0, 0), emissions.last())
        }

        @Test
        fun `GIVEN very short duration WHEN start THEN completes quickly`() = runTest {
            val emissions = mutableListOf<CountdownTimer.Remainder>()

            val job = backgroundScope.launch {
                underTest.start(durationMs = 50L, intervalMillis = 100L).collect {
                    emissions.add(it)
                }
            }
            runCurrent()

            testClock.advanceBy(100)
            advanceTimeBy(101)
            runCurrent()

            job.cancel()
            assertEquals(CountdownTimer.Remainder(0, 0), emissions.last())
        }

        @Test
        fun `GIVEN millis remainder WHEN emitting THEN splits seconds and millis correctly`() = runTest {
            val emissions = mutableListOf<CountdownTimer.Remainder>()

            val job = backgroundScope.launch {
                underTest.start(durationMs = 2500L, intervalMillis = 1000L).collect {
                    emissions.add(it)
                }
            }
            runCurrent()

            assertEquals(CountdownTimer.Remainder(2, 500), emissions.first())

            testClock.advanceBy(1000)
            advanceTimeBy(1001)
            runCurrent()
            assertEquals(CountdownTimer.Remainder(1, 500), emissions.last())

            job.cancel()
        }

        @Test
        fun `GIVEN multiple starts WHEN collecting second flow THEN works independently`() = runTest {
            val firstEmissions = mutableListOf<CountdownTimer.Remainder>()
            val secondEmissions = mutableListOf<CountdownTimer.Remainder>()

            val job1 = backgroundScope.launch {
                underTest.start(durationMs = 3000L, intervalMillis = 1000L).collect {
                    firstEmissions.add(it)
                }
            }
            runCurrent()

            testClock.advanceBy(1000)
            advanceTimeBy(1001)
            runCurrent()
            job1.cancel()

            val job2 = backgroundScope.launch {
                underTest.start(durationMs = 2000L, intervalMillis = 1000L).collect {
                    secondEmissions.add(it)
                }
            }
            runCurrent()

            assertTrue(secondEmissions.isNotEmpty())
            assertEquals(2, secondEmissions.first().seconds)
            job2.cancel()
        }
    }
}
