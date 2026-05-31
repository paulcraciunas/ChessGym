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
import com.paulcraciunas.logic.builders.Builders
import javax.inject.Inject

/**
 * Implementation of [GenerateMoveThePieceBoard] that generates random but valid
 * board configurations for the Move the Piece game.
 *
 * Uses a path-first approach:
 * 1. Generate a random valid path for the player piece (retrying if the random
 *    walk reaches a dead end where no unvisited moves remain).
 * 2. Place opposing pieces that do not attack or block any square on the
 *    predetermined path.
 */
class GenerateMoveThePieceBoardImpl @Inject constructor(
    private val randomLocus: GenerateRandomLociImpl,
    private val randomFactory: RandomFactory,
) : GenerateMoveThePieceBoard {
    private val gameFactory: GameFactory = Builders.gameFactory()
    private val moveValidator: MoveValidator = Builders.moveValidator()

    override fun invoke(
        piece: Piece,
        requiredMoves: Int,
        opposingPieceCount: Int,
    ): MoveThePieceBoardData {
        repeat(MAX_PATH_ATTEMPTS) {
            val path = generateRandomPath(piece, requiredMoves) ?: return@repeat
            val opposingPieces = placeOpposingPieces(piece, path, opposingPieceCount)
            val board = buildBoard(piece, path.first(), opposingPieces)

            return MoveThePieceBoardData(playerPieceLocus = path.first(), board = board)
        }

        throw IllegalStateException(
            "Failed to generate a valid path for $piece with $requiredMoves moves after $MAX_PATH_ATTEMPTS attempts"
        )
    }

    /**
     * Generates a random path for the given [piece] starting from a random position.
     * Each subsequent square is a valid move from the previous one, and no square is revisited.
     *
     * @return the path, or null if the random walk reached a dead end
     */
    private fun generateRandomPath(piece: Piece, requiredMoves: Int): List<Locus>? {
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

            if (validMoves.isEmpty()) return null

            val nextLocus = validMoves[randomFactory.nextInt(0, validMoves.size)]

            board.move(currentLocus, nextLocus, Side.WHITE)
            path.add(nextLocus)
            currentLocus = nextLocus
        }

        return path
    }

    /**
     * Places opposing pieces randomly on the board such that none of them
     * attack or block any square in the predetermined [path].
     */
    private fun placeOpposingPieces(
        playerPiece: Piece,
        path: List<Locus>,
        count: Int,
    ): Map<Locus, Piece> {
        val placedPieces = mutableMapOf<Locus, Piece>()
        val occupiedSquares = path.toMutableSet()

        var placed = 0
        var attempts = 0

        while (placed < count && attempts < MAX_PLACEMENT_ATTEMPTS) {
            attempts++
            val candidateLocus = randomLocus()

            if (candidateLocus in occupiedSquares) continue

            val candidatePiece = randomOpposingPiece()
            val candidatePieces = placedPieces + (candidateLocus to candidatePiece)

            if (isPathTraversable(playerPiece, path, candidatePieces)) {
                placedPieces[candidateLocus] = candidatePiece
                occupiedSquares.add(candidateLocus)
                placed++
            }
        }

        return placedPieces
    }

    /**
     * Verifies that the white piece can traverse the entire [path] in sequence
     * on a board containing the given [opposingPieces], and that no path square
     * is under attack.
     */
    private fun isPathTraversable(
        playerPiece: Piece,
        path: List<Locus>,
        opposingPieces: Map<Locus, Piece>,
    ): Boolean {
        val tempBoard = buildBoard(playerPiece, path.first(), opposingPieces)
        val visited = mutableSetOf(path.first())

        for (i in 1 until path.size) {
            val from = path[i - 1]
            val to = path[i]

            val validMoves = moveValidator.getValidMoves(
                piece = playerPiece,
                side = Side.WHITE,
                from = from,
                board = tempBoard,
                blockers = visited + opposingPieces.keys
            )

            if (to !in validMoves) return false

            // Move the piece first so the old position no longer blocks attack lines
            tempBoard.move(from, to, Side.WHITE)

            if (moveValidator.isSquareUnderAttack(to, Side.BLACK, tempBoard)) return false

            visited.add(to)
        }

        return true
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
        private const val MAX_PATH_ATTEMPTS = 20
        private const val MAX_PLACEMENT_ATTEMPTS = 200
        private val OPPOSING_PIECES = listOf(Piece.Rook, Piece.Bishop, Piece.Knight, Piece.Queen)
    }
}
