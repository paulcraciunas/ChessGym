package com.paulcraciunas.screens.boardvis.squares.vm

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.screens.boardvis.squares.vm.SideSelection.BLACK
import com.paulcraciunas.screens.boardvis.squares.vm.SideSelection.RANDOM
import com.paulcraciunas.screens.boardvis.squares.vm.SideSelection.WHITE
import com.paulcraciunas.screens.common.model.BoardViewData
import com.paulcraciunas.screens.common.previews.SampleBoardViewData

enum class SideSelection {
    WHITE,
    BLACK,
    RANDOM;
}

sealed class FindTheSquareUiState {
    val boardData: BoardViewData = SampleBoardViewData.startingBoard()
    abstract val orientation: Side

    data class Setup(
        val selectedSide: SideSelection = WHITE,
        val timeRemainingSeconds: Int = DEFAULT_DURATION_SECONDS,
        override val orientation: Side = selectedSide.toSetupSide(),
    ) : FindTheSquareUiState()

    data class Playing(
        override val orientation: Side,
        val currentSquare: Locus,
        val score: Int,
        val timeRemainingSeconds: Int,
        val showError: Boolean = false,
    ) : FindTheSquareUiState()

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

interface FindTheSquareScreenInteractor {
    fun onSideSelected(side: SideSelection)
    fun onPlayClicked()
    fun onSquareClicked(locus: Locus)
    fun onPlayAgain()
    fun onErrorShown()
}

private fun SideSelection.toSetupSide(): Side = when (this) {
    WHITE -> Side.WHITE
    BLACK -> Side.BLACK
    RANDOM -> Side.WHITE // during setup, we don't care about generating random sides
}