package com.paulcraciunas.puzzles.impl.usecases

import androidx.work.Data
import androidx.work.WorkInfo
import com.paulcraciunas.puzzles.api.usecases.FetchPuzzleDatabase
import com.paulcraciunas.puzzles.impl.network.PuzzleSyncWorker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

internal class WorkInfoDataAdapterTest {

    private val underTest = WorkInfoDataAdapter()

    @Test
    fun `GIVEN null workInfo WHEN adapt is called THEN returns progress with all zeros`() {
        // Given
        val workInfo: WorkInfo? = null

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(0, result.download)
        assertEquals(0, result.unpack)
        assertEquals(0, result.buildDb)
        assertFalse(result.isComplete())
    }

    @Test
    fun `GIVEN workInfo with SUCCEEDED state WHEN adapt is called THEN returns complete progress`() {
        // Given
        val workInfo = createWorkInfo(WorkInfo.State.SUCCEEDED)

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(100, result.download)
        assertEquals(100, result.unpack)
        assertEquals(100, result.buildDb)
        assertTrue(result.isComplete())
    }

    @Test
    fun `GIVEN workInfo with FAILED state WHEN adapt is called THEN throws exception`() {
        // Given
        val workInfo = createWorkInfo(WorkInfo.State.FAILED)

        // When & Then
        assertThrows(NotImplementedError::class.java) {
            underTest.adapt(workInfo)
        }
    }

    @Test
    fun `GIVEN workInfo with CANCELLED state WHEN adapt is called THEN throws exception`() {
        // Given
        val workInfo = createWorkInfo(WorkInfo.State.CANCELLED)

        // When & Then
        assertThrows(NotImplementedError::class.java) {
            underTest.adapt(workInfo)
        }
    }

