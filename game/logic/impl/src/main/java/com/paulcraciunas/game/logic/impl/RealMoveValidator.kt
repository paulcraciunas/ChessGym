package com.paulcraciunas.game.logic.impl

import com.paulcraciunas.game.logic.api.MoveValidator
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.plies.strategies.BishopPlyStrategy
import com.paulcraciunas.game.logic.impl.plies.strategies.KnightPlyStrategy
import com.paulcraciunas.game.logic.impl.plies.strategies.Next
import com.paulcraciunas.game.logic.impl.plies.strategies.QueenPlyStrategy
import com.paulcraciunas.game.logic.impl.plies.strategies.RookPlyStrategy

/**
 * Implementation of [MoveValidator] that uses the existing ply strategies
 * to validate moves and check attacks.
 */
class RealMoveValidator : MoveValidator {

    override fun canAttack(piece: Piece, side: Side, from: Locus, to: Locus, board: IBoard): Boolean {
        if (from == to) return false

        return when (piece) {
            Piece.Rook -> canAttackInDirections(from, to, board, RookPlyStrategy.directions)
            Piece.Bishop -> canAttackInDirections(from, to, board, BishopPlyStrategy.directions)
            Piece.Queen -> canAttackInDirections(from, to, board, QueenPlyStrategy.directions)
            Piece.Knight -> canAttackWithSimpleMoves(from, to, KnightPlyStrategy.standardMoves)
            Piece.Pawn -> canAttackAsPawn(from, to, side)
            // King uses queen directions but only one square
            Piece.King -> canAttackWithSimpleMoves(from, to, QueenPlyStrategy.directions)
        }
    }

    override fun isSquareUnderAttack(square: Locus, attackingSide: Side, board: IBoard): Boolean {
        var isUnderAttack = false
        board.forEachPiece(attackingSide) { piece, pieceLocation ->
            if (canAttack(piece, attackingSide, pieceLocation, square, board)) {
                isUnderAttack = true
            }
        }
        return isUnderAttack
    }

    override fun getValidMoves(
        piece: Piece,
        side: Side,
        from: Locus,
        board: IBoard,
        blockers: Set<Locus>
    ): Set<Locus> {
        val allBlockers = blockers + getOccupiedSquares(side, board)

        return when (piece) {
            Piece.Rook -> getMovesInDirections(from, board, RookPlyStrategy.directions, allBlockers)
            Piece.Bishop -> getMovesInDirections(from, board, BishopPlyStrategy.directions, allBlockers)
            Piece.Queen -> getMovesInDirections(from, board, QueenPlyStrategy.directions, allBlockers)
            Piece.Knight -> getSimpleMoves(from, KnightPlyStrategy.standardMoves, allBlockers)
            Piece.King -> getSimpleMoves(from, QueenPlyStrategy.directions, allBlockers)
            Piece.Pawn -> emptySet() // Pawns are not used in this game mode
        }
    }

    private fun canAttackInDirections(
        from: Locus,
        to: Locus,
        board: IBoard,
        directions: Collection<Next>
    ): Boolean {
        for (direction in directions) {
            var current = direction(from)
            while (current != null) {
                if (current == to) return true
                // Stop if we hit a piece (can't attack through pieces)
                if (board.at(current) != null) break
                current = direction(current)
            }
        }
        return false
    }

    private fun canAttackWithSimpleMoves(
        from: Locus,
        to: Locus,
        moves: Collection<Next>
    ): Boolean {
        return moves.any { move -> move(from) == to }
    }

    private fun canAttackAsPawn(from: Locus, to: Locus, side: Side): Boolean {
        // Pawns attack diagonally
        val attacks = if (side == Side.WHITE) {
            listOfNotNull(from.topLeft(), from.topRight())
        } else {
            listOfNotNull(from.downLeft(), from.downRight())
        }
        return to in attacks
    }

    private fun getMovesInDirections(
        from: Locus,
        board: IBoard,
        directions: Collection<Next>,
        blockers: Set<Locus>
    ): Set<Locus> {
        val moves = mutableSetOf<Locus>()
        for (direction in directions) {
            var current = direction(from)
            while (current != null && current !in blockers) {
                moves.add(current)
                // Stop if we hit a piece on the board
                if (board.at(current) != null) break
                current = direction(current)
            }
        }
        return moves
    }

    private fun getSimpleMoves(
        from: Locus,
        moves: Collection<Next>,
        blockers: Set<Locus>
    ): Set<Locus> {
        return moves.mapNotNull { move -> move(from) }
            .filter { it !in blockers }
            .toSet()
    }

    private fun getOccupiedSquares(side: Side, board: IBoard): Set<Locus> {
        val occupied = mutableSetOf<Locus>()
        board.forEachPiece(side) { _, loc -> occupied.add(loc) }
        return occupied
    }
}
