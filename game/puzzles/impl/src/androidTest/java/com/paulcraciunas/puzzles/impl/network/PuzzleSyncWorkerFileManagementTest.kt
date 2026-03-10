package com.paulcraciunas.puzzles.impl.network

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.paulcraciunas.puzzles.impl.impl.AbstractPuzzleDatabase
import com.paulcraciunas.puzzles.impl.network.PuzzleSyncWorker.Companion.ERROR_DATABASE_WRITE_FAILED
import com.paulcraciunas.puzzles.impl.network.PuzzleSyncWorker.Companion.ERROR_DECOMPRESSION_FAILED
import com.paulcraciunas.puzzles.impl.network.PuzzleSyncWorker.Companion.ERROR_DOWNLOAD_FAILED
import com.paulcraciunas.puzzles.impl.network.PuzzleSyncWorker.Companion.ERROR_TYPE
import com.paulcraciunas.puzzles.impl.network.PuzzleSyncWorker.Companion.FAILED_STEP
import com.paulcraciunas.puzzles.impl.network.PuzzleSyncWorker.Step
import com.paulcraciunas.puzzles.impl.network.fakes.FakeWorkerFactory
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

    private val testDatabase = Room.inMemoryDatabaseBuilder(
        context.applicationContext,
        AbstractPuzzleDatabase::class.java
    ).allowMainThreadQueries().build()

    private val factory = FakeWorkerFactory(database = testDatabase, dispatcher = testDispatcher)

    private lateinit var downloadFile: File
    private lateinit var csvFile: File

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        downloadFile = File(context.cacheDir, "puzzles.zst")
        csvFile = File(context.cacheDir, "puzzles.csv")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        downloadFile.delete()
        csvFile.delete()
        testDatabase.close()
    }

    @Test
    fun given_successfulFullWorkflow_WHEN_doWork_THEN_allFilesAreCleanedUp() = runTest {
        // Given
        val worker = createWorker()

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.success(), result)
        assertFalse("Download file should be deleted after successful completion", downloadFile.exists())
        assertFalse("CSV file should be deleted after successful completion", csvFile.exists())

        val puzzles = testDatabase.get(10)
        assertEquals(1, puzzles.size)
    }

    @Test
    fun given_downloadFailure_WHEN_doWork_THEN_noFilesCreated() = runTest {
        // Given
        factory.databaseSource.fail(RuntimeException("Download failed"))
        val worker = createWorker()

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.failure(
            workDataOf(ERROR_TYPE to ERROR_DOWNLOAD_FAILED, FAILED_STEP to (Step.Download.toString()))
        ), result)
        assertFalse("Download file should not exist after download failure", downloadFile.exists())
        assertFalse("CSV file should not exist after download failure", csvFile.exists())
    }

    @Test
    fun given_decompressionFailure_WHEN_doWork_THEN_downloadFilePreservedCsvDeleted() = runTest {
        // Given
        factory.decompressor.fail(RuntimeException("Decompression failed"))
        val worker = createWorker()

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.failure(
            workDataOf(ERROR_TYPE to ERROR_DECOMPRESSION_FAILED, FAILED_STEP to (Step.Unpack.toString()))
        ), result)
        assertTrue("Download file should be preserved for retry", downloadFile.exists())
        assertFalse("CSV file should be deleted after decompression failure", csvFile.exists())
    }

    @Test
    fun given_databaseWriteFailure_WHEN_doWork_THEN_csvFilePreservedDownloadDeleted() = runTest {
        // Given - Setup to fail at database write stage
        factory.writer.fail(RuntimeException("Database write failed"))
        val worker = createWorker()

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.failure(
            workDataOf(ERROR_TYPE to ERROR_DATABASE_WRITE_FAILED, FAILED_STEP to (Step.BuildDb.toString()))
        ), result)
        assertFalse("Download file should be deleted after successful decompression", downloadFile.exists())
        assertTrue("CSV file should be preserved for retry", csvFile.exists())
    }

    @Test
    fun given_existingDownloadFile_WHEN_doWork_THEN_skipsDownloadStep() = runTest {
        // Given - Create a fake download file
        downloadFile.writeText("fake zst content")
        val worker = createWorker()

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.success(), result)
        assertFalse("Download file should be deleted after successful decompression", downloadFile.exists())
        assertFalse("CSV file should be deleted after successful completion", csvFile.exists())
    }

    @Test
    fun given_existingCsvFile_WHEN_doWork_THEN_skipsDownloadAndDecompressionSteps() = runTest {
        // Given - Create fake files
        downloadFile.writeText("fake zst content")
        csvFile.writeText(PUZZLES_CSV_CONTENT)
        val worker = createWorker()

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.success(), result)
        assertFalse("Download file should be deleted", downloadFile.exists())
        assertFalse("CSV file should be deleted after successful database write", csvFile.exists())
    }

    @Test
    fun given_existingCsvFileButNoDatabaseWriteFailure_WHEN_doWork_THEN_csvFilePreserved() = runTest {
        // Given - Create fake CSV file and setup database write failure
        csvFile.writeText(PUZZLES_CSV_CONTENT)
        factory.writer.fail(RuntimeException("Database write failed"))
        val worker = createWorker()

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.failure(
            workDataOf(ERROR_TYPE to ERROR_DATABASE_WRITE_FAILED, FAILED_STEP to (Step.BuildDb.toString()))
        ), result)
        assertTrue("CSV file should be preserved for retry", csvFile.exists())
    }

    @Test
    fun given_retryAfterDecompressionFailure_WHEN_doWork_THEN_reusesDownloadFile() = runTest {
        // Given - First run with decompression failure
        factory.decompressor.fail(RuntimeException("Decompression failed"))
        val worker1 = createWorker()
        val result1 = worker1.doWork()

        assertEquals(ListenableWorker.Result.failure(
            workDataOf(ERROR_TYPE to ERROR_DECOMPRESSION_FAILED, FAILED_STEP to (Step.Unpack.toString()))
        ), result1)
        assertTrue("Download file should be preserved", downloadFile.exists())

        // Reset the decompressor to work
        factory.decompressor.reset()
        val worker2 = createWorker()

        // When - Second run
        val result2 = worker2.doWork()

        // Then
        assertEquals(ListenableWorker.Result.success(), result2)
        assertFalse("Download file should be deleted after successful completion", downloadFile.exists())
        assertFalse("CSV file should be deleted after successful completion", csvFile.exists())

        val puzzles = testDatabase.get(10)
        assertEquals(1, puzzles.size)
    }

    @Test
    fun given_retryAfterDatabaseWriteFailure_WHEN_doWork_THEN_reusesCsvFile() = runTest {
        // Given - First run with database write failure
        factory.writer.fail(RuntimeException("Database write failed"))
        val worker1 = createWorker()
        val result1 = worker1.doWork()

        assertEquals(ListenableWorker.Result.failure(
            workDataOf(ERROR_TYPE to ERROR_DATABASE_WRITE_FAILED, FAILED_STEP to (Step.BuildDb.toString()))
        ), result1)
        assertTrue("CSV file should be preserved", csvFile.exists())
        assertFalse("Download file should be deleted after successful decompression", downloadFile.exists())

        // Reset the database writer to work
        factory.writer.reset()
        val worker2 = createWorker()

        // When - Second run
        val result2 = worker2.doWork()

        // Then
        assertEquals(ListenableWorker.Result.success(), result2)
        assertFalse("CSV file should be deleted after successful database write", csvFile.exists())

        val puzzles = testDatabase.get(10)
        assertEquals(1, puzzles.size)
    }

    private fun createWorker() = TestListenableWorkerBuilder<PuzzleSyncWorker>(context = context)
        .setWorkerFactory(factory)
        .build()

    companion object {
        private const val PUZZLES_CSV_CONTENT = """
            # A few puzzles so that we can properly test the loading
            00sHx,q3k1nr/1pp1nQpp/3p4/1P2p3/4P3/B1PP1b2/B5PP/5K2 b k - 0 17,e8d7 a2e6 d7d8 f7f8,1760,80,83,72,mate mateIn2 middlegame short,https://lichess.org/yyznGmXs/black#34,Italian_Game Italian_Game_Classical_Variation
            00sJ9,r3r1k1/p4ppp/2p2n2/1p6/3P1qb1/2NQR3/PPB2PP1/R1B3K1 w - - 5 18,e3g3 e8e1 g1h2 e1c1 a1c1 f4h6 h2g1 h6c1,2671,105,87,325,advantage attraction fork middlegame sacrifice veryLong,https://lichess.org/gyFeQsOE#35,French_Defense French_Defense_Exchange_Variation
            00sJb,Q1b2r1k/p2np2p/5bp1/q7/5P2/4B3/PPP3PP/2KR1B1R w - - 1 17,d1d7 a5e1 d7d1 e1e3 c1b1 e3b6,2235,76,97,64,advantage fork long,https://lichess.org/kiuvTFoE#33,Sicilian_Defense Sicilian_Defense_Dragon_Variation
            00sO1,1k1r4/pp3pp1/2p1p3/4b3/P3n1P1/8/KPP2PN1/3rBR1R b - - 2 31,b8c7 e1a5 b7b6 f1d1,998,85,94,293,advantage discoveredAttack master middlegame short,https://lichess.org/vsfFkG0s/black#62,
        """
    }
}
