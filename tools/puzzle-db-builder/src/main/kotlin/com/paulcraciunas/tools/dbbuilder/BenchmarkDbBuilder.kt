package com.paulcraciunas.tools.dbbuilder

import com.paulcraciunas.logic.builders.Builders
import com.paulcraciunas.serializer.impl.FenSerializer
import com.paulcraciunas.serializer.impl.binary.BinaryAdapter
import com.paulcraciunas.serializer.impl.binary.BinaryPuzzleWriter
import java.io.File
import java.sql.DriverManager

private const val IDENTITY_HASH = "445cfb97105e05c40e29961f336a0498"
private const val MIN_RATING = 400
private const val MAX_RATING = 3000
private const val COPIES_PER_RATING = 10

private const val FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
private const val MOVES = "e2e4 e7e5 g1f3 b8c6 f1c4 f8c5 c2c3 g8f6 d2d4 e5d4"

private const val OUTPUT_FILE_NAME = "puzzles-lite.db"

fun main(args: Array<String>) {
    val outputDir = File(args.firstOrNull() ?: ".")
    outputDir.mkdirs()

    val dbFile = File(outputDir, OUTPUT_FILE_NAME)
    dbFile.delete()

    val writer = BinaryPuzzleWriter(FenSerializer(Builders.gameFactory()), BinaryAdapter())
    val puzzleBinary = writer.write(FEN, MOVES)

    println("=== Building Benchmark Puzzle Database ===")
    println("Output: ${dbFile.absolutePath}")
    println("Ratings: $MIN_RATING–$MAX_RATING ($COPIES_PER_RATING copies per rating)")

    DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}").use { conn ->
        conn.createStatement().use { stmt ->
            stmt.execute(
                """CREATE TABLE IF NOT EXISTS Puzzle (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    fenBinary BLOB NOT NULL,
                    rating INTEGER NOT NULL
                )"""
            )
            stmt.execute("CREATE INDEX IF NOT EXISTS index_Puzzle_rating ON Puzzle (rating)")
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)"
            )
            stmt.execute(
                "INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES(42, '$IDENTITY_HASH')"
            )
        }

        conn.autoCommit = false
        conn.prepareStatement("INSERT INTO Puzzle (fenBinary, rating) VALUES (?, ?)").use { stmt ->
            for (rating in MIN_RATING..MAX_RATING) {
                repeat(COPIES_PER_RATING) {
                    stmt.setBytes(1, puzzleBinary)
                    stmt.setInt(2, rating)
                    stmt.addBatch()
                }
            }
            stmt.executeBatch()
            conn.commit()
        }
        conn.autoCommit = true

        conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery("SELECT COUNT(*), MIN(rating), MAX(rating) FROM Puzzle")
            if (rs.next()) {
                println("Inserted ${rs.getInt(1)} rows, rating range ${rs.getInt(2)}–${rs.getInt(3)}")
            }
        }
    }

    println("File size: ${dbFile.length() / 1024} KB")
    println("=== Done ===")
}
