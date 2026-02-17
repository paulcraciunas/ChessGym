package com.paulcraciunas.screens.boardvis.pieces.vm

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.model.BoardViewData
import com.paulcraciunas.screens.common.model.PieceViewData
import com.paulcraciunas.screens.common.model.SquareViewData

sealed class MoveThePieceUiState {
    abstract val boardData: BoardViewData

    data class Setup(
        val isTrainingMode: Boolean = true,
        val selectedPiece: Piece = Piece.Rook,
        val timeRemainingSeconds: Int = DEFAULT_DURATION_SECONDS,
    ) : MoveThePieceUiState() {
        override val boardData: BoardViewData = createEmptyBoard()
    }

    data class Playing(
        override val boardData: BoardViewData,
        val playerPiece: Piece,
        val playerPieceLocus: Locus,
        val movesRemaining: Int,
        val currentScore: Int,
        val timeRemainingSeconds: Int,
        val visitedSquares: Set<Locus>,
        val isTrainingMode: Boolean,
    ) : MoveThePieceUiState()

    data class GameOver(
        override val boardData: BoardViewData,
        val finalScore: Int,
        val isNewHighScore: Boolean,
        val wasCaptured: Boolean,
    ) : MoveThePieceUiState()

    companion object {
        const val DEFAULT_DURATION_SECONDS = 60
        // Pieces available for selection in the game, in the order they cycle.
        val GAME_PIECES = listOf(Piece.Rook, Piece.Bishop, Piece.Knight, Piece.Queen)

        private fun createEmptyBoard(): BoardViewData {
            val squares = Array(8) { Array(8) { SquareViewData(piece = null) } }
            return BoardViewData(squares = squares)
        }
    }
}

/**
 * Interactor interface for the Move the Piece screen.
 * Defines all user interactions that the ViewModel should handle.
 */
interface MoveThePieceScreenInteractor {
    fun onTrainingModeToggled(enabled: Boolean)
    fun onPieceSelected(piece: Piece)
    fun onPlayClicked()
    fun onSquareClicked(locus: Locus)
    fun onPlayAgain()
}

/**
 * Data class representing a generated board for the Move the Piece game.
 */
data class MoveThePieceBoardState(
    val playerPieceLocus: Locus,
    val playerPiece: Piece,
    val opposingPieces: Map<Locus, Piece>,
    val visitedSquares: Set<Locus>,
) {
    fun toBoardViewData(): BoardViewData {
        val squares = Array(8) { rankIndex ->
            Array(8) { fileIndex ->
                SquareViewData(piece = null)
            }
        }

        // Place the player's piece (white)
        squares[playerPieceLocus.rank.dec()][playerPieceLocus.file.dec()] = SquareViewData(
            piece = PieceViewData(
                piece = playerPiece,
                side = Side.WHITE,
                isSelected = false
            )
        )

        // Place opposing pieces (black)
        opposingPieces.forEach { (locus, piece) ->
            squares[locus.rank.dec()][locus.file.dec()] = SquareViewData(
                piece = PieceViewData(
                    piece = piece,
                    side = Side.BLACK,
                    isSelected = false
                )
            )
        }

        // Mark visited squares (optional visual indicator could be added)
        return BoardViewData(squares = squares)
    }
}
