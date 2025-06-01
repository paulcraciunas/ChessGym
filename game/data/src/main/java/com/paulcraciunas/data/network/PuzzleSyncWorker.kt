package com.paulcraciunas.data.network

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.luben.zstd.ZstdInputStream
import com.paulcraciunas.data.api.PuzzleDatabase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL

@HiltWorker
class PuzzleSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val db: PuzzleDatabase
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
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

    /**
     * Lichess format is: PuzzleId, FEN, Moves, Rating, RatingDeviation, Popularity, NbPlays,
     * Themes, GameUrl, OpeningTags
     *
     * We want to keep only the following: FEN, Moves, Rating and Themes
     */
    private suspend fun writeDb(csvFile: File) {
        csvFile.useLines { lines -> // Read CSV line-by-line
            lines.drop(1).forEach { line -> // The first line contains the CSV schema
                val tokens = line.split(',')
                if (tokens.size == 10) { // ignore if malformed
                    db.insertFromList(listOf(tokens[1], tokens[2], tokens[3], tokens[7]))
                }
            }
        }
    }

    private fun downloadDb(url: URL, zstFile: File) {
        url.openStream().use { input ->
            zstFile.outputStream().use { output ->
                input.copyTo(output)
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

    companion object {
        private const val LICHESS_URL = "https://database.lichess.org/lichess_db_puzzle.csv.zst"
    }
}
