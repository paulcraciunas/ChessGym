package com.paulcraciunas.game.logic

import com.paulcraciunas.game.logic.board.Board
import com.paulcraciunas.game.logic.board.Locus
import com.paulcraciunas.game.logic.board.Piece
import com.paulcraciunas.game.logic.plies.Ply
import com.paulcraciunas.game.logic.plies.PromotionPly
import java.util.Queue

//TODO Paul: exposing the game is temporary. Figure out the API and clean everything up
class Puzzle(
    val game: Game,
    private val moves: Queue<String>
) {
    private var result: PuzzleResult? = null

    fun state(): GameState = game.state()
    fun turn(): Side = game.turn()
    fun isOver(): PuzzleResult? = result
    fun playablePlies(from: Locus): Collection<Ply> =
        game.playablePlies(from)

    fun board(): Board = game.board()
    fun requiresPromotion(ply: Ply): Boolean = game.requiresPromotion(ply)
    fun promote(piece: Piece, on: Ply) = game.promote(piece, on)
    fun resign() = game.resign()
    fun play(ply: Ply) {
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
                result = PuzzleResult.Success
            }
        } else {
            // If the move played wasn't the expected one, we failed
            result = PuzzleResult.Failed
        }
    }

    enum class PuzzleResult {
        Failed,
        Success
    }
}
