package com.paulcraciunas.screens.boardvis.squares.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.screens.common.controls.SideSelection
import com.paulcraciunas.screens.common.model.BoardViewData

@Immutable
sealed class FindTheSquareUiState {
    val boardData: BoardViewData = BoardViewData.default()
    abstract val orientation: Side

    @Immutable
    data class Setup(
        val selectedSide: SideSelection = SideSelection.WHITE,
        val timeRemainingSeconds: Int = DEFAULT_DURATION_SECONDS,
        override val orientation: Side = selectedSide.toSetupSide(),
    ) : FindTheSquareUiState()

    @Immutable
    data class Playing(
        override val orientation: Side,
        val currentSquare: Locus,
        val score: Int,
        val timeRemainingSeconds: Int,
        val showError: Boolean = false,
    ) : FindTheSquareUiState()

    @Immutable
    data class GameOver(
        override val orientation: Side,
        val score: Int,
        val isNewHighScore: Boolean,
        val previousHighScore: Int,
    ) : FindTheSquareUiState()

    companion object {
        const val DEFAULT_DURATION_SECONDS = 30
    }
}

private fun SideSelection.toSetupSide(): Side = when (this) {
    SideSelection.WHITE -> Side.WHITE
    SideSelection.BLACK -> Side.BLACK
    SideSelection.RANDOM -> Side.WHITE // during setup, we don't care about generating random sides
}