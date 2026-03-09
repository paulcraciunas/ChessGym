package com.paulcraciunas.puzzles.impl.network

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.paulcraciunas.puzzles.impl.impl.AbstractPuzzleDatabase
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
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
internal class PuzzleSyncWorkerIntegrationTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    private val testDatabase = Room.inMemoryDatabaseBuilder(
        context.applicationContext,
        AbstractPuzzleDatabase::class.java
    ).allowMainThreadQueries().build()
    private val factory = FakeWorkerFactory(database = testDatabase, dispatcher = testDispatcher)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun given_successful_download_and_processing_WHEN_doWork_is_called_THEN_worker_returns_success() = runTest {
        // Given
        val worker = createWorker()

        // When
        val result = worker.doWork()
        val puzzles = testDatabase.get(10)

        // Then
        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(1, puzzles.size)
        assertEquals(1411, puzzles[0].rating)
    }

    @Test
    fun given_failure_to_fetch_database_WHEN_doWork_is_called_THEN_worker_returns_failure() = runTest {
        // Given
        factory.databaseSource.fail(RuntimeException("Can't connect"))
        val worker = createWorker()

        // When
        val result = worker.doWork()
        val puzzles = testDatabase.get(1)

        // Then
        assertEquals(
            ListenableWorker.Result.failure(
                workDataOf(ERROR_TYPE to ERROR_DOWNLOAD_FAILED, FAILED_STEP to (Step.Download.toString()))
            ), result
        )
        assertTrue(puzzles.isEmpty())
    }

    @Test
    fun given_failure_to_decompress_WHEN_doWork_is_called_THEN_worker_returns_failure() = runTest {
        // Given
        factory.decompressor.fail(RuntimeException("Can't decompress"))
        val worker = createWorker()

        // When
        val result = worker.doWork()
        val puzzles = testDatabase.get(1)

        // Then
        assertEquals(
            ListenableWorker.Result.failure(
                workDataOf(ERROR_TYPE to ERROR_DECOMPRESSION_FAILED, FAILED_STEP to (Step.Unpack.toString()))
            ), result
        )
        assertTrue(puzzles.isEmpty())
    }

    private fun createWorker() = TestListenableWorkerBuilder<PuzzleSyncWorker>(context = context)
        .setWorkerFactory(factory)
        .build()
}
