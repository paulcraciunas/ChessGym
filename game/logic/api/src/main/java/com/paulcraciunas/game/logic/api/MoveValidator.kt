package com.paulcraciunas.game.logic.api

import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

/**
 * Validates moves and checks attacks on the board.
 *
 * This interface provides move validation functionality that can be used
 * outside the context of a full chess game (e.g., for training exercises
 * where normal chess rules may not fully apply).
 */
interface MoveValidator {
    /**
     * Checks if a piece at [from] can attack the square [to].
     *
     * @param piece The type of piece
     * @param side The side (color) of the piece
     * @param from The current position of the piece
     * @param to The target square to check
     * @param board The current board state
     * @return true if the piece can attack the target square
     */
    fun canAttack(piece: Piece, side: Side, from: Locus, to: Locus, board: IBoard): Boolean

    /**
     * Checks if a square is under attack by any piece of the given side.
     *
     * @param square The square to check
     * @param attackingSide The side that might be attacking
     * @param board The current board state
     * @return true if the square is under attack
     */
    fun isSquareUnderAttack(square: Locus, attackingSide: Side, board: IBoard): Boolean

    /**
     * Gets all squares that a piece can move to from its current position.
     * This returns raw movement squares without considering check or other game rules.
     *
     * @param piece The type of piece
     * @param side The side (color) of the piece
     * @param from The current position of the piece
     * @param board The current board state
     * @param blockers Additional squares that block movement (e.g., visited squares)
     * @return Set of squares the piece can move to
     */
    fun getValidMoves(
        piece: Piece,
        side: Side,
        from: Locus,
        board: IBoard,
        blockers: Set<Locus> = emptySet()
    ): Set<Locus>
}
