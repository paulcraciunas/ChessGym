package com.paulcraciunas.game.logic.impl

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.IPuzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import java.util.Queue

class Puzzle(
    private val game: Game,
    private val moves: Queue<String>
) : IPuzzle {
    private var result: IPuzzle.Result? = null

    override fun turn(): Side = game.info.turn
    override fun board(): IBoard = game.board
    override fun isOver(): IPuzzle.Result? = result
    override fun playablePlies(from: Locus): Collection<Ply> =
        game.info.plies(from)

    override fun play(ply: Ply) {
        assert(moves.isNotEmpty())
        assert(result == null)

        val expected = moves.poll()
        val promotedPiece = if (ply.isPromotion()) ply.algebraic().last().lowercase() else ""
        // check if the move is the first in the list of expected moves
        if (expected == "${ply.from}${ply.to}$promotedPiece") {
            game.play(ply)
            if (moves.isEmpty()) { // Now check if we have any expected moves left
                result = IPuzzle.Result.Success
            }
        } else {
            // If the move played wasn't the expected one, we failed
            result = IPuzzle.Result.Failed
        }
    }
    override fun resign() = game.resign()
}