package com.paulcraciunas.puzzles.impl.usecases

import androidx.work.workDataOf
import com.paulcraciunas.puzzles.impl.network.PuzzleSyncWorker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class WorkInfoDataAdapterIntegrationTest {

    private val underTest = WorkInfoDataAdapter()

    @Test
    fun `GIVEN data created with workDataOf WHEN adapt is called THEN correctly processes data`() {
        // Given - Using the same method that would be used in the actual Worker
        val data = workDataOf(
            PuzzleSyncWorker.STEP to PuzzleSyncWorker.Step.Download.toString(),
            PuzzleSyncWorker.PROGRESS_NAME to 42
        )

        // When
        val result = underTest.adapt(data)

        // Then
        assertEquals(42, result.download)
        assertEquals(0, result.unpack)
        assertEquals(0, result.buildDb)
    }

    @Test
    fun `GIVEN complete workflow simulation WHEN adapt is called THEN correctly tracks progress through all steps`() {
        // Simulate a complete download workflow
        val workflowSteps = listOf(
            // Download phase
            workDataOf(PuzzleSyncWorker.STEP to "Download", PuzzleSyncWorker.PROGRESS_NAME to 0),
            workDataOf(PuzzleSyncWorker.STEP to "Download", PuzzleSyncWorker.PROGRESS_NAME to 25),
            workDataOf(PuzzleSyncWorker.STEP to "Download", PuzzleSyncWorker.PROGRESS_NAME to 50),
            workDataOf(PuzzleSyncWorker.STEP to "Download", PuzzleSyncWorker.PROGRESS_NAME to 100),

            // Unpack phase
            workDataOf(PuzzleSyncWorker.STEP to "Unpack", PuzzleSyncWorker.PROGRESS_NAME to 0),
            workDataOf(PuzzleSyncWorker.STEP to "Unpack", PuzzleSyncWorker.PROGRESS_NAME to 33),
            workDataOf(PuzzleSyncWorker.STEP to "Unpack", PuzzleSyncWorker.PROGRESS_NAME to 66),
            workDataOf(PuzzleSyncWorker.STEP to "Unpack", PuzzleSyncWorker.PROGRESS_NAME to 100),

            // BuildDb phase
            workDataOf(PuzzleSyncWorker.STEP to "BuildDb", PuzzleSyncWorker.PROGRESS_NAME to 0),
            workDataOf(PuzzleSyncWorker.STEP to "BuildDb", PuzzleSyncWorker.PROGRESS_NAME to 40),
            workDataOf(PuzzleSyncWorker.STEP to "BuildDb", PuzzleSyncWorker.PROGRESS_NAME to 80),
            workDataOf(PuzzleSyncWorker.STEP to "BuildDb", PuzzleSyncWorker.PROGRESS_NAME to 100)
        )

        val expectedResults = listOf(
            // Download phase
            Triple(0, 0, 0),
            Triple(25, 0, 0),
            Triple(50, 0, 0),
            Triple(100, 0, 0),

            // Unpack phase
            Triple(100, 0, 0),
            Triple(100, 33, 0),
            Triple(100, 66, 0),
            Triple(100, 100, 0),

            // BuildDb phase
            Triple(100, 100, 0),
            Triple(100, 100, 40),
            Triple(100, 100, 80),
            Triple(100, 100, 100)
        )

        workflowSteps.forEachIndexed { index, data ->
            // When
            val result = underTest.adapt(data)
            val expected = expectedResults[index]

            // Then
            assertEquals(expected.first, result.download, "Step $index download mismatch")
            assertEquals(expected.second, result.unpack, "Step $index unpack mismatch")
            assertEquals(expected.third, result.buildDb, "Step $index buildDb mismatch")

            // Check completion status
            val shouldBeComplete = expected.first == 100 && expected.second == 100 && expected.third == 100
            assertEquals(shouldBeComplete, result.isComplete(), "Step $index completion status mismatch")
        }
    }

    @Test
    fun `GIVEN enum step values WHEN adapt is called THEN correctly handles enum toString conversion`() {
        // Given - Test using actual enum values converted to strings
        val downloadData = workDataOf(
            PuzzleSyncWorker.STEP to PuzzleSyncWorker.Step.Download.toString(),
            PuzzleSyncWorker.PROGRESS_NAME to 30
        )
        val unpackData = workDataOf(
            PuzzleSyncWorker.STEP to PuzzleSyncWorker.Step.Unpack.toString(),
            PuzzleSyncWorker.PROGRESS_NAME to 60
        )
        val buildDbData = workDataOf(
            PuzzleSyncWorker.STEP to PuzzleSyncWorker.Step.BuildDb.toString(),
            PuzzleSyncWorker.PROGRESS_NAME to 90
        )

        // When & Then
        val downloadResult = underTest.adapt(downloadData)
        assertEquals(30, downloadResult.download)
        assertEquals(0, downloadResult.unpack)
        assertEquals(0, downloadResult.buildDb)

        val unpackResult = underTest.adapt(unpackData)
        assertEquals(100, unpackResult.download)
        assertEquals(60, unpackResult.unpack)
        assertEquals(0, unpackResult.buildDb)

        val buildDbResult = underTest.adapt(buildDbData)
        assertEquals(100, buildDbResult.download)
        assertEquals(100, buildDbResult.unpack)
        assertEquals(90, buildDbResult.buildDb)
    }
}
