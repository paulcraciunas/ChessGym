package com.paulcraciunas.puzzles.impl.network.save

import com.paulcraciunas.puzzles.impl.network.fakes.FakeProgressReporter
import com.paulcraciunas.puzzles.impl.network.fakes.FakePuzzleDatabase
import com.paulcraciunas.puzzles.impl.network.fakes.FakePuzzleWriter
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class CsvPuzzleDatabaseWriterTest {

    private val reporter = FakeProgressReporter()
    private val testPuzzleWriter = FakePuzzleWriter()
    private val testDatabase = FakePuzzleDatabase()

    private val underTest = CsvPuzzleDatabaseWriter(reporter, testPuzzleWriter, testDatabase)

    @Test
    fun `GIVEN valid CSV file WHEN writePuzzlesToDatabase is called THEN puzzles are written with progress updates`(@TempDir tempDir: File) =
        runTest {
            // Given
            val csvContent = """
            PuzzleId,FEN,Moves,Rating,RatingDeviation,Popularity,NbPlays,Themes,GameUrl,OpeningTags
            00008,rnbqkb1r/pppp1ppp/5n2/4p3/2B1P3/8/PPPP1PPP/RNBQK1NR w KQkq - 2 3,f2f3 d8h4,1411,74,88,135,advantage hangingPiece long middlegame,https://lichess.org/GukToZd3/black#6,Italian_Game Italian_Game_Anti-Arian_Variation
            00009,r1bqkb1r/pppp1ppp/2n2n2/4p3/2B1P3/3P1N2/PPP2PPP/RNBQK2R b KQkq - 0 4,f6d5 e4d5,1454,76,89,269,advantage hangingPiece short middlegame,https://lichess.org/F8M2Haif/black#8,Italian_Game Italian_Game_Classical_Variation
        """.trimIndent()

            val csvFile = File(tempDir, "test.csv")
            csvFile.writeText(csvContent)

            // When
            underTest.writePuzzlesToDatabase(csvFile)

            // Then
            assertEquals(2, testDatabase.insertedPuzzles.size)
            assertTrue(reporter.progressUpdates.isNotEmpty())
            assertEquals(2, reporter.progressUpdates.sum()) // 2 puzzles processed

            // Verify puzzle data
            val firstPuzzle = testDatabase.insertedPuzzles[0]
            assertEquals(1411, firstPuzzle.rating)
            assertEquals(
                "rnbqkb1r/pppp1ppp/5n2/4p3/2B1P3/8/PPPP1PPP/RNBQK1NR w KQkq - 2 3|f2f3 d8h4",
                String(firstPuzzle.fenBinary)
            )
        }

    @Test
    fun `GIVEN CSV with invalid lines WHEN writePuzzlesToDatabase is called THEN invalid lines are skipped`(@TempDir tempDir: File) =
        runTest {
            // Given
            val csvContent = """
            PuzzleId,FEN,Moves,Rating,RatingDeviation,Popularity,NbPlays,Themes,GameUrl,OpeningTags
            # This is a comment line
            invalid,line,with,few,columns
            00008,rnbqkb1r/pppp1ppp/5n2/4p3/2B1P3/8/PPPP1PPP/RNBQK1NR w KQkq - 2 3,f2f3 d8h4,1411,74,88,135,advantage hangingPiece long middlegame,https://lichess.org/GukToZd3/black#6,Italian_Game Italian_Game_Anti-Arian_Variation
        """.trimIndent()

            val csvFile = File(tempDir, "test.csv")
            csvFile.writeText(csvContent)

            // When
            underTest.writePuzzlesToDatabase(csvFile)

            // Then
            assertEquals(1, testDatabase.insertedPuzzles.size) // Only 1 valid puzzle
        }

    @Test
    fun `GIVEN database error WHEN writePuzzlesToDatabase is called THEN exception is not propagated`(@TempDir tempDir: File) = runTest {
        // Given
        val csvContent = """
            PuzzleId,FEN,Moves,Rating,RatingDeviation,Popularity,NbPlays,Themes,GameUrl,OpeningTags
            00008,rnbqkb1r/pppp1ppp/5n2/4p3/2B1P3/8/PPPP1PPP/RNBQK1NR w KQkq - 2 3,f2f3 d8h4,1411,74,88,135,advantage hangingPiece long middlegame,https://lichess.org/GukToZd3/black#6,Italian_Game Italian_Game_Anti-Arian_Variation
        """.trimIndent()

        val csvFile = File(tempDir, "test.csv")
        csvFile.writeText(csvContent)

        testDatabase.shouldThrowError = true

        // When & Then
        underTest.writePuzzlesToDatabase(csvFile)
    }

    @Test
    fun `GIVEN large number of puzzles WHEN writePuzzlesToDatabase is called THEN bulk inserts are performed correctly`(@TempDir tempDir: File) =
        runTest {
            // Given
            val csvBuilder = StringBuilder()
            csvBuilder.appendLine("PuzzleId,FEN,Moves,Rating,RatingDeviation,Popularity,NbPlays,Themes,GameUrl,OpeningTags")

            // Create 100 puzzles to test bulk insert logic
            repeat(100) { i ->
                csvBuilder.appendLine("$i,rnbqkb1r/pppp1ppp/5n2/4p3/2B1P3/8/PPPP1PPP/RNBQK1NR w KQkq - 2 3,f2f3 d8h4,${1400 + i},74,88,135,advantage,https://lichess.org/test$i,Italian_Game")
            }

            val csvFile = File(tempDir, "large_test.csv")
            csvFile.writeText(csvBuilder.toString())

            // When
            underTest.writePuzzlesToDatabase(csvFile)

            // Then
            assertEquals(100, testDatabase.insertedPuzzles.size)
            assertEquals(100, reporter.progressUpdates.sum())

            // Verify ratings are correct
            testDatabase.insertedPuzzles.forEachIndexed { index, puzzle ->
                assertEquals(1400 + index, puzzle.rating)
            }
        }
}
