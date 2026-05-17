package com.paulcraciunas.puzzles.impl.network

import android.database.sqlite.SQLiteDatabase
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
internal class PuzzleSyncWorkerIntegrationTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    private val factory = FakeWorkerFactory(dispatcher = testDispatcher)
    private val roomDbPath get() = context.getDatabasePath(PuzzleDatabaseContract.ROOM_DATABASE_NAME)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        factory.databaseSource.testDbContent = TestDatabaseBuilder.buildTestDatabase(
            context,
            listOf(TEST_PUZZLE),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        roomDbPath.delete()
    }

    @Test
    fun given_successful_download_and_processing_WHEN_doWork_is_called_THEN_worker_returns_success() = runTest {
        // Given
        val worker = createWorker()

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.success(), result)

        SQLiteDatabase.openDatabase(roomDbPath.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            db.rawQuery("SELECT COUNT(*), rating FROM Puzzle", null).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(1, cursor.getInt(0))
                assertEquals(TEST_PUZZLE.rating, cursor.getInt(1))
            }
        }
    }

    @Test
    fun given_failure_to_fetch_database_WHEN_doWork_is_called_THEN_worker_returns_failure() = runTest {
        // Given
        factory.databaseSource.fail(RuntimeException("Can't connect"))
        val worker = createWorker()

        // When
        val result = worker.doWork()

        // Then
        assertEquals(
            ListenableWorker.Result.failure(
                workDataOf(ERROR_TYPE to ERROR_DOWNLOAD_FAILED, FAILED_STEP to (Step.Download.toString()))
            ), result
        )
        assertTrue(!roomDbPath.exists())
    }

    @Test
    fun given_failure_to_decompress_WHEN_doWork_is_called_THEN_worker_returns_failure() = runTest {
        // Given
        factory.decompressor.fail(RuntimeException("Can't decompress"))
        val worker = createWorker()

        // When
        val result = worker.doWork()

        // Then
        assertEquals(
            ListenableWorker.Result.failure(
                workDataOf(ERROR_TYPE to ERROR_DECOMPRESSION_FAILED, FAILED_STEP to (Step.Unpack.toString()))
            ), result
        )
        assertTrue(!roomDbPath.exists())
    }

    private fun createWorker() = TestListenableWorkerBuilder<PuzzleSyncWorker>(context = context)
        .setWorkerFactory(factory)
        .build()

    companion object {
        private val TEST_PUZZLE = TestDatabaseBuilder.TestPuzzle(
            fen = "rnbqkb1r/pppp1ppp/5n2/4p3/2B1P3/8/PPPP1PPP/RNBQK1NR w KQkq - 2 3",
            moves = "f2f3 d8h4",
            rating = 1411,
        )
    }
}
