package com.paulcraciunas.screens.boardvis.squares.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.screens.data.SideSelection
import com.paulcraciunas.screens.data.BoardViewData
import com.paulcraciunas.screens.data.RemainingTime
import com.paulcraciunas.screens.data.toSide

@Immutable
sealed class FindTheSquareUiState {
    val boardData: BoardViewData = BoardViewData.default()
    abstract val orientation: Side
    abstract val timeRemaining: RemainingTime

    @Immutable
    data class Setup(
        val selectedSide: SideSelection = SideSelection.WHITE,
        override val orientation: Side = selectedSide.toSide(),
    ) : FindTheSquareUiState() {
        override val timeRemaining: RemainingTime = RemainingTime(value = "${DEFAULT_DURATION_SECONDS}.0", danger = false)
    }

    @Immutable
    data class Playing(
        override val orientation: Side,
        override val timeRemaining: RemainingTime,
        val currentSquare: Locus,
        val score: Int,
        val showError: Boolean = false,
    ) : FindTheSquareUiState()

    @Immutable
    data class GameOver(
        override val orientation: Side,
        val score: Int,
        val isNewHighScore: Boolean,
        val previousHighScore: Int,
    ) : FindTheSquareUiState() {
        override val timeRemaining: RemainingTime = RemainingTime(value = "0.0", danger = true)
    }

    companion object {
        const val DEFAULT_DURATION_SECONDS = 30
        const val DANGER_DURATION_SECONDS = 5
    }
}
