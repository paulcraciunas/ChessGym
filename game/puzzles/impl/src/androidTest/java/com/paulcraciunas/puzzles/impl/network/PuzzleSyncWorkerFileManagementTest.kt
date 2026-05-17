package com.paulcraciunas.puzzles.impl.network

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.paulcraciunas.puzzles.api.PuzzleDatabaseContract
import com.paulcraciunas.puzzles.impl.network.PuzzleSyncWorker.Companion.ERROR_DECOMPRESSION_FAILED
import com.paulcraciunas.puzzles.impl.network.PuzzleSyncWorker.Companion.ERROR_DOWNLOAD_FAILED
import com.paulcraciunas.puzzles.impl.network.PuzzleSyncWorker.Companion.ERROR_TYPE
import com.paulcraciunas.puzzles.impl.network.PuzzleSyncWorker.Companion.FAILED_STEP
import com.paulcraciunas.puzzles.impl.network.PuzzleSyncWorker.Companion.INPUT_TIER
import com.paulcraciunas.puzzles.impl.network.PuzzleSyncWorker.Step
import com.paulcraciunas.puzzles.impl.network.fakes.FakeWorkerFactory
import com.paulcraciunas.puzzles.impl.network.fakes.TestDatabaseBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
internal class PuzzleSyncWorkerFileManagementTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    private val factory = FakeWorkerFactory(dispatcher = testDispatcher)

    private lateinit var validDbContent: ByteArray
    private lateinit var downloadFile: File
    private lateinit var dbFile: File

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        downloadFile = File(context.cacheDir, "puzzles.db.zst")
        dbFile = File(context.cacheDir, "puzzles.db")
        validDbContent = TestDatabaseBuilder.buildTestDatabase(
            context,
            listOf(TEST_PUZZLE),
        )
        factory.databaseSource.testDbContent = validDbContent
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        downloadFile.delete()
        dbFile.delete()
        context.getDatabasePath(PuzzleDatabaseContract.ROOM_DATABASE_NAME).delete()
    }

    @Test
    fun given_downloadFailure_WHEN_doWork_THEN_noFilesCreated() = runTest {
        factory.databaseSource.fail(RuntimeException("Download failed"))
        val worker = createWorker()

        val result = worker.doWork()

        assertEquals(
            ListenableWorker.Result.failure(
                workDataOf(ERROR_TYPE to ERROR_DOWNLOAD_FAILED, FAILED_STEP to Step.Download.toString())
            ), result
        )
        assertFalse("Download file should not exist after download failure", downloadFile.exists())
        assertFalse("DB file should not exist after download failure", dbFile.exists())
    }

    @Test
    fun given_decompressionFailure_WHEN_doWork_THEN_downloadFilePreservedDbDeleted() = runTest {
        factory.decompressor.fail(RuntimeException("Decompression failed"))
        val worker = createWorker()

        val result = worker.doWork()

        assertEquals(
            ListenableWorker.Result.failure(
                workDataOf(ERROR_TYPE to ERROR_DECOMPRESSION_FAILED, FAILED_STEP to Step.Unpack.toString())
            ), result
        )
        assertTrue("Download file should be preserved for retry", downloadFile.exists())
        assertFalse("DB file should be deleted after decompression failure", dbFile.exists())
    }

    @Test
    fun given_existingDownloadFile_WHEN_doWork_THEN_skipsDownloadStep() = runTest {
        downloadFile.writeBytes(validDbContent)
        val worker = createWorker()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertFalse("Download file should be deleted after successful decompression", downloadFile.exists())
    }

    @Test
    fun given_retryAfterDecompressionFailure_WHEN_doWork_THEN_reusesDownloadFile() = runTest {
        factory.decompressor.fail(RuntimeException("Decompression failed"))
        val worker1 = createWorker()
        val result1 = worker1.doWork()

        assertEquals(
            ListenableWorker.Result.failure(
                workDataOf(ERROR_TYPE to ERROR_DECOMPRESSION_FAILED, FAILED_STEP to Step.Unpack.toString())
            ), result1
        )
        assertTrue("Download file should be preserved", downloadFile.exists())

        factory.decompressor.reset()
        val worker2 = createWorker()
        val result2 = worker2.doWork()

        assertEquals(ListenableWorker.Result.success(), result2)
        assertFalse("Download file should be deleted after successful completion", downloadFile.exists())
    }

    private fun createWorker() = TestListenableWorkerBuilder<PuzzleSyncWorker>(context = context)
        .setWorkerFactory(factory)
        .setInputData(workDataOf(INPUT_TIER to PuzzleDatabaseContract.Tier.COMPACT))
        .build()

    companion object {
        private val TEST_PUZZLE = TestDatabaseBuilder.TestPuzzle(
            fen = "rnbqkb1r/pppp1ppp/5n2/4p3/2B1P3/8/PPPP1PPP/RNBQK1NR w KQkq - 2 3",
            moves = "f2f3 d8h4",
            rating = 1411,
        )
    }
}
