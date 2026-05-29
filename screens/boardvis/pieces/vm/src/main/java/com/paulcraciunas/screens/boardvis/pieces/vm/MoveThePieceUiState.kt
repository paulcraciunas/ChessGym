package com.paulcraciunas.screens.boardvis.pieces.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.model.BoardViewData2

@Immutable
sealed class MoveThePieceUiState {
    abstract val boardData: BoardViewData2

    @Immutable
    data class Setup(
        val isTrainingMode: Boolean = true,
        val selectedPiece: Piece = Piece.Rook,
        val timeRemainingSeconds: Int = DEFAULT_DURATION_SECONDS,
    ) : MoveThePieceUiState() {
        override val boardData: BoardViewData2 = BoardViewData2.empty()
    }

    @Immutable
    data class Playing(
        override val boardData: BoardViewData2,
        val playerPiece: Piece,
        val playerPieceLocus: Locus,
        val movesRemaining: Int,
        val currentScore: Int,
        val timeRemainingSeconds: Int,
        val visitedSquares: Set<Locus>,
        val isTrainingMode: Boolean,
    ) : MoveThePieceUiState()

    @Immutable
    data class GameOver(
        override val boardData: BoardViewData2,
        val finalScore: Int,
        val isNewHighScore: Boolean,
        val wasCaptured: Boolean,
    ) : MoveThePieceUiState()

    companion object {
        const val DEFAULT_DURATION_SECONDS = 60

        // Pieces available for selection in the game, in the order they cycle.
        val GAME_PIECES = listOf(Piece.Rook, Piece.Bishop, Piece.Knight, Piece.Queen)
    }
}
