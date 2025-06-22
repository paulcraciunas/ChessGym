package com.paulcraciunas.puzzles.impl.network.fakes

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.paulcraciunas.notifications.api.NotificationFactory
import com.paulcraciunas.puzzles.impl.impl.AbstractPuzzleDatabase
import com.paulcraciunas.puzzles.impl.network.PuzzleSyncWorker
import com.paulcraciunas.puzzles.impl.network.progress.WorkerProgressReporter
import com.paulcraciunas.puzzles.impl.network.save.CsvPuzzleDatabaseWriter
import com.paulcraciunas.puzzles.impl.network.writer.FileProgressWriter
import com.paulcraciunas.serializer.api.PuzzleWriter
import com.paulcraciunas.settings.testutils.FakeAppSettingsRepository

// Test implementation of PuzzleWriter
private class FakePuzzleWriter : PuzzleWriter {
    override fun write(puzzleAndMoves: String): ByteArray = puzzleAndMoves.toByteArray()
    override fun write(puzzle: String, moves: String): ByteArray = "$puzzle|$moves".toByteArray()
}

// Test notification factory
private class FakeNotificationFactory : NotificationFactory {
    override fun createChannel(context: Context) {
        // No-op
    }

    override fun createForegroundNotification(context: Context): android.app.Notification {
        return android.app.Notification.Builder(context, "test_channel")
            .setContentTitle("Test")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }
}

internal class FakeWorkerFactory(
    database: AbstractPuzzleDatabase
) : WorkerFactory() {
    private val reporter = WorkerProgressReporter()
    private val databaseWriter = CsvPuzzleDatabaseWriter(reporter, FakePuzzleWriter(), database, FakeAppSettingsRepository())
    val decompressor = FakeDecompressor()
    val databaseSource = FakeDatabaseSource()

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {

        return PuzzleSyncWorker(
            appContext,
            workerParameters,
            notificationFactory = FakeNotificationFactory(),
            progressReporter = reporter,
            databaseSource = databaseSource,
            fileWriter = FileProgressWriter(reporter),
            decompressor = decompressor,
            puzzleDatabaseWriter = databaseWriter,
        )
    }
}