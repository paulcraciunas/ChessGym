package com.paulcraciunas.puzzles.impl.network

// TODO(https://github.com/paulcraciunas/ChessGym/issues/65) Paul: Extract this implementation to a separate module
//noinspection PureDomain
import android.content.Context
//noinspection PureDomain
import android.content.pm.ServiceInfo
//noinspection PureDomain
import android.database.sqlite.SQLiteDatabase
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
import com.paulcraciunas.puzzles.api.PuzzleDatabaseContract
import com.paulcraciunas.puzzles.impl.network.progress.ProgressReporter
import com.paulcraciunas.puzzles.impl.network.source.PuzzleDatabaseSource
import com.paulcraciunas.puzzles.impl.network.unpack.FileDecompressor
import com.paulcraciunas.puzzles.impl.network.writer.FileWriter
import com.paulcraciunas.settings.application.api.AppSettingsRepository
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
    private val appSettingsRepository: AppSettingsRepository,
) : CoroutineWorker(context, workerParams) {
    private val ioDispatcher = dispatcher
    private var step: Step = Step.Download

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        setForegroundAsync(createForegroundInfo())
        progressReporter.init { progress ->
            setProgress(workDataOf(STEP to step.toString(), PROGRESS_NAME to progress))
        }

        val tierSegment = inputData.getString(INPUT_TIER) ?: PuzzleDatabaseContract.Tier.COMPACT
        val zstFile = File(applicationContext.cacheDir, DOWNLOAD_FILENAME)
        val dbFile = File(applicationContext.cacheDir, UNPACK_FILENAME)

        try {
            downloadPuzzleDatabase(tierSegment, zstFile, dbFile)?.let { return@withContext it }
            decompressPuzzleDatabase(zstFile, dbFile, tierSegment)?.let { return@withContext it }
            return@withContext placePuzzleDatabase(dbFile)
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error during puzzle database sync")
            zstFile.delete()
            dbFile.delete()
            return@withContext Result.failure(
                workDataOf(ERROR_TYPE to ERROR_UNKNOWN, FAILED_STEP to step.toString())
            )
        }
    }

    private suspend fun downloadPuzzleDatabase(tierSegment: String, destination: File, dbFile: File): Result? {
        step = Step.Download
        if (!destination.exists() && !dbFile.exists()) {
            try {
                fileWriter.onBegin(databaseSource.open(tierSegment))
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

    private suspend fun decompressPuzzleDatabase(source: File, destination: File, tierSegment: String): Result? {
        step = Step.Unpack
        if (!destination.exists()) {
            try {
                fileWriter.onBegin((source.length() * compressionFactor(tierSegment)).toLong())
                fileWriter.write(decompressor.decompress(source.inputStream()), destination)
                source.delete() // Only delete download file after successful decompression
                Timber.i("Download file deleted after successful decompression")
            } catch (e: Exception) {
                Timber.e(e, "Decompression failed")
                destination.delete() // Keep download file for retry, but clean up any partial decompression
                return Result.failure(
                    workDataOf(ERROR_TYPE to ERROR_DECOMPRESSION_FAILED, FAILED_STEP to Step.Unpack.toString())
                )
            }
        } else {
            Timber.i("DB file already exists, skipping decompression step")
            source.delete() // Make sure the downloaded file is cleared
            setProgress(workDataOf(STEP to step.toString(), PROGRESS_NAME to 100))
        }

        return null
    }

    private suspend fun placePuzzleDatabase(dbFile: File): Result {
        val roomDbPath = applicationContext.getDatabasePath(PuzzleDatabaseContract.ROOM_DATABASE_NAME)
        try {
            roomDbPath.parentFile?.mkdirs()
            dbFile.copyTo(roomDbPath, overwrite = true)
            dbFile.delete()

            verifyDatabaseIntegrity(roomDbPath)
            updateDatabaseStats(roomDbPath)

            appSettingsRepository.updatePuzzlesDownloaded(true)
            Timber.i("Puzzle database placed and verified successfully")
            return Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Database placement failed")
            roomDbPath.delete()
            return Result.failure(
                workDataOf(ERROR_TYPE to ERROR_DATABASE_WRITE_FAILED, FAILED_STEP to Step.Unpack.toString())
            )
        }
    }

    private fun verifyDatabaseIntegrity(dbFile: File) {
        SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            db.rawQuery("PRAGMA integrity_check", null).use { cursor ->
                if (cursor.moveToFirst()) {
                    val result = cursor.getString(0)
                    check(result == "ok") { "Database integrity check failed: $result" }
                }
            }
        }
    }

    private fun updateDatabaseStats(dbFile: File) {
        SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            db.rawQuery("SELECT COUNT(*), MIN(rating), MAX(rating) FROM Puzzle", null).use { cursor ->
                if (cursor.moveToFirst()) {
                    val count = cursor.getInt(0)
                    val minRating = cursor.getInt(1)
                    val maxRating = cursor.getInt(2)
                    Timber.i("Database stats: count=$count, minRating=$minRating, maxRating=$maxRating")
                }
            }
        }
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
        Unpack;

        companion object {
            fun fromString(value: String?): Step? = when (value) {
                "Download" -> Download
                "Unpack" -> Unpack
                else -> null
            }
        }
    }

    companion object {
        private const val DOWNLOAD_FILENAME = "puzzles.db.zst"
        private const val UNPACK_FILENAME = "puzzles.db"

        const val INPUT_TIER = "tier"
        const val STEP = "step"
        const val PROGRESS_NAME = "progress"
        const val ERROR_TYPE = "error_type"
        const val FAILED_STEP = "failed_step"
        const val TAG = "PuzzleSync"

        const val ERROR_DOWNLOAD_FAILED = "download_failed"
        const val ERROR_DECOMPRESSION_FAILED = "decompression_failed"
        const val ERROR_DATABASE_WRITE_FAILED = "database_write_failed"
        const val ERROR_UNKNOWN = "unknown_error"

        private fun compressionFactor(tierSegment: String): Float = when (tierSegment) {
            PuzzleDatabaseContract.Tier.FULL -> 1.88f
            PuzzleDatabaseContract.Tier.COMPACT -> 1.73f
            else -> 1.0f
        }
    }
}
