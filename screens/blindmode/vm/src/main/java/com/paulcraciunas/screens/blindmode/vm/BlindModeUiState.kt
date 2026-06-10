package com.paulcraciunas.screens.blindmode.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.screens.data.BoardState
import com.paulcraciunas.screens.data.SideSelection

@Immutable
sealed class BlindModeUiState {
    abstract val isTrainingMode: Boolean

    @Immutable
    data class Setup(
        override val isTrainingMode: Boolean = true,
        val selectedSide: SideSelection = SideSelection.WHITE,
    ) : BlindModeUiState()

    @Immutable
    data class Playing(
        override val isTrainingMode: Boolean,
        val data: BoardState,
        val moveHistory: String = "",
        val isRevealAvailable: Boolean = true,
        val isAbandonDialogShown: Boolean = false,
        val isRevealing: Boolean = false,
        val isThinking: Boolean = false,
    ) : BlindModeUiState()

    @Immutable
    data class GameOver(
        override val isTrainingMode: Boolean,
        val data: BoardState,
        val moveHistory: String,
    ) : BlindModeUiState()
}
