package com.paulcraciunas.data.network

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.github.luben.zstd.ZstdInputStream
import com.paulcraciunas.data.db.Puzzle
import com.paulcraciunas.data.impl.PuzzleDatabase
import com.paulcraciunas.notifications.api.NotificationFactory
import com.paulcraciunas.serializer.api.PuzzleWriter
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

@HiltWorker
class PuzzleSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val puzzleWriter: PuzzleWriter,
    private val notificationFactory: NotificationFactory,
    private val progressReporter: WorkerProgressReporter,
    private val db: PuzzleDatabase
) : CoroutineWorker(context, workerParams) {

    private val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    private var bytes: Int = 0
    private var step: Step = Step.Download

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        setForegroundAsync(createForegroundInfo())
        progressReporter.init { progress -> setProgress(workDataOf(STEP to step.toString(), PROGRESS_NAME to progress)) }

        val zstFile = File(applicationContext.cacheDir, "puzzles.zst")
        val csvFile = File(applicationContext.cacheDir, "puzzles.csv")

        try {
            downloadDb(URL(LICHESS_URL), zstFile)
            decompressZst(zstFile, csvFile)
            writeDb(csvFile)
            return@withContext Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext Result.retry()
        } finally {
            zstFile.delete()
            csvFile.delete()
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        // Create a Notification channel if necessary
        notificationFactory.createChannel(applicationContext)
        val notification = notificationFactory.createForegroundNotification(applicationContext)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NotificationFactory.Ids.DOWNLOAD_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NotificationFactory.Ids.DOWNLOAD_NOTIFICATION_ID, notification)
        }
    }

    private suspend fun downloadDb(url: URL, zstFile: File) {
        val connection = withContext(Dispatchers.IO) {
            url.openConnection()
        } as HttpURLConnection
        progressReporter.onBegin(connection.contentLength.toLong())

        connection.inputStream.use { input ->
            zstFile.outputStream().use { output ->
                bytes = input.read(buffer)
                while (bytes >= 0) {
                    output.write(buffer, 0, bytes)
                    progressReporter.onCompleted(bytes)
                    bytes = input.read(buffer)
                }
            }
        }
    }

    private suspend fun decompressZst(zstFile: File, csvFile: File) {
        step = Step.Unpack
        progressReporter.onBegin((zstFile.length() * COMPRESS_FACTOR).toLong())
        withContext(Dispatchers.IO) {
            FileInputStream(zstFile).use { fis ->
                ZstdInputStream(fis).use { zis ->
                    FileOutputStream(csvFile).use { fos ->
                        bytes = zis.read(buffer)
                        while (bytes >= 0) {
                            fos.write(buffer, 0, bytes)
                            progressReporter.onCompleted(bytes)
                            bytes = zis.read(buffer)
                        }
                    }
                }
            }
        }
    }

    /**
     * Lichess format is: PuzzleId, FEN, Moves, Rating, RatingDeviation, Popularity, NbPlays,
     * Themes, GameUrl, OpeningTags
     *
     * We want to keep only the following: FEN, Moves, Rating and Themes
     */
    private suspend fun writeDb(csvFile: File) {
        step = Step.BuildDb
        progressReporter.onBegin(DB_SIZE)

        val puzzles = ArrayList<Puzzle>(BULK_INSERT_COUNT)
        csvFile.reader(Charsets.UTF_8).buffered(BUFFER_SIZE).use { reader ->
            val lines = reader.lineSequence().drop(1)
            lines.forEach { line ->
                val tokens = line.split(',')
                if (!line.startsWith("#") && tokens.size == 10) {
                    puzzles.add(Puzzle(fenBinary = puzzleWriter.write(tokens[1], tokens[2]), rating = tokens[3].toInt()))
                }
                insert(puzzles)
            }
        }
    }

    private suspend fun insert(puzzles: ArrayList<Puzzle>) {
        if (puzzles.size == BULK_INSERT_COUNT) {
            try {
                db.bulkInsert(puzzles)
                progressReporter.onCompleted(BULK_INSERT_COUNT)
                puzzles.clear()
            } catch (e: Exception) {
                Log.e(PuzzleSyncWorker::class.java.simpleName, "Bulk insert failed!", e)
            }
        }
    }

    // TODO Paul: move these to Repository layer
    enum class Step {
        Download,
        Unpack,
        BuildDb
    }

    companion object {
        private const val LICHESS_URL = "https://database.lichess.org/lichess_db_puzzle.csv.zst"
        private const val COMPRESS_FACTOR = 3.7f // It's an approximation. Can't get the proper full size from the Zstd library
        private const val BUFFER_SIZE = 32 * 1024
        private const val DB_SIZE = 5_000_000L // The 05/2025 version has 4,824,507 puzzles. We'll use this as an approximation for progress
        private const val BULK_INSERT_COUNT = 50_000 // Insert 50k puzzles at once, to improve performance

        const val STEP = "step"
        const val PROGRESS_NAME = "progress"
        const val TAG = "PuzzleSync"
    }
}
