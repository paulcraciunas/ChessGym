package com.paulcraciunas.game.logic.impl

import com.paulcraciunas.game.logic.api.IGame
import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.IPuzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.state.IGameState
import com.paulcraciunas.game.logic.impl.plies.PromotionPly
import java.util.Queue

class Puzzle(
    private val game: IGame,
    private val moves: Queue<String>
) : IPuzzle {
    private var result: IPuzzle.Result? = null

    override fun turn(): Side = game.turn()
    override fun board(): IBoard = game.board()
    override fun state(): IGameState = game.state()
    override fun isOver(): IPuzzle.Result? = result
    override fun playablePlies(from: Locus): Collection<Ply> =
        game.playablePlies(from)
    override fun requiresPromotion(ply: Ply): Boolean = game.requiresPromotion(ply)

    override fun play(ply: Ply) {
        assert(moves.isNotEmpty())
        assert(result == null)

        val expected = moves.poll()
        // TODO Paul: fix this down-casting; Also, FIXME! if I remove the cast, tests fail
        val promotedPiece = (ply as? PromotionPly)?.algebraic()?.last()?.lowercase() ?: ""
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
    override fun promote(piece: Piece, on: Ply) = game.promote(piece, on)
    override fun resign() = game.resign()
}