    @Test
    fun `GIVEN workInfo with empty progress data WHEN adapt is called THEN returns progress with all zeros`() {
        // Given
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, Data.Builder().build())

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(0, result.download)
        assertEquals(0, result.unpack)
        assertEquals(0, result.buildDb)
        assertFalse(result.isComplete())
    }

    @Test
    fun `GIVEN download step with 0 percent WHEN adapt is called THEN returns correct progress`() {
        // Given
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, createData("Download", 0))

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(0, result.download)
        assertEquals(0, result.unpack)
        assertEquals(0, result.buildDb)
        assertFalse(result.isComplete())
    }

    @Test
    fun `GIVEN download step with 50 percent WHEN adapt is called THEN returns correct progress`() {
        // Given
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, createData("Download", 50))

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(50, result.download)
        assertEquals(0, result.unpack)
        assertEquals(0, result.buildDb)
        assertFalse(result.isComplete())
    }

    @Test
    fun `GIVEN download step with 100 percent WHEN adapt is called THEN returns correct progress`() {
        // Given
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, createData("Download", 100))

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(100, result.download)
        assertEquals(0, result.unpack)
        assertEquals(0, result.buildDb)
        assertFalse(result.isComplete())
    }

    @Test
    fun `GIVEN unpack step with 0 percent WHEN adapt is called THEN returns correct progress`() {
        // Given
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, createData("Unpack", 0))

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(100, result.download)
        assertEquals(0, result.unpack)
        assertEquals(0, result.buildDb)
        assertFalse(result.isComplete())
    }

    @Test
    fun `GIVEN unpack step with 30 percent WHEN adapt is called THEN returns correct progress`() {
        // Given
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, createData("Unpack", 30))

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(100, result.download)
        assertEquals(30, result.unpack)
        assertEquals(0, result.buildDb)
        assertFalse(result.isComplete())
    }

    @Test
    fun `GIVEN unpack step with 100 percent WHEN adapt is called THEN returns correct progress`() {
        // Given
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, createData("Unpack", 100))

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(100, result.download)
        assertEquals(100, result.unpack)
        assertEquals(0, result.buildDb)
        assertFalse(result.isComplete())
    }

    @Test
    fun `GIVEN buildDb step with 0 percent WHEN adapt is called THEN returns correct progress`() {
        // Given
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, createData("BuildDb", 0))

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(100, result.download)
        assertEquals(100, result.unpack)
        assertEquals(0, result.buildDb)
        assertFalse(result.isComplete())
    }

    @Test
    fun `GIVEN buildDb step with 80 percent WHEN adapt is called THEN returns correct progress`() {
        // Given
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, createData("BuildDb", 80))

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(100, result.download)
        assertEquals(100, result.unpack)
        assertEquals(80, result.buildDb)
        assertFalse(result.isComplete())
    }

    @Test
    fun `GIVEN buildDb step with 100 percent WHEN adapt is called THEN returns complete progress`() {
        // Given
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, createData("BuildDb", 100))

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(100, result.download)
        assertEquals(100, result.unpack)
        assertEquals(100, result.buildDb)
        assertTrue(result.isComplete())
    }

    @Test
    fun `GIVEN invalid step string WHEN adapt is called THEN defaults to download`() {
        // Given
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, createData("InvalidStep", 50))

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(50, result.download)
        assertEquals(0, result.unpack)
        assertEquals(0, result.buildDb)
        assertFalse(result.isComplete())
    }

    @Test
    fun `GIVEN null step string WHEN adapt is called THEN defaults to download`() {
        // Given
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, createData(null, 75))

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(75, result.download)
        assertEquals(0, result.unpack)
        assertEquals(0, result.buildDb)
        assertFalse(result.isComplete())
    }

    @Test
    fun `GIVEN case sensitive step string WHEN adapt is called THEN defaults to download`() {
        // Given - valueOf is case sensitive, so "unpack" != "Unpack"
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, createData("unpack", 75))

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(75, result.download) // we default to download
        assertEquals(0, result.unpack)
        assertEquals(0, result.buildDb)
        assertFalse(result.isComplete())
    }

    @Test
    fun `GIVEN negative progress value WHEN adapt is called THEN defaults to 0`() {
        // Given
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, createData("Download", -10))

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(0, result.download)
        assertEquals(0, result.unpack)
        assertEquals(0, result.buildDb)
        // Note: This will fail the Progress class assertion, which is expected behavior
    }

    @Test
    fun `GIVEN progress value over 100 WHEN adapt is called THEN clamps to 100`() {
        // Given
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, createData("Download", 150))

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(100, result.download)
        assertEquals(0, result.unpack)
        assertEquals(0, result.buildDb)
        // Note: This will fail the Progress class assertion, which is expected behavior
    }

    @Test
    fun `GIVEN data with only step and no progress WHEN adapt is called THEN uses default progress value`() {
        // Given
        val data = Data.Builder()
            .putString(PuzzleSyncWorker.STEP, "Download")
            .build()
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, data)

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(0, result.download) // Default value from getInt
        assertEquals(0, result.unpack)
        assertEquals(0, result.buildDb)
    }

    @Test
    fun `GIVEN data with only progress and no step WHEN adapt is called THEN defaults to download`() {
        // Given
        val data = Data.Builder()
            .putInt(PuzzleSyncWorker.PROGRESS_NAME, 60)
            .build()
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, data)

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(60, result.download) // Default to download
        assertEquals(0, result.unpack)
        assertEquals(0, result.buildDb)
    }

    @Test
    fun `GIVEN data with additional unrelated fields WHEN adapt is called THEN ignores extra fields`() {
        // Given
        val data = Data.Builder()
            .putString(PuzzleSyncWorker.STEP, "Unpack")
            .putInt(PuzzleSyncWorker.PROGRESS_NAME, 45)
            .putString("extraField", "extraValue")
            .putInt("extraInt", 999)
            .build()
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, data)

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(100, result.download)
        assertEquals(45, result.unpack)
        assertEquals(0, result.buildDb)
    }

    @Test
    fun `GIVEN all valid step values WHEN adapt is called THEN correctly maps each step`() {
        // Test all enum values to ensure they're handled correctly
        val testCases = listOf(
            Triple("Download", 25, FetchPuzzleDatabase.Progress(25, 0, 0)),
            Triple("Unpack", 50, FetchPuzzleDatabase.Progress(100, 50, 0)),
            Triple("BuildDb", 75, FetchPuzzleDatabase.Progress(100, 100, 75))
        )

        testCases.forEach { (step, progress, expected) ->
            // Given
            val workInfo = createWorkInfo(WorkInfo.State.RUNNING, createData(step, progress))

            // When
            val result = underTest.adapt(workInfo)

            // Then
            assertEquals(expected.download, result.download, "Failed for step: $step")
            assertEquals(expected.unpack, result.unpack, "Failed for step: $step")
            assertEquals(expected.buildDb, result.buildDb, "Failed for step: $step")
        }
    }

    @Test
    fun `GIVEN boundary progress values WHEN adapt is called THEN handles boundaries correctly`() {
        val boundaryValues = listOf(0, 1, 99, 100)

        boundaryValues.forEach { progressValue ->
            // Test Download step
            val downloadWorkInfo = createWorkInfo(WorkInfo.State.RUNNING, createData("Download", progressValue))
            val downloadResult = underTest.adapt(downloadWorkInfo)
            assertEquals(progressValue, downloadResult.download, "Download failed for progress: $progressValue")

            // Test Unpack step
            val unpackWorkInfo = createWorkInfo(WorkInfo.State.RUNNING, createData("Unpack", progressValue))
            val unpackResult = underTest.adapt(unpackWorkInfo)
            assertEquals(progressValue, unpackResult.unpack, "Unpack failed for progress: $progressValue")

            // Test BuildDb step
            val buildDbWorkInfo = createWorkInfo(WorkInfo.State.RUNNING, createData("BuildDb", progressValue))
            val buildDbResult = underTest.adapt(buildDbWorkInfo)
            assertEquals(progressValue, buildDbResult.buildDb, "BuildDb failed for progress: $progressValue")
        }
    }

    @Test
    fun `GIVEN workInfo with ENQUEUED state WHEN adapt is called THEN returns zero progress`() {
        // Given
        val workInfo = createWorkInfo(WorkInfo.State.ENQUEUED)

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(0, result.download)
        assertEquals(0, result.unpack)
        assertEquals(0, result.buildDb)
        assertFalse(result.isComplete())
    }

    @Test
    fun `GIVEN workInfo with BLOCKED state WHEN adapt is called THEN returns zero progress`() {
        // Given
        val workInfo = createWorkInfo(WorkInfo.State.BLOCKED)

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(0, result.download)
        assertEquals(0, result.unpack)
        assertEquals(0, result.buildDb)
        assertFalse(result.isComplete())
    }

    // Helper function to create WorkInfo objects with different states
    private fun createWorkInfo(
        state: WorkInfo.State,
        progressData: Data = Data.Builder().build()
    ) = WorkInfo(
        UUID.randomUUID(),
        state,
        emptySet(), // tags
        progress = progressData, // progress
    )

    // Helper function to create Data objects with step and progress
    private fun createData(step: String?, progress: Int): Data = Data.Builder().apply {
        step?.let { putString(PuzzleSyncWorker.STEP, step) }
        putInt(PuzzleSyncWorker.PROGRESS_NAME, progress)
    }.build()
}
