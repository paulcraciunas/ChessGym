package com.paulcraciunas.serializer.impl

import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.loc
import com.paulcraciunas.serializer.impl.binary.BinaryAdapter
import com.paulcraciunas.serializer.impl.binary.BinaryPuzzleReader
import com.paulcraciunas.serializer.impl.binary.BinaryPuzzleWriter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class MutablePuzzleTest {
    private val adapter = BinaryAdapter()
    private val reader = BinaryPuzzleReader(FenSerializer, adapter)
    private val writer = BinaryPuzzleWriter(FenSerializer, adapter)

    @ParameterizedTest(name = "Checking puzzle {0}")
    @MethodSource("fenPuzzles")
    fun `WHEN serializing a loaded game THEN contents are identical`(fenGame: String) {
        val puzzle = reader.readPuzzle(RATING, writer.write(fenGame)).apply { start() }
        val moves = fenGame.split(',')[1].split(' ')
        var from: String
        var to: String
        var expectedMove: Ply?

        assertDoesNotThrow {
            moves.forEach { move ->
                from = move.substring(0, 2)
                to = move.substring(2, 4)
                expectedMove = puzzle.info.plies(from.loc()).find { dest ->
                    dest.to == to.loc()
                }
                // Verify promotions
                if (move.length == 5) { // promotion
                    expectedMove?.promote(
                        Piece.entries.find { it.alg().lowercase().lastOrNull() == move[4] }!!
                    )
                }
                Assertions.assertNotNull(expectedMove) // Verify that the expected move exists
                puzzle.play(expectedMove!!) // Verify that we can play this move
            }
        }
        assertEquals(Puzzle.State.Success, puzzle.state)
    }

    companion object {
        // fen_puzzles.csv is a small subset of ~900 puzzles from all the available puzzles
        @JvmStatic
        fun fenPuzzles(): List<String> =
            ClassLoader.getSystemResource("fen_puzzles.csv").readText().split("\n")
        private const val RATING = 42 // Chosen at random
    }
}