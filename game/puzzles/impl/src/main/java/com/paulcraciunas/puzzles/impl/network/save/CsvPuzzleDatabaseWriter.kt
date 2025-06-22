package com.paulcraciunas.puzzles.impl.network.save

import android.util.Log
import com.paulcraciunas.puzzles.impl.db.Puzzle
import com.paulcraciunas.puzzles.impl.impl.PuzzleDatabase
import com.paulcraciunas.puzzles.impl.network.progress.ProgressReporter
import com.paulcraciunas.serializer.api.PuzzleWriter
import com.paulcraciunas.settings.application.AppSettingsRepository
import java.io.File
import javax.inject.Inject

class CsvPuzzleDatabaseWriter @Inject constructor(
    private val progressReporter: ProgressReporter,
    private val puzzleWriter: PuzzleWriter,
    private val db: PuzzleDatabase,
    private val appSettingsRepository: AppSettingsRepository
) : PuzzleDatabaseWriter {

    override suspend fun writePuzzlesToDatabase(csvSource: File) {
        val puzzles = ArrayList<Puzzle>(BULK_INSERT_COUNT)
        var written = 0
        var maxRating = 0
        var currentRating: Int

        csvSource.reader(Charsets.UTF_8).buffered(BUFFER_SIZE).use { reader ->
            val lines = reader.lineSequence().drop(1) // Skip header

            lines.forEach { line ->
                val tokens = line.split(',')
                if (!line.startsWith("#") && tokens.size == 10) {
                    currentRating = tokens[3].toInt()
                    puzzles.add(
                        Puzzle(
                            fenBinary = puzzleWriter.write(tokens[1], tokens[2]),
                            rating = currentRating
                        )
                    )
                    maxRating = maxRating.coerceAtLeast(currentRating)

                    if (puzzles.size == BULK_INSERT_COUNT) {
                        written += insertBulk(puzzles)
                    }
                }
            }

            // Insert remaining puzzles
            if (puzzles.isNotEmpty()) {
                written += insertBulk(puzzles)
            }
        }
        appSettingsRepository.updateTotalPuzzleCount(written)
        appSettingsRepository.updateMaxPuzzleRating(maxRating)
        appSettingsRepository.updatePuzzlesDownloaded(true)
    }

    private suspend fun insertBulk(puzzles: ArrayList<Puzzle>): Int {
        try {
            val count = puzzles.size
            db.bulkInsert(puzzles)
            progressReporter.onCompleted(puzzles.size)
            puzzles.clear()
            return count
        } catch (e: Exception) {
            Log.e(CsvPuzzleDatabaseWriter::class.java.simpleName, "Bulk insert failed!", e)
            return 0
        }
    }

    companion object {
        private const val BUFFER_SIZE = 32 * 1024
        private const val BULK_INSERT_COUNT = 50_000
    }
}
