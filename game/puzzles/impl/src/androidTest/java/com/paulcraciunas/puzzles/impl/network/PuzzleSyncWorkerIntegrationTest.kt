package com.paulcraciunas.puzzles.impl.network

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.paulcraciunas.puzzles.impl.impl.AbstractPuzzleDatabase
import com.paulcraciunas.puzzles.impl.network.fakes.FakeWorkerFactory
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class PuzzleSyncWorkerIntegrationTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val testDatabase = Room.inMemoryDatabaseBuilder(
        context.applicationContext,
        AbstractPuzzleDatabase::class.java
    ).allowMainThreadQueries().build()
    private val factory = FakeWorkerFactory(database = testDatabase)

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
        assertTrue("Worker should return failure", result is ListenableWorker.Result.Failure)
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
        assertTrue("Worker should return failure", result is ListenableWorker.Result.Failure)
        assertTrue(puzzles.isEmpty())
    }

    private fun createWorker() = TestListenableWorkerBuilder<PuzzleSyncWorker>(context = context)
        .setWorkerFactory(factory)
        .build()
}
