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
 * Uses a path-first approach:
 * 1. Generate a random valid path for the player piece.
 * 2. Place opposing pieces that do not attack any square on the predetermined path.
 *
 * This guarantees a valid path always exists without retries.
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
        val path = generateRandomPath(piece, requiredMoves)
        val opposingPieces = placeOpposingPieces(path, opposingPieceCount)
        val board = buildBoard(piece, path.first(), opposingPieces)

        return MoveThePieceBoardData(playerPieceLocus = path.first(), board = board)
    }

    /**
     * Generates a random path for the given [piece] starting from a random position.
     * Each subsequent square is a valid move from the previous one, and no square is revisited.
     */
    private fun generateRandomPath(piece: Piece, requiredMoves: Int): List<Locus> {
        val path = mutableListOf<Locus>()
        val board = gameFactory.builder().buildBoard()

        var currentLocus = randomLocus()
        path.add(currentLocus)
        board.add(piece, Side.WHITE, currentLocus)

        repeat(requiredMoves) {
            val validMoves = moveValidator.getValidMoves(
                piece = piece,
                side = Side.WHITE,
                from = currentLocus,
                board = board,
                blockers = path.toSet()
            ).toList()

            val nextLocus = validMoves[randomFactory.nextInt(0, validMoves.size)]

            board.move(currentLocus, nextLocus, Side.WHITE)
            path.add(nextLocus)
            currentLocus = nextLocus
        }

        return path
    }

    /**
     * Places opposing pieces randomly on the board such that none of them
     * attack any square in the predetermined [path].
     */
    private fun placeOpposingPieces(
        path: List<Locus>,
        count: Int,
    ): Map<Locus, Piece> {
        val pathSquares = path.toSet()
        val placedPieces = mutableMapOf<Locus, Piece>()
        val occupiedSquares = pathSquares.toMutableSet()

        var placed = 0
        var attempts = 0

        while (placed < count && attempts < MAX_PLACEMENT_ATTEMPTS) {
            attempts++
            val candidateLocus = randomLocus()

            if (candidateLocus in occupiedSquares) continue

            val candidatePiece = randomOpposingPiece()
            if (isOpposingPieceSafe(candidatePiece, candidateLocus, pathSquares, placedPieces)) {
                placedPieces[candidateLocus] = candidatePiece
                occupiedSquares.add(candidateLocus)
                placed++
            }
        }

        return placedPieces
    }

    /**
     * Checks whether placing [piece] at [locus] would attack any square in the [pathSquares].
     * Uses a temporary board with already placed opposing pieces for accurate validation.
     */
    private fun isOpposingPieceSafe(
        piece: Piece,
        locus: Locus,
        pathSquares: Set<Locus>,
        existingPieces: Map<Locus, Piece>,
    ): Boolean {
        val tempBoard = gameFactory.builder().buildBoard()

        existingPieces.forEach { (loc, p) -> tempBoard.add(p, Side.BLACK, loc) }
        tempBoard.add(piece, Side.BLACK, locus)

        return pathSquares.none { square ->
            moveValidator.canAttack(
                piece = piece,
                side = Side.BLACK,
                from = locus,
                to = square,
                board = tempBoard
            )
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

    private fun randomOpposingPiece(): Piece {
        val pieces = OPPOSING_PIECES
        return pieces[randomFactory.nextInt(0, pieces.size)]
    }

    companion object {
        private const val MAX_PLACEMENT_ATTEMPTS = 200
        private val OPPOSING_PIECES = listOf(Piece.Rook, Piece.Bishop, Piece.Knight, Piece.Queen)
    }
}
