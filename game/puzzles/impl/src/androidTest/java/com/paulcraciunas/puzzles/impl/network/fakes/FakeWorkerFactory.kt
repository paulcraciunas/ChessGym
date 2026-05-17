package com.paulcraciunas.puzzles.impl.network.fakes

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.paulcraciunas.notifications.api.NotificationFactory
import com.paulcraciunas.puzzles.impl.network.PuzzleSyncWorker
import com.paulcraciunas.puzzles.impl.network.progress.WorkerProgressReporter
import com.paulcraciunas.puzzles.impl.network.writer.FileProgressWriter
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository
import kotlinx.coroutines.test.TestDispatcher

private class FakeNotificationFactory : NotificationFactory {
    override fun createChannel(context: Context) {}

    override fun createForegroundNotification(context: Context): android.app.Notification {
        return android.app.Notification.Builder(context, "test_channel")
            .setContentTitle("Test")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }
}

internal class FakeWorkerFactory(
    val dispatcher: TestDispatcher,
) : WorkerFactory() {
    val decompressor = FakeDecompressor()
    val databaseSource = FakeDatabaseSource()
    private val reporter = WorkerProgressReporter()
    private val appSettingsRepository = FakeAppSettingsRepository()

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker {
        return PuzzleSyncWorker(
            appContext,
            workerParameters,
            dispatcher,
            notificationFactory = FakeNotificationFactory(),
            progressReporter = reporter,
            databaseSource = databaseSource,
            fileWriter = FileProgressWriter(dispatcher, reporter),
            decompressor = decompressor,
            appSettingsRepository = appSettingsRepository,
        )
    }
}