package com.paulcraciunas.screens.blindmode.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.screens.common.controls.SideSelection
import com.paulcraciunas.screens.common.model.GameViewModelHelper

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
        val data: GameViewModelHelper.GameData2,
        val pendingPromotion: GameViewModelHelper.GamePromotion? = null,
        val moveHistory: String = "",
        val isRevealAvailable: Boolean = true,
        val isThinking: Boolean = false,
        val isAbandonDialogShown: Boolean = false,
        val isRevealing: Boolean = false,
    ) : BlindModeUiState()

    @Immutable
    data class GameOver(
        override val isTrainingMode: Boolean,
        val data: GameViewModelHelper.GameData2,
        val moveHistory: String,
        val result: GameResult,
    ) : BlindModeUiState()

    enum class GameResult {
        Win,
        Draw,
        Loss
    }
}
