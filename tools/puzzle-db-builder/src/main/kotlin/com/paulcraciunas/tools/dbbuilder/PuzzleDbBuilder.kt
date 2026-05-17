package com.paulcraciunas.tools.dbbuilder

import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.puzzles.api.PuzzleDatabaseContract
import com.paulcraciunas.serializer.impl.FenSerializer
import com.paulcraciunas.serializer.impl.binary.BinaryAdapter
import com.paulcraciunas.serializer.impl.binary.BinaryPuzzleWriter
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.sql.Connection
import java.sql.DriverManager
import kotlin.random.Random
import kotlin.system.exitProcess

private const val EXPECTED_CSV_COLUMNS = 10
private const val COMPACT_FACTOR = 0.4
private const val LITE_FACTOR = 0.10
private const val MIN_PER_RATING = 5
private const val SEED = 42
private const val BATCH_SIZE = 10_000
private const val PROGRESS_INTERVAL = 100_000L
private const val IDENTITY_HASH = "445cfb97105e05c40e29961f336a0498"

private const val CREATE_PUZZLE_TABLE = """
    CREATE TABLE IF NOT EXISTS Puzzle (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        fenBinary BLOB NOT NULL,
        rating INTEGER NOT NULL
    )
"""
private const val CREATE_RATING_INDEX =
    "CREATE INDEX IF NOT EXISTS index_Puzzle_rating ON Puzzle (rating)"
private const val CREATE_ROOM_MASTER =
    "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)"
private const val INSERT_IDENTITY_HASH =
    "INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES(42, '$IDENTITY_HASH')"
private const val INSERT_PUZZLE =
    "INSERT INTO Puzzle (fenBinary, rating) VALUES (?, ?)"

data class TierConfig(
    val name: String,
    val factor: Double,
)

fun main(args: Array<String>) {
    val csvFile = args.firstOrNull() ?: "puzzles.csv"
    if (!File(csvFile).exists()) {
        System.err.println("Error: CSV file not found: $csvFile")
        exitProcess(1)
    }

    val writer = BinaryPuzzleWriter(
        FenSerializer(RealGameFactory()),
        BinaryAdapter(),
    )

    val tiers = listOf(
        TierConfig(PuzzleDatabaseContract.Tier.FULL, 1.0),
        TierConfig(PuzzleDatabaseContract.Tier.COMPACT, COMPACT_FACTOR),
        TierConfig(PuzzleDatabaseContract.Tier.LITE, LITE_FACTOR),
    )

    println("=== Building Puzzle Databases ===")
    println("Source: $csvFile")
    println()

    tiers.forEach { tier ->
        buildDatabase(csvFile, tier, writer)
    }

    println()
    println("=== Build Complete ===")
    tiers.forEach { tier ->
        val dbFile = File(PuzzleDatabaseContract.databaseFileName(tier.name))
        val size = formatFileSize(dbFile.length())
        val count = countPuzzles(PuzzleDatabaseContract.databaseFileName(tier.name))
        println("  ${tier.name}: $count puzzles ($size)")
    }
}

