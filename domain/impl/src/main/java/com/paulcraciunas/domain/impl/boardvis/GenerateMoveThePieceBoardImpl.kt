package com.paulcraciunas.domain.impl.boardvis

import com.paulcraciunas.domain.api.boardvis.GenerateMoveThePieceBoard
import com.paulcraciunas.domain.api.boardvis.MoveThePieceBoardData
import com.paulcraciunas.domain.api.general.RandomFactory
import com.paulcraciunas.domain.impl.GenerateRandomLociImpl
import com.paulcraciunas.game.logic.api.GameFactory
import com.paulcraciunas.game.logic.api.MoveValidator
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import javax.inject.Inject

/**
 * Implementation of [GenerateMoveThePieceBoard] that generates random but valid
 * board configurations for the Move the Piece game.
 *
 * Uses [MoveValidator] from the game logic module to validate moves and attacks.
 */
class GenerateMoveThePieceBoardImpl @Inject constructor(
    private val randomLocus: GenerateRandomLociImpl,
    private val randomFactory: RandomFactory,
    private val gameFactory: GameFactory,
) : GenerateMoveThePieceBoard {

    private val moveValidator: MoveValidator = gameFactory.moveValidator()

    override fun invoke(
        piece: Piece,
        requiredMoves: Int,
        opposingPieceCount: Int,
    ): MoveThePieceBoardData {
        var attempts = 0
        val maxAttempts = 100

        while (attempts < maxAttempts) {
            attempts++
            val board = tryGenerateBoard(piece, requiredMoves, opposingPieceCount)
            if (board != null) {
                return board
            }
        }

        throw IllegalStateException("Failed to generate board for $piece with $requiredMoves moves and $opposingPieceCount pieces")
    }

    private fun tryGenerateBoard(
        piece: Piece,
        requiredMoves: Int,
        opposingPieceCount: Int,
    ): MoveThePieceBoardData? {
        // Generate random player position
        val playerLocus = randomLocus()

        // Generate opposing pieces
        val opposingPieces = mutableMapOf<Locus, Piece>()
        val occupiedSquares = mutableSetOf(playerLocus)

        repeat(opposingPieceCount) {
            var pieceLocus: Locus
            var pieceAttempts = 0
            do {
                pieceLocus = randomLocus()
                pieceAttempts++
            } while (pieceLocus in occupiedSquares && pieceAttempts < 50)

            if (pieceLocus !in occupiedSquares) {
                occupiedSquares.add(pieceLocus)
                val opposingPiece = randomOpposingPiece()
                opposingPieces[pieceLocus] = opposingPiece
            } else {
                // we couldn't place the piece even after 50 attempts
                return null
            }
        }

        // Build a temporary board for validation
        val board = buildBoard(piece, playerLocus, opposingPieces)

        // Verify that a valid path exists
        val hasValidPath = dfsValidPath(
            piece = piece,
            currentLocus = playerLocus,
            board = board,
            visitedSquares = setOf(playerLocus),
            remainingMoves = requiredMoves
        )

        return if (hasValidPath) {
            MoveThePieceBoardData(playerPieceLocus = playerLocus, board = board)
        } else {
            null
        }
    }

    private fun buildBoard(
        playerPiece: Piece,
        playerLocus: Locus,
        opposingPieces: Map<Locus, Piece>,
    ): IBoard {

        val builder = gameFactory.builder()
            .withPiece(playerPiece, Side.WHITE, playerLocus)

        opposingPieces.forEach { (locus, piece) ->
            builder.withPiece(piece, Side.BLACK, locus)
        }

        return builder.buildBoard()
    }

    private fun dfsValidPath(
        piece: Piece,
        currentLocus: Locus,
        board: IBoard,
        visitedSquares: Set<Locus>,
        remainingMoves: Int,
    ): Boolean {
        if (remainingMoves <= 0) return true

        val validMoves = moveValidator.getValidMoves(
            piece = piece,
            side = Side.WHITE,
            from = currentLocus,
            board = board,
            blockers = visitedSquares
        )

        val safeMoves = validMoves.filter { move ->
            !moveValidator.isSquareUnderAttack(move, Side.BLACK, board)
        }

        for (move in safeMoves) {
            if (dfsValidPath(
                    piece = piece, currentLocus = move, board = board, visitedSquares = visitedSquares + move,
                    remainingMoves = remainingMoves - 1
                )) {
                return true
            }
        }

        return false
    }

    private fun randomOpposingPiece(): Piece {
        // Use all major pieces for variety
        val pieces = listOf(Piece.Rook, Piece.Bishop, Piece.Knight, Piece.Queen)
        return pieces[randomFactory.nextInt(0, pieces.size)]
    }
}
