package com.paulcraciunas.puzzles.impl.network.save

import android.util.Log
import com.paulcraciunas.puzzles.impl.db.Puzzle
import com.paulcraciunas.puzzles.impl.impl.PuzzleDatabase
import com.paulcraciunas.puzzles.impl.network.progress.ProgressReporter
import com.paulcraciunas.serializer.api.PuzzleWriter
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import java.io.File
import javax.inject.Inject

class CsvPuzzleDatabaseWriter @Inject constructor(
    private val progressReporter: ProgressReporter,
    private val puzzleWriter: PuzzleWriter,
    private val db: PuzzleDatabase,
    private val appSettingsRepository: AppSettingsRepository
) : PuzzleDatabaseWriter {
    private var totalWritten = 0
    private var maxRating = 0
    private var minRating = Integer.MAX_VALUE

    override suspend fun writePuzzlesToDatabase(csvSource: File) {
        val puzzles = ArrayList<Puzzle>(BULK_INSERT_COUNT)
        var written: Int
        var currentMax = maxRating
        var currentMin = minRating
        var currentRating: Int

        suspend fun updateValues() {
            written = insertBulk(puzzles)
            if (written > 0) {
                totalWritten += written
                maxRating = maxRating.coerceAtLeast(currentMax)
                minRating = minRating.coerceAtMost(currentMin)
                currentMax = maxRating
                currentMin = minRating
            }
        }

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
                    currentMax = currentMax.coerceAtLeast(currentRating)
                    currentMin = currentMin.coerceAtMost(currentRating)

                    if (puzzles.size == BULK_INSERT_COUNT) {
                        updateValues()
                    }
                }
            }

            // Insert remaining puzzles
            if (puzzles.isNotEmpty()) {
                updateValues()
            }
        }
        appSettingsRepository.updateTotalPuzzleCount(totalWritten)
        appSettingsRepository.updateMaxPuzzleRating(maxRating)
        appSettingsRepository.updateMinPuzzleRating(minRating)
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