private fun buildDatabase(
    csvFile: String,
    tier: TierConfig,
    writer: BinaryPuzzleWriter,
) {
    val dbFile = PuzzleDatabaseContract.databaseFileName(tier.name)
    println("Building ${tier.name} database (factor=${tier.factor}, min=$MIN_PER_RATING per rating)...")

    File(dbFile).delete()
    val random = Random(SEED)
    val ratingCounts = mutableMapOf<Int, Int>()
    var totalProcessed = 0L
    var totalWritten = 0L
    var serializationErrors = 0L

    connect(dbFile) { conn ->
        conn.createStatement().use { stmt ->
            stmt.execute(CREATE_PUZZLE_TABLE)
            stmt.execute(CREATE_RATING_INDEX)
            stmt.execute(CREATE_ROOM_MASTER)
            stmt.execute(INSERT_IDENTITY_HASH)
        }

        conn.autoCommit = false
        conn.prepareStatement(INSERT_PUZZLE).use { insertStmt ->
            var batchCount = 0

            BufferedReader(FileReader(csvFile)).use { reader ->
                reader.lineSequence()
                    .drop(1)
                    .filter { it.isNotBlank() && !it.startsWith("#") }
                    .forEach { line ->
                        val tokens = line.split(',')
                        if (tokens.size != EXPECTED_CSV_COLUMNS) return@forEach

                        val fen = tokens[1]
                        val moves = tokens[2]
                        val rating = tokens[3].toIntOrNull() ?: return@forEach

                        totalProcessed++

                        if (!shouldInclude(tier.factor, rating, ratingCounts, random)) {
                            return@forEach
                        }

                        val binary = try {
                            writer.write(fen, moves)
                        } catch (e: Exception) {
                            serializationErrors++
                            if (serializationErrors <= 10) {
                                System.err.println("  Serialization error for puzzle ${tokens[0]}: ${e.message}")
                            }
                            return@forEach
                        }

                        ratingCounts[rating] = (ratingCounts[rating] ?: 0) + 1

                        insertStmt.setBytes(1, binary)
                        insertStmt.setInt(2, rating)
                        insertStmt.addBatch()
                        batchCount++
                        totalWritten++

                        if (batchCount >= BATCH_SIZE) {
                            insertStmt.executeBatch()
                            conn.commit()
                            batchCount = 0
                        }

                        if (totalProcessed % PROGRESS_INTERVAL == 0L) {
                            println("  Processed $totalProcessed, written $totalWritten...")
                        }
                    }
            }

            if (batchCount > 0) {
                insertStmt.executeBatch()
                conn.commit()
            }
        }
        conn.autoCommit = true
    }

    println("  ${tier.name} DB: $totalWritten puzzles written ($serializationErrors serialization errors)")

    verifyDatabase(dbFile, tier.name, ratingCounts)
}

private fun shouldInclude(
    factor: Double,
    rating: Int,
    ratingCounts: Map<Int, Int>,
    random: Random,
): Boolean {
    if (factor >= 1.0) return true

    val currentCount = ratingCounts[rating] ?: 0
    if (currentCount < MIN_PER_RATING) return true

    return random.nextDouble() < factor
}

private fun verifyDatabase(dbFile: String, tierName: String, ratingCounts: Map<Int, Int>) {
    connect(dbFile) { conn ->
        conn.createStatement().use { stmt ->
            val integrity = stmt.executeQuery("PRAGMA integrity_check")
            if (integrity.next()) {
                val result = integrity.getString(1)
                if (result != "ok") {
                    System.err.println("  WARNING: Integrity check failed for $tierName: $result")
                }
            }

            val stats = stmt.executeQuery("SELECT COUNT(*), MIN(rating), MAX(rating) FROM Puzzle")
            if (stats.next()) {
                println("  Verified: ${stats.getInt(1)} puzzles, rating range ${stats.getInt(2)}-${stats.getInt(3)}")
            }

            val distinctRatings = stmt.executeQuery("SELECT COUNT(DISTINCT rating) FROM Puzzle")
            if (distinctRatings.next()) {
                println("  Distinct ratings: ${distinctRatings.getInt(1)}")
            }

            val hash = stmt.executeQuery("SELECT identity_hash FROM room_master_table WHERE id = 42")
            if (hash.next()) {
                val dbHash = hash.getString(1)
                if (dbHash != IDENTITY_HASH) {
                    System.err.println("  WARNING: Identity hash mismatch! Expected $IDENTITY_HASH, got $dbHash")
                }
            }
        }
    }

    val coveredRatings = ratingCounts.size
    val underservedRatings = ratingCounts.filter { it.value < MIN_PER_RATING }.keys.sorted()
    println("  Covered $coveredRatings distinct rating values")
    if (underservedRatings.isNotEmpty()) {
        println("  Note: ${underservedRatings.size} ratings have fewer than $MIN_PER_RATING puzzles (source data limited)")
    }
}

private fun countPuzzles(dbFile: String): Int {
    var count = 0
    connect(dbFile) { conn ->
        conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery("SELECT COUNT(*) FROM Puzzle")
            if (rs.next()) count = rs.getInt(1)
        }
    }
    return count
}

private fun connect(dbFile: String, block: (Connection) -> Unit) {
    DriverManager.getConnection("jdbc:sqlite:$dbFile").use(block)
}

private fun formatFileSize(bytes: Long): String = when {
    bytes >= 1_000_000_000 -> "%.1f GB".format(bytes / 1_000_000_000.0)
    bytes >= 1_000_000 -> "%.1f MB".format(bytes / 1_000_000.0)
    bytes >= 1_000 -> "%.1f KB".format(bytes / 1_000.0)
    else -> "$bytes B"
}
