package com.paulcraciunas.puzzles.impl.usecases

import androidx.work.Data
import androidx.work.WorkInfo
import com.paulcraciunas.puzzles.api.usecases.FetchPuzzleDatabase
import com.paulcraciunas.puzzles.impl.network.PuzzleSyncWorker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
    fun `GIVEN workInfo with FAILED state and download error WHEN adapt is called THEN returns progress with error`() {
        // Given
        val outputData = Data.Builder()
            .putString(PuzzleSyncWorker.ERROR_TYPE, PuzzleSyncWorker.ERROR_DOWNLOAD_FAILED)
            .putString(PuzzleSyncWorker.FAILED_STEP, "Download")
            .build()
        val workInfo = createWorkInfo(state = WorkInfo.State.FAILED, outputData = outputData)

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(0, result.download)
        assertEquals(0, result.unpack)
        assertEquals(0, result.buildDb)
        assertEquals(FetchPuzzleDatabase.Error.DownloadFailed, result.error)
        assertTrue(result.hasError())
        assertFalse(result.isComplete())
    }

    @Test
    fun `GIVEN workInfo with CANCELLED state WHEN adapt is called THEN returns zero progress with no error`() {
        // Given
        val workInfo = createWorkInfo(WorkInfo.State.CANCELLED)

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(0, result.download)
        assertEquals(0, result.unpack)
        assertEquals(0, result.buildDb)
        assertEquals(FetchPuzzleDatabase.Error.None, result.error)
        assertFalse(result.hasError())
        assertFalse(result.isComplete())
    }

    @Test
    fun `GIVEN workInfo with empty progress data WHEN adapt is called THEN returns progress with all zeros`() {
        // Given
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, progressData = Data.Builder().build())

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
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, progressData = createData("Download", 0))

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
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, progressData = createData("Download", 50))

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
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, progressData = createData("Download", 100))

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
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, progressData = createData("Unpack", 0))

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
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, progressData = createData("Unpack", 30))

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
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, progressData = createData("Unpack", 100))

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
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, progressData = createData("BuildDb", 0))

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(100, result.download)
        assertEquals(100, result.unpack)
        assertEquals(0, result.buildDb)
        assertFalse(result.isComplete())
    }

    @Test
    fun `GIVEN buildDb step with 50 percent WHEN adapt is called THEN returns correct progress`() {
        // Given
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, progressData = createData("BuildDb", 50))

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(100, result.download)
        assertEquals(100, result.unpack)
        assertEquals(50, result.buildDb)
        assertFalse(result.isComplete())
    }

    @Test
    fun `GIVEN buildDb step with 100 percent WHEN adapt is called THEN returns correct progress`() {
        // Given
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, progressData = createData("BuildDb", 100))

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(100, result.download)
        assertEquals(100, result.unpack)
        assertEquals(100, result.buildDb)
        assertTrue(result.isComplete())
    }

    @Test
    fun `GIVEN null step in progress data WHEN adapt is called THEN defaults to Download step`() {
        // Given
        val data = Data.Builder()
            .putInt(PuzzleSyncWorker.PROGRESS_NAME, 25)
            .build()
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, progressData = data)

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(25, result.download)
        assertEquals(0, result.unpack)
        assertEquals(0, result.buildDb)
    }

    @Test
    fun `GIVEN invalid step value WHEN adapt is called THEN defaults to Download step`() {
        // Given
        val data = Data.Builder()
            .putString(PuzzleSyncWorker.STEP, "InvalidStep")
            .putInt(PuzzleSyncWorker.PROGRESS_NAME, 75)
            .build()
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, progressData = data)

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(75, result.download)
        assertEquals(0, result.unpack)
        assertEquals(0, result.buildDb)
    }

    @Test
    fun `GIVEN negative progress value WHEN adapt is called THEN clamps to zero`() {
        // Given
        val data = Data.Builder()
            .putString(PuzzleSyncWorker.STEP, "Download")
            .putInt(PuzzleSyncWorker.PROGRESS_NAME, -10)
            .build()
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, progressData = data)

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(0, result.download)
        assertEquals(0, result.unpack)
        assertEquals(0, result.buildDb)
    }

    @Test
    fun `GIVEN progress value greater than 100 WHEN adapt is called THEN clamps to 100`() {
        // Given
        val data = Data.Builder()
            .putString(PuzzleSyncWorker.STEP, "Download")
            .putInt(PuzzleSyncWorker.PROGRESS_NAME, 150)
            .build()
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, progressData = data)

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(100, result.download)
        assertEquals(0, result.unpack)
        assertEquals(0, result.buildDb)
    }

    @Test
    fun `GIVEN progress data with extra fields WHEN adapt is called THEN ignores extra fields`() {
        // Given
        val data = Data.Builder()
            .putString(PuzzleSyncWorker.STEP, "Unpack")
            .putInt(PuzzleSyncWorker.PROGRESS_NAME, 45)
            .putString("extraField", "extraValue")
            .putInt("extraInt", 999)
            .build()
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, progressData = data)

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
            val workInfo = createWorkInfo(WorkInfo.State.RUNNING, progressData = createData(step, progress))

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
            val downloadWorkInfo = createWorkInfo(WorkInfo.State.RUNNING, progressData = createData("Download", progressValue))
            val downloadResult = underTest.adapt(downloadWorkInfo)
            assertEquals(progressValue, downloadResult.download, "Download failed for progress: $progressValue")

            // Test Unpack step
            val unpackWorkInfo = createWorkInfo(WorkInfo.State.RUNNING, progressData = createData("Unpack", progressValue))
            val unpackResult = underTest.adapt(unpackWorkInfo)
            assertEquals(progressValue, unpackResult.unpack, "Unpack failed for progress: $progressValue")

            // Test BuildDb step
            val buildDbWorkInfo = createWorkInfo(WorkInfo.State.RUNNING, progressData = createData("BuildDb", progressValue))
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

    @Test
    fun `GIVEN workInfo with FAILED state and decompression error WHEN adapt is called THEN returns correct progress`() {
        // Given
        val outputData = Data.Builder()
            .putString(PuzzleSyncWorker.ERROR_TYPE, PuzzleSyncWorker.ERROR_DECOMPRESSION_FAILED)
            .putString(PuzzleSyncWorker.FAILED_STEP, "Unpack")
            .build()
        val workInfo = createWorkInfo(WorkInfo.State.FAILED, outputData = outputData)

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(100, result.download) // Download completed
        assertEquals(0, result.unpack)     // Unpack failed
        assertEquals(0, result.buildDb)    // BuildDb not started
        assertEquals(FetchPuzzleDatabase.Error.DecompressionFailed, result.error)
        assertTrue(result.hasError())
        assertFalse(result.isComplete())
    }

    @Test
    fun `GIVEN workInfo with FAILED state and database write error WHEN adapt is called THEN returns correct progress`() {
        // Given
        val outputData = Data.Builder()
            .putString(PuzzleSyncWorker.ERROR_TYPE, PuzzleSyncWorker.ERROR_DATABASE_WRITE_FAILED)
            .putString(PuzzleSyncWorker.FAILED_STEP, "BuildDb")
            .build()
        val workInfo = createWorkInfo(WorkInfo.State.FAILED, outputData = outputData)

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(100, result.download) // Download completed
        assertEquals(100, result.unpack)   // Unpack completed
        assertEquals(0, result.buildDb)    // BuildDb failed
        assertEquals(FetchPuzzleDatabase.Error.DatabaseWriteFailed, result.error)
        assertTrue(result.hasError())
        assertFalse(result.isComplete())
    }

    @Test
    fun `GIVEN workInfo with unknown error type WHEN adapt is called THEN maps to None error`() {
        // Given
        val outputData = Data.Builder()
            .putString(PuzzleSyncWorker.ERROR_TYPE, "unknown_error_type")
            .putString(PuzzleSyncWorker.FAILED_STEP, "Download")
            .build()
        val workInfo = createWorkInfo(WorkInfo.State.FAILED, outputData = outputData)

        // When
        val result = underTest.adapt(workInfo)

        // Then
        assertEquals(FetchPuzzleDatabase.Error.None, result.error)
    }

    // Helper function to create Data objects with step and progress
    private fun createData(step: String?, progress: Int): Data = Data.Builder().apply {
        step?.let { putString(PuzzleSyncWorker.STEP, step) }
        putInt(PuzzleSyncWorker.PROGRESS_NAME, progress)
    }.build()

    private fun createWorkInfo(
        state: WorkInfo.State,
        outputData: Data = Data.Builder().build(),
        progressData: Data = Data.Builder().build()
    ) = WorkInfo(
        id = UUID.randomUUID(),
        state = state,
        tags = emptySet(),
        outputData = outputData,
        progress = progressData,
    )
}
