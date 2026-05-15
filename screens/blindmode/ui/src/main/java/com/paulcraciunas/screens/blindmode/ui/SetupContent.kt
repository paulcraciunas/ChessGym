package com.paulcraciunas.screens.blindmode.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.blindmode.vm.BlindModeScreenInteractor
import com.paulcraciunas.screens.blindmode.vm.BlindModeUiState
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard
import com.paulcraciunas.screens.common.controls.SideSelectionControls
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.PlayButton
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.components.ToggleRow
import com.paulcraciunas.screens.common.previews.SampleBoardViewData

@Composable
internal fun SetupContent(
    state: BlindModeUiState.Setup,
    showBorders: Boolean,
    interactions: BlindModeScreenInteractor,
) {
    ChessBoard(
        board = SampleBoardViewData.emptyBoardComposable(),
        orientation = BoardOrientation.White,
        onClick = {},
        showBorders = showBorders,
        enableAnimations = false,
        modifier = Modifier.fillMaxWidth()
    )
    ChessGymSpacer(size = SpacerSize.XXLARGE)
    SideSelectionControls(
        selectedSide = state.selectedSide,
        onSideSelected = interactions::onSideSelected,
    )
    ChessGymSpacer(size = SpacerSize.XXLARGE)
    ToggleRow(
        title = stringResource(R.string.blind_mode_training_mode),
        on = state.isTrainingMode,
        onChange = interactions::onTrainingModeToggled,
        subtitle = if (state.isTrainingMode) stringResource(R.string.blind_mode_training_description) else null,
        last = true,
    )
    ChessGymSpacer(size = SpacerSize.XXLARGE)
    PlayButton(onClick = interactions::onPlayClicked)
}
