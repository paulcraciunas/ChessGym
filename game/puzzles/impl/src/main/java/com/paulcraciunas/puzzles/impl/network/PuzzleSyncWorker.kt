package com.paulcraciunas.puzzles.impl.network

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.paulcraciunas.notifications.api.NotificationFactory
import com.paulcraciunas.puzzles.impl.network.progress.ProgressReporter
import com.paulcraciunas.puzzles.impl.network.save.PuzzleDatabaseWriter
import com.paulcraciunas.puzzles.impl.network.source.PuzzleDatabaseSource
import com.paulcraciunas.puzzles.impl.network.unpack.FileDecompressor
import com.paulcraciunas.puzzles.impl.network.writer.FileWriter
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@HiltWorker
class PuzzleSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationFactory: NotificationFactory,
    private val progressReporter: ProgressReporter,
    private val databaseSource: PuzzleDatabaseSource,
    private val fileWriter: FileWriter,
    private val decompressor: FileDecompressor,
    private val puzzleDatabaseWriter: PuzzleDatabaseWriter,
) : CoroutineWorker(context, workerParams) {

    private var step: Step = Step.Download

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        setForegroundAsync(createForegroundInfo())
        progressReporter.init { progress ->
            setProgress(workDataOf(STEP to step.toString(), PROGRESS_NAME to progress))
        }

        val zstFile = File(applicationContext.cacheDir, "puzzles.zst")
        val csvFile = File(applicationContext.cacheDir, "puzzles.csv")

        try {
            downloadPuzzleDatabase(zstFile)
            decompressPuzzleDatabase(zstFile, csvFile)
            writePuzzlesToDatabase(csvFile)
            return@withContext Result.success()
        } catch (e: Exception) {
            // TODO Paul: propagate the error so we know the reason why, so we can show appropriate error
            Log.e(PuzzleSyncWorker::class.java.canonicalName, "Failed to provision puzzle database. Will retry later", e)
            return@withContext Result.retry()
        } finally {
            zstFile.delete()
            csvFile.delete()
        }
    }

    private suspend fun downloadPuzzleDatabase(destination: File) {
        step = Step.Download
        fileWriter.onBegin(databaseSource.open())
        fileWriter.write(databaseSource.read(), destination)
    }

    private suspend fun decompressPuzzleDatabase(source: File, destination: File) {
        step = Step.Unpack
        fileWriter.onBegin((source.length() * COMPRESS_FACTOR).toLong())
        fileWriter.write(decompressor.decompress(source.inputStream()), destination)
    }

    private suspend fun writePuzzlesToDatabase(source: File) {
        step = Step.BuildDb
        progressReporter.onBegin(DB_SIZE)
        puzzleDatabaseWriter.writePuzzlesToDatabase(source)
    }

    private fun createForegroundInfo(): ForegroundInfo {
        notificationFactory.createChannel(applicationContext)
        val notification = notificationFactory.createForegroundNotification(applicationContext)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NotificationFactory.Ids.DOWNLOAD_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NotificationFactory.Ids.DOWNLOAD_NOTIFICATION_ID, notification)
        }
    }

    enum class Step {
        Download,
        Unpack,
        BuildDb;

        companion object {
            fun fromString(value: String?) = when(value) {
                "Download" -> Download
                "Unpack" -> Unpack
                "BuildDb" -> BuildDb
                else -> null
            }
        }
    }

    companion object {
        private const val COMPRESS_FACTOR = 3.7f
        private const val DB_SIZE = 5_000_000L
        const val STEP = "step"
        const val PROGRESS_NAME = "progress"
        const val TAG = "PuzzleSync"
    }
}
