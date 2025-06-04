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
import com.paulcraciunas.game.io.api.PuzzleWriter
import com.paulcraciunas.notifications.api.NotificationFactory
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

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // TODO Paul: add support for retry/continue. Pick up from where we left off. Perhaps save how many records we've inserted
        // TODO Paul: check if the files already exist. If they do, don't bother recreating them.
        setForeground(getForegroundInfo())
        progressReporter.init { progress -> setProgress(workDataOf(PROGRESS_NAME to progress)) }

        val zstFile = File(applicationContext.cacheDir, "puzzles.zst")
        val csvFile = File(applicationContext.cacheDir, "puzzles.csv")

        try {
            downloadDb(URL(LICHESS_URL), zstFile)
            progressReporter.onDecompressing()
            decompressZst(zstFile, csvFile)
            progressReporter.onDecompressDone()
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

    override suspend fun getForegroundInfo(): ForegroundInfo {
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
        progressReporter.onBeginDownload(connection.contentLength)

        connection.inputStream.use { input ->
            zstFile.outputStream().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var bytes = input.read(buffer)
                while (bytes >= 0) {
                    output.write(buffer, 0, bytes)
                    progressReporter.onDownloaded(bytes)
                    bytes = input.read(buffer)
                }
            }
        }
    }

    private fun decompressZst(zstFile: File, csvFile: File) {
        FileInputStream(zstFile).use { fis ->
            ZstdInputStream(fis).use { zis ->
                FileOutputStream(csvFile).use { fos ->
                    zis.copyTo(fos)
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
        // === Parse CSV and insert with progress ===
        csvFile.useLines { lines ->
            progressReporter.onBeginInsert(DB_SIZE)
            val puzzles = ArrayList<Puzzle>(BULK_INSERT_COUNT)
            lines.drop(1).forEach { line ->
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
                progressReporter.onInserted(BULK_INSERT_COUNT)
                puzzles.clear()
            } catch (e: Exception) {
                Log.e(PuzzleSyncWorker::class.java.simpleName, "Bulk insert failed!", e)
            }
        }
    }

    companion object {
        private const val LICHESS_URL = "https://database.lichess.org/lichess_db_puzzle.csv.zst"
        private const val DB_SIZE = 5_000_000 // The 05/2025 version has 4,824,507 puzzles. We'll use this as an approximation for progress
        private const val BULK_INSERT_COUNT = 50_000 // Insert 50k puzzles at once, to improve performance

        const val PROGRESS_NAME = "progress"
        const val TAG = "PuzzleSync"
    }
}
