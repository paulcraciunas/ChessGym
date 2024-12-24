package com.paulcraciunas.game

import com.paulcraciunas.game.api.IBoard
import com.paulcraciunas.game.api.IPly
import com.paulcraciunas.game.api.IPuzzle
import com.paulcraciunas.game.board.Locus
import com.paulcraciunas.game.board.Piece
import com.paulcraciunas.game.plies.PromotionPly
import java.util.Queue

class Puzzle(
    private val game: Game,
    private val moves: Queue<String>
) : IPuzzle {
    private var result: IPuzzle.PuzzleResult? = null

    override fun turn(): Side = game.turn()
    override fun isOver(): IPuzzle.PuzzleResult? = result
    override fun playablePlies(from: Locus): Collection<IPly> =
        game.playablePlies(from)

    override fun board(): IBoard = game.board()
    override fun requiresPromotion(ply: IPly): Boolean = game.requiresPromotion(ply)
    override fun promote(piece: Piece, on: IPly) = game.promote(piece, on)
    override fun resign() = game.resign()
    override fun play(ply: IPly) {
        assert(moves.isNotEmpty())
        assert(result == null)

        val expected = moves.poll()
        // We need to also check the promotion. Jesus, forgive me for Down-casting - :puke
        // TODO Paul: perhaps we could hoist this information up to the iPly somehow?!
        val promotedPiece = (ply as? PromotionPly)?.algebraic()?.last()?.lowercase() ?: ""
        // check if the move is the first in the list of expected moves
        if (expected == "${ply.from}${ply.to}$promotedPiece") {
            game.play(ply)
            if (moves.isEmpty()) { // Now check if we have any expected moves left
                result = IPuzzle.PuzzleResult.Success
            }
        } else {
            // If the move played wasn't the expected one, we failed
            result = IPuzzle.PuzzleResult.Failed
        }
    }
}
