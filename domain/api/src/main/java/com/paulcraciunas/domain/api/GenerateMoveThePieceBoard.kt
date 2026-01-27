package com.paulcraciunas.domain.api

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

/**
 * Use case for generating a board configuration for the "Move the Piece" game.
 *
 * Generates a random board with a player piece and opposing pieces, ensuring
 * that at least one valid sequence of moves exists for the player.
 */
interface GenerateMoveThePieceBoard {
    /**
     * Generates a new board configuration.
     *
     * @param piece The type of piece the player will control
     * @param requiredMoves The number of moves the player needs to make
     * @param opposingPieceCount The number of opposing pieces on the board
     * @return A board configuration with guaranteed valid move sequence
     * @throws IllegalStateException if a new board cannot be generated
     */
    operator fun invoke(
        piece: Piece,
        requiredMoves: Int,
        opposingPieceCount: Int
    ): MoveThePieceBoardData
}

data class MoveThePieceBoardData(
    val playerPieceLocus: Locus,
    val board: IBoard,
) {
    val opposingPieces: Map<Locus, Piece> = blackPieces()

    private fun blackPieces(): Map<Locus, Piece> {
        val results = mutableMapOf<Locus, Piece>()
        board.forEachPiece(Side.BLACK) { piece, loc ->
            results[loc] = piece
        }
        return results
    }
}
