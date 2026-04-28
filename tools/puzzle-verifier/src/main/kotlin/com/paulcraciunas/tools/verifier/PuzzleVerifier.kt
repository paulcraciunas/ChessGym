package com.paulcraciunas.tools.verifier

import com.github.luben.zstd.ZstdInputStream
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.loc
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.serializer.impl.FenSerializer
import com.paulcraciunas.serializer.impl.binary.BinaryAdapter
import com.paulcraciunas.serializer.impl.binary.BinaryPuzzleReader
import com.paulcraciunas.serializer.impl.binary.BinaryPuzzleWriter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import kotlin.system.exitProcess

private const val LICHESS_URL = "https://database.lichess.org/lichess_db_puzzle.csv.zst"
private const val EXPECTED_CSV_COLUMNS = 10
private const val PROGRESS_INTERVAL = 100_000
private const val DUMMY_RATING = 1500

fun main() {
    val factory = RealGameFactory()
    val adapter = BinaryAdapter()
    val writer = BinaryPuzzleWriter(FenSerializer(factory), adapter)
    val reader = BinaryPuzzleReader(factory, adapter)

    val failures = mutableListOf<FailedPuzzle>()
    var totalProcessed = 0L
    var totalSkipped = 0L

    println("Downloading and decompressing Lichess puzzle database...")
    println("URL: $LICHESS_URL")

    val connection = URL(LICHESS_URL).openConnection()
    val zstdStream = ZstdInputStream(connection.getInputStream())
    val bufferedReader = BufferedReader(InputStreamReader(zstdStream, Charsets.UTF_8))

    bufferedReader.use { csv ->
        csv.lineSequence()
            .drop(1) // Skip CSV header
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .forEach { line ->
                val tokens = line.split(',')
                if (tokens.size != EXPECTED_CSV_COLUMNS) {
                    totalSkipped++
                    return@forEach
                }

                val puzzleId = tokens[0]
                val fen = tokens[1]
                val moves = tokens[2]
                val rating = tokens[3].toIntOrNull() ?: DUMMY_RATING

                verifySerialization(writer, puzzleId, fen, moves, failures)?.let { bytes ->
                    verifyDeserialization(reader, rating, bytes, puzzleId, fen, moves, failures)
                        ?.let { puzzle -> verifyPlaythrough(puzzle, puzzleId, fen, moves, failures) }
                }

                totalProcessed++
                if (totalProcessed % PROGRESS_INTERVAL == 0L) {
                    printProgress(totalProcessed, failures.size)
                }
            }
    }

    printReport(totalProcessed, totalSkipped, failures)

    if (failures.isNotEmpty()) {
        exitProcess(1)
    }
}

private fun verifySerialization(
    writer: BinaryPuzzleWriter,
    puzzleId: String,
    fen: String,
    moves: String,
    failures: MutableList<FailedPuzzle>,
): ByteArray? = try {
    writer.write(fen, moves)
} catch (e: Exception) {
    failures.add(
        FailedPuzzle(puzzleId, fen, moves, FailureType.SERIALIZATION, e.messageOrName())
    )
    null
}

private fun verifyDeserialization(
    reader: BinaryPuzzleReader,
    rating: Int,
    bytes: ByteArray,
    puzzleId: String,
    fen: String,
    moves: String,
    failures: MutableList<FailedPuzzle>,
): Puzzle? = try {
    reader.readPuzzle(rating, bytes)
} catch (e: Exception) {
    failures.add(
        FailedPuzzle(puzzleId, fen, moves, FailureType.DESERIALIZATION, e.messageOrName())
    )
    null
}

private fun verifyPlaythrough(
    puzzle: Puzzle,
    puzzleId: String,
    fen: String,
    moves: String,
    failures: MutableList<FailedPuzzle>,
) {
    try {
        puzzle.start()
        val moveList = moves.split(' ')
        moveList.forEach { move ->
            val from: Locus = move.substring(0, 2).loc()
            val to: Locus = move.substring(2, 4).loc()
            val ply = puzzle.ply(from, to)
                ?: error("No valid ply found for move $move (from=$from, to=$to)")

            if (move.length == 5) {
                val promotionPiece = Piece.entries
                    .find { it.alg().lowercase().lastOrNull() == move[4] }
                    ?: error("Unknown promotion piece: ${move[4]}")
                ply.promote(promotionPiece)
            }
            puzzle.play(ply)
        }
        check(puzzle.state == Puzzle.State.Success) {
            "Expected Success but was ${puzzle.state}"
        }
    } catch (e: Exception) {
        failures.add(
            FailedPuzzle(puzzleId, fen, moves, FailureType.PLAYTHROUGH, e.messageOrName())
        )
    }
}

private fun printProgress(totalProcessed: Long, failureCount: Int) {
    println("  Processed $totalProcessed puzzles ($failureCount failures so far)")
}

private fun printReport(
    totalProcessed: Long,
    totalSkipped: Long,
    failures: List<FailedPuzzle>,
) {
    println()
    println("=".repeat(80))
    println("PUZZLE VERIFICATION REPORT")
    println("=".repeat(80))
    println("Total processed: $totalProcessed")
    println("Total skipped (malformed lines): $totalSkipped")
    println("Total failures:  ${failures.size}")

    if (failures.isEmpty()) {
        println()
        println("All puzzles passed verification.")
        return
    }

    val grouped = failures.groupBy { it.failureType }
    println()
    println("Failures by type:")
    FailureType.entries.forEach { type ->
        val count = grouped[type]?.size ?: 0
        println("  $type: $count")
    }

    println()
    println("Failed puzzles:")
    println("-".repeat(80))
    failures.forEach { failure ->
        println("  ID: ${failure.id}")
        println("  FEN: ${failure.fen}")
        println("  Moves: ${failure.moves}")
        println("  Type: ${failure.failureType}")
        println("  Error: ${failure.errorMessage}")
        println("  -".repeat(40))
    }
}

private fun Exception.messageOrName(): String = message ?: this::class.simpleName ?: "Unknown"
