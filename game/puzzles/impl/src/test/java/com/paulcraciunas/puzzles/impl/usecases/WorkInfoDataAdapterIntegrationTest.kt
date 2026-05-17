package com.paulcraciunas.puzzles.impl.usecases

import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.workDataOf
import com.paulcraciunas.puzzles.impl.network.PuzzleSyncWorker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

internal class WorkInfoDataAdapterIntegrationTest {

    private val underTest = WorkInfoDataAdapter()

    @Test
    fun `GIVEN workInfo created with workDataOf WHEN adapt is called THEN correctly processes data`() {
        val data = workDataOf(
            PuzzleSyncWorker.STEP to PuzzleSyncWorker.Step.Download.toString(),
            PuzzleSyncWorker.PROGRESS_NAME to 42
        )
        val workInfo = createWorkInfo(WorkInfo.State.RUNNING, data)

        val result = underTest.adapt(workInfo)

        assertEquals(42, result.download)
        assertEquals(0, result.unpack)
    }

    @Test
    fun `GIVEN complete workflow simulation WHEN adapt is called THEN correctly tracks progress through all steps`() {
        val workflowSteps = listOf(
            workDataOf(PuzzleSyncWorker.STEP to "Download", PuzzleSyncWorker.PROGRESS_NAME to 0),
            workDataOf(PuzzleSyncWorker.STEP to "Download", PuzzleSyncWorker.PROGRESS_NAME to 25),
            workDataOf(PuzzleSyncWorker.STEP to "Download", PuzzleSyncWorker.PROGRESS_NAME to 50),
            workDataOf(PuzzleSyncWorker.STEP to "Download", PuzzleSyncWorker.PROGRESS_NAME to 100),
            workDataOf(PuzzleSyncWorker.STEP to "Unpack", PuzzleSyncWorker.PROGRESS_NAME to 0),
            workDataOf(PuzzleSyncWorker.STEP to "Unpack", PuzzleSyncWorker.PROGRESS_NAME to 33),
            workDataOf(PuzzleSyncWorker.STEP to "Unpack", PuzzleSyncWorker.PROGRESS_NAME to 66),
            workDataOf(PuzzleSyncWorker.STEP to "Unpack", PuzzleSyncWorker.PROGRESS_NAME to 100),
        )

        val expectedResults = listOf(
            Pair(0, 0),
            Pair(25, 0),
            Pair(50, 0),
            Pair(100, 0),
            Pair(100, 0),
            Pair(100, 33),
            Pair(100, 66),
            Pair(100, 100),
        )

        workflowSteps.forEachIndexed { index, data ->
            val workInfo = createWorkInfo(WorkInfo.State.RUNNING, data)

            val result = underTest.adapt(workInfo)
            val expected = expectedResults[index]

            assertEquals(expected.first, result.download, "Step $index download mismatch")
            assertEquals(expected.second, result.unpack, "Step $index unpack mismatch")

            val shouldBeComplete = expected.first == 100 && expected.second == 100
            assertEquals(shouldBeComplete, result.isComplete(), "Step $index completion status mismatch")
        }
    }

    @Test
    fun `GIVEN enum step values WHEN adapt is called THEN correctly handles enum toString conversion`() {
        val downloadData = workDataOf(
            PuzzleSyncWorker.STEP to PuzzleSyncWorker.Step.Download.toString(),
            PuzzleSyncWorker.PROGRESS_NAME to 30
        )
        val unpackData = workDataOf(
            PuzzleSyncWorker.STEP to PuzzleSyncWorker.Step.Unpack.toString(),
            PuzzleSyncWorker.PROGRESS_NAME to 60
        )

        val downloadResult = underTest.adapt(createWorkInfo(WorkInfo.State.RUNNING, downloadData))
        assertEquals(30, downloadResult.download)
        assertEquals(0, downloadResult.unpack)

        val unpackResult = underTest.adapt(createWorkInfo(WorkInfo.State.RUNNING, unpackData))
        assertEquals(100, unpackResult.download)
        assertEquals(60, unpackResult.unpack)
    }

    @Test
    fun `GIVEN complete workflow ending with SUCCESS state WHEN adapt is called THEN final result shows all completed`() {
        val successWorkInfo = createWorkInfo(WorkInfo.State.SUCCEEDED)

        val result = underTest.adapt(successWorkInfo)

        assertEquals(100, result.download)
        assertEquals(100, result.unpack)
        assertEquals(true, result.isComplete())
    }

    @Test
    fun `GIVEN workflow with intermediate RUNNING states and final SUCCESS WHEN adapt is called THEN correctly transitions`() {
        val workflowStates = listOf(
            createWorkInfo(WorkInfo.State.ENQUEUED),
            createWorkInfo(WorkInfo.State.RUNNING, workDataOf(PuzzleSyncWorker.STEP to "Download", PuzzleSyncWorker.PROGRESS_NAME to 50)),
            createWorkInfo(WorkInfo.State.RUNNING, workDataOf(PuzzleSyncWorker.STEP to "Unpack", PuzzleSyncWorker.PROGRESS_NAME to 30)),
            createWorkInfo(WorkInfo.State.SUCCEEDED),
        )

        val expectedResults = listOf(
            Pair(0, 0),
            Pair(50, 0),
            Pair(100, 30),
            Pair(100, 100),
        )

        workflowStates.forEachIndexed { index, workInfo ->
            val result = underTest.adapt(workInfo)
            val expected = expectedResults[index]

            assertEquals(expected.first, result.download, "State $index download mismatch")
            assertEquals(expected.second, result.unpack, "State $index unpack mismatch")

            val shouldBeComplete = index == workflowStates.lastIndex
            assertEquals(shouldBeComplete, result.isComplete(), "State $index completion status mismatch")
        }
    }

    private fun createWorkInfo(
        state: WorkInfo.State,
        progressData: Data = Data.Builder().build(),
    ) = WorkInfo(
        id = UUID.randomUUID(),
        state = state,
        tags = emptySet(),
        outputData = Data.Builder().build(),
        progress = progressData,
    )
}
