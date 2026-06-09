package com.paulcraciunas.domain.impl.general

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class RealPulseTimerTest {
    private val testClock = ControllableClock()
    private val testDispatcher = StandardTestDispatcher()
    private val underTest = RealPulseTimer(testClock, testDispatcher)

    @Nested
    internal inner class BasicEmissions {
        @Test
        fun `GIVEN 100ms interval WHEN collected THEN emits elapsed millis per tick`() = runTest(testDispatcher) {
            val emissions = mutableListOf<Long>()

            val job = backgroundScope.launch {
                underTest.start(intervalMillis = 100L).collect {
                    emissions.add(it)
                }
            }
            runCurrent()

            // First tick
            testClock.advanceBy(100)
            advanceTimeBy(101)
            runCurrent()

            // Second tick
            testClock.advanceBy(100)
            advanceTimeBy(101)
            runCurrent()

            // Third tick
            testClock.advanceBy(100)
            advanceTimeBy(101)
            runCurrent()

            job.cancel()

            assertEquals(3, emissions.size)
            emissions.forEach { elapsed ->
                assertEquals(100L, elapsed)
            }
        }

        @Test
        fun `GIVEN 1000ms interval WHEN collected THEN emits at 1s intervals`() = runTest(testDispatcher) {
            val emissions = mutableListOf<Long>()

            val job = backgroundScope.launch {
                underTest.start(intervalMillis = 1000L).collect {
                    emissions.add(it)
                }
            }
            runCurrent()

            testClock.advanceBy(1000)
            advanceTimeBy(1001)
            runCurrent()

            testClock.advanceBy(1000)
            advanceTimeBy(1001)
            runCurrent()

            job.cancel()

            assertEquals(2, emissions.size)
            assertEquals(1000L, emissions[0])
            assertEquals(1000L, emissions[1])
        }

        @Test
        fun `GIVEN default interval WHEN start called without args THEN uses 100ms`() = runTest(testDispatcher) {
            val emissions = mutableListOf<Long>()

            val job = backgroundScope.launch {
                underTest.start().collect {
                    emissions.add(it)
                }
            }
            runCurrent()

            testClock.advanceBy(100)
            advanceTimeBy(101)
            runCurrent()

            job.cancel()

            assertEquals(1, emissions.size)
            assertEquals(100L, emissions.first())
        }
    }

    @Nested
    internal inner class WallClockAccuracy {
        @Test
        fun `GIVEN delay drift WHEN ticking THEN elapsed reflects actual wall clock`() = runTest(testDispatcher) {
            val emissions = mutableListOf<Long>()

            val job = backgroundScope.launch {
                underTest.start(intervalMillis = 100L).collect {
                    emissions.add(it)
                }
            }
            runCurrent()

            // Simulate drift: clock advances 120ms but delay was 100ms
            testClock.advanceBy(120)
            advanceTimeBy(101)
            runCurrent()

            job.cancel()

            assertEquals(1, emissions.size)
            assertEquals(120L, emissions.first())
        }

        @Test
        fun `GIVEN varying tick durations WHEN collected THEN each emission reflects actual elapsed`() = runTest(testDispatcher) {
            val emissions = mutableListOf<Long>()

            val job = backgroundScope.launch {
                underTest.start(intervalMillis = 100L).collect {
                    emissions.add(it)
                }
            }
            runCurrent()

            // First tick: exact 100ms
            testClock.advanceBy(100)
            advanceTimeBy(101)
            runCurrent()

            // Second tick: delayed to 150ms
            testClock.advanceBy(150)
            advanceTimeBy(101)
            runCurrent()

            // Third tick: back to 100ms
            testClock.advanceBy(100)
            advanceTimeBy(101)
            runCurrent()

            job.cancel()

            assertEquals(3, emissions.size)
            assertEquals(100L, emissions[0])
            assertEquals(150L, emissions[1])
            assertEquals(100L, emissions[2])
        }
    }

    @Nested
    internal inner class InfiniteFlow {
        @Test
        fun `GIVEN timer started WHEN not cancelled THEN continues emitting indefinitely`() = runTest(testDispatcher) {
            val emissions = mutableListOf<Long>()

            val job = backgroundScope.launch {
                underTest.start(intervalMillis = 100L).collect {
                    emissions.add(it)
                }
            }
            runCurrent()

            repeat(20) {
                testClock.advanceBy(100)
                advanceTimeBy(101)
                runCurrent()
            }

            job.cancel()

            assertEquals(20, emissions.size)
        }
    }

    @Nested
    internal inner class Cancellation {
        @Test
        fun `GIVEN collecting flow WHEN job cancelled THEN no more emissions`() = runTest(testDispatcher) {
            val emissions = mutableListOf<Long>()

            val job = backgroundScope.launch {
                underTest.start(intervalMillis = 100L).collect {
                    emissions.add(it)
                }
            }
            runCurrent()

            testClock.advanceBy(100)
            advanceTimeBy(101)
            runCurrent()

            testClock.advanceBy(100)
            advanceTimeBy(101)
            runCurrent()

            val countBeforeCancel = emissions.size
            job.cancel()

            testClock.advanceBy(500)
            advanceTimeBy(501)
            runCurrent()

            assertEquals(countBeforeCancel, emissions.size)
        }

        @Test
        fun `GIVEN timer just started WHEN immediately cancelled THEN emits nothing`() = runTest(testDispatcher) {
            val emissions = mutableListOf<Long>()

            val job = backgroundScope.launch {
                underTest.start(intervalMillis = 100L).collect {
                    emissions.add(it)
                }
            }
            runCurrent()
            job.cancel()

            assertTrue(emissions.isEmpty())
        }
    }

    @Nested
    internal inner class DifferentIntervals {
        @Test
        fun `GIVEN 50ms interval WHEN collected THEN emits at 50ms rate`() = runTest(testDispatcher) {
            val emissions = mutableListOf<Long>()

            val job = backgroundScope.launch {
                underTest.start(intervalMillis = 50L).collect {
                    emissions.add(it)
                }
            }
            runCurrent()

            repeat(5) {
                testClock.advanceBy(50)
                advanceTimeBy(51)
                runCurrent()
            }

            job.cancel()

            assertEquals(5, emissions.size)
            emissions.forEach { assertEquals(50L, it) }
        }

        @Test
        fun `GIVEN 500ms interval WHEN collected THEN emits at 500ms rate`() = runTest(testDispatcher) {
            val emissions = mutableListOf<Long>()

            val job = backgroundScope.launch {
                underTest.start(intervalMillis = 500L).collect {
                    emissions.add(it)
                }
            }
            runCurrent()

            // After 200ms: no emission yet (delay hasn't completed)
            testClock.advanceBy(200)
            advanceTimeBy(201)
            runCurrent()
            assertTrue(emissions.isEmpty())

            // After 500ms total: first emission
            testClock.advanceBy(300)
            advanceTimeBy(300)
            runCurrent()
            assertEquals(1, emissions.size)
            assertEquals(500L, emissions.first())

            job.cancel()
        }
    }
}
