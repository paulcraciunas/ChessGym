package com.paulcraciunas.tools.puzzles.generate

import com.paulcraciunas.game.io.binary.BinaryPuzzleReader
import com.paulcraciunas.game.logic.api.IPly
import com.paulcraciunas.game.logic.api.IPuzzle
import com.paulcraciunas.game.logic.board.Locus
import com.paulcraciunas.game.logic.board.Piece
import com.paulcraciunas.game.logic.plies.Ply

internal class Validator {
    private val reader = BinaryPuzzleReader()

    fun test(binary: ByteArray, original: String): Boolean {
        val puzzle = reader.readPuzzle(binary)
        val moves = original.split(',')[1].split(' ')
        var from: String
        var to: String
        var expectedMove: IPly?

        try {
            moves.forEach { move ->
                from = move.substring(0, 2)
                to = move.substring(2, 4)
                expectedMove = puzzle.playablePlies(Locus.from(from)!!).find { dest ->
                    dest.to == Locus.from(to)!!
                }
                // Verify promotions
                if (move.length == 5) { // promotion
                    (expectedMove as Ply).accept(
                        Piece.entries.find { it.alg().lowercase().lastOrNull() == move[4] }!!
                    )
                }
                if (expectedMove == null) {// Verify that the expected move exists
                    return false
                }
                puzzle.play(expectedMove!!) // Verify that we can play this move
            }
        } catch (up: Throwable) {
            return false
        }
        return IPuzzle.PuzzleResult.Success == puzzle.isOver()
    }
}
