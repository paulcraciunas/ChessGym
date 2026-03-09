package com.paulcraciunas.puzzles.impl.network

// TODO(https://github.com/paulcraciunas/ChessGym/issues/65) Paul: Extract this implementation to a separate module
//noinspection PureDomain
import android.content.Context
//noinspection PureDomain
import android.content.pm.ServiceInfo
//noinspection PureDomain
import android.os.Build
//noinspection PureDomain
import androidx.hilt.work.HiltWorker
//noinspection PureDomain
import androidx.work.CoroutineWorker
//noinspection PureDomain
import androidx.work.ForegroundInfo
//noinspection PureDomain
import androidx.work.WorkerParameters
//noinspection PureDomain
import androidx.work.workDataOf
import com.paulcraciunas.notifications.api.NotificationFactory
import com.paulcraciunas.puzzles.impl.network.progress.ProgressReporter
import com.paulcraciunas.puzzles.impl.network.save.PuzzleDatabaseWriter
import com.paulcraciunas.puzzles.impl.network.source.PuzzleDatabaseSource
import com.paulcraciunas.puzzles.impl.network.unpack.FileDecompressor
import com.paulcraciunas.puzzles.impl.network.writer.FileWriter
import com.paulcraciunas.utils.IoDispatcher
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

@HiltWorker
class PuzzleSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    @IoDispatcher dispatcher: CoroutineDispatcher,
    private val notificationFactory: NotificationFactory,
    private val progressReporter: ProgressReporter,
    private val databaseSource: PuzzleDatabaseSource,
    private val fileWriter: FileWriter,
    private val decompressor: FileDecompressor,
    private val puzzleDatabaseWriter: PuzzleDatabaseWriter,
) : CoroutineWorker(context, workerParams) {
    private val ioDispatcher = dispatcher
    private var step: Step = Step.Download

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        setForegroundAsync(createForegroundInfo())
        progressReporter.init { progress ->
            setProgress(workDataOf(STEP to step.toString(), PROGRESS_NAME to progress))
        }

        val zstFile = File(applicationContext.cacheDir, DOWNLOAD_FILENAME)
        val csvFile = File(applicationContext.cacheDir, UNPACK_FILENAME)

        try {
            downloadPuzzleDatabase(zstFile, csvFile)?.let { return@withContext it }
            decompressPuzzleDatabase(zstFile, csvFile)?.let { return@withContext it }
            return@withContext writePuzzlesToDatabase(csvFile)
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error during puzzle database sync")
            // Clean up any partial files on unexpected errors
            zstFile.delete()
            csvFile.delete()
            return@withContext Result.failure(
                workDataOf(ERROR_TYPE to ERROR_UNKNOWN, FAILED_STEP to (step.toString()))
            )
        }
    }

    private suspend fun downloadPuzzleDatabase(destination: File, csvFile: File): Result? {
        step = Step.Download
        if (!destination.exists() && !csvFile.exists()) {
            try {
                fileWriter.onBegin(databaseSource.open())
                fileWriter.write(databaseSource.read(), destination)
            } catch (e: Exception) {
                Timber.e(e, "Download failed")
                destination.delete() // Clean up partial downloads
                return Result.failure(
                    workDataOf(ERROR_TYPE to ERROR_DOWNLOAD_FAILED, FAILED_STEP to Step.Download.toString())
                )
            }
        } else {
            Timber.i("Download file already exists, skipping download step")
            setProgress(workDataOf(STEP to step.toString(), PROGRESS_NAME to 100))
        }
        return null
    }

    private suspend fun decompressPuzzleDatabase(source: File, destination: File): Result? {
        step = Step.Unpack
        if (!destination.exists()) {
            try {
                fileWriter.onBegin((source.length() * COMPRESS_FACTOR).toLong())
                fileWriter.write(decompressor.decompress(source.inputStream()), destination)
                source.delete() // Only delete download file after successful decompression
                Timber.i("Download file deleted after successful decompression")
            } catch (e: Exception) {
                Timber.e(e,"Decompression failed")
                destination.delete() // Keep download file for retry, but clean up any partial decompression
                return Result.failure(
                    workDataOf(ERROR_TYPE to ERROR_DECOMPRESSION_FAILED, FAILED_STEP to Step.Unpack.toString())
                )
            }
        } else {
            Timber.i("CSV file already exists, skipping decompression step")
            source.delete() // Make sure the downloaded file is cleared
            setProgress(workDataOf(STEP to step.toString(), PROGRESS_NAME to 100))
        }
        return null
    }

    private suspend fun writePuzzlesToDatabase(source: File): Result {
        step = Step.BuildDb
        try {
            progressReporter.onBegin(DB_SIZE)
            puzzleDatabaseWriter.writePuzzlesToDatabase(source)
            source.delete() // Only delete CSV file after successful database write
            Timber.i("CSV file deleted after successful database write")
        } catch (e: Exception) {
            Timber.e(e,"Database write failed")
            return Result.failure(
                workDataOf(ERROR_TYPE to ERROR_DATABASE_WRITE_FAILED, FAILED_STEP to Step.BuildDb.toString())
            )
        }

        Timber.i("Puzzle database sync completed successfully")
        return Result.success()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        notificationFactory.createChannel(applicationContext)
        val notification = notificationFactory.createForegroundNotification(applicationContext)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NotificationFactory.DOWNLOAD_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NotificationFactory.DOWNLOAD_NOTIFICATION_ID, notification)
        }
    }

    enum class Step {
        Download,
        Unpack,
        BuildDb;

        companion object {
            fun fromString(value: String?) = when (value) {
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
        private const val DOWNLOAD_FILENAME = "puzzles.zst"
        private const val UNPACK_FILENAME = "puzzles.csv"

        const val STEP = "step"
        const val PROGRESS_NAME = "progress"
        const val ERROR_TYPE = "error_type"
        const val FAILED_STEP = "failed_step"
        const val TAG = "PuzzleSync"

        // Error types
        const val ERROR_DOWNLOAD_FAILED = "download_failed"
        const val ERROR_DECOMPRESSION_FAILED = "decompression_failed"
        const val ERROR_DATABASE_WRITE_FAILED = "database_write_failed"
        const val ERROR_UNKNOWN = "unknown_error"
    }
}
