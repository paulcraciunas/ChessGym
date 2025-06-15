package com.paulcraciunas.puzzles.impl.network

import com.paulcraciunas.puzzles.impl.network.progress.WorkerProgressReporter
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class WorkerProgressReporterTest {

    private val underTest = WorkerProgressReporter()

    @Test
    fun `GIVEN progress reporter WHEN progress is updated THEN correct percentage is reported`() = runTest {
        // Given
        val reportedProgress = mutableListOf<Int>()
        underTest.init { progress ->
            reportedProgress.add(progress)
        }

        // When
        underTest.onBegin(100L)
        underTest.onCompleted(25)
        underTest.onCompleted(25)
        underTest.onCompleted(50)

        // Then
        assertEquals(listOf(25, 50, 100), reportedProgress)
    }

    @Test
    fun `GIVEN same progress value WHEN onCompleted is called multiple times THEN progress is only reported once`() = runTest {
        // Given
        val reportedProgress = mutableListOf<Int>()
        underTest.init { progress ->
            reportedProgress.add(progress)
        }

        // When
        underTest.onBegin(100L)
        underTest.onCompleted(25)
        underTest.onCompleted(0) // Same progress (25%)
        underTest.onCompleted(25) // Now 50%

        // Then
        assertEquals(listOf(25, 50), reportedProgress)
    }

    @Test
    fun `GIVEN zero total WHEN onCompleted is called THEN no division by zero error occurs`() = runTest {
        // Given
        val reportedProgress = mutableListOf<Int>()
        underTest.init { progress ->
            reportedProgress.add(progress)
        }

        // When
        underTest.onBegin(0L)
        underTest.onCompleted(10)

        // Then
        assertEquals(listOf(100), reportedProgress) // Should handle gracefully
    }

    @Test
    fun `GIVEN progress exceeding total WHEN onCompleted is called THEN progress is capped at 100`() = runTest {
        // Given
        val reportedProgress = mutableListOf<Int>()
        underTest.init { progress ->
            reportedProgress.add(progress)
        }

        // When
        underTest.onBegin(50L)
        underTest.onCompleted(75) // Exceeds total

        // Then
        assertTrue(reportedProgress.last() <= 100) // Should not exceed 100%
    }
}
