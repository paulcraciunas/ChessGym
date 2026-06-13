package com.paulcraciunas.screens.blindmode.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.blindmode.vm.BlindModeUiState
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard
import com.paulcraciunas.screens.data.SideSelection
import com.paulcraciunas.screens.common.controls.SideSelectionControls
import com.paulcraciunas.screens.common.design.components.PlayButton
import com.paulcraciunas.screens.common.design.components.ToggleRow
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.data.BoardViewData

@Composable
internal fun SetupContent(
    state: BlindModeUiState.Setup,
    onTrainingModeToggled: (enabled: Boolean) -> Unit = {},
    onSideSelected: (side: SideSelection) -> Unit = {},
    onPlayClicked: () -> Unit = {},
) {
    ChessBoard(
        board = BoardViewData.empty(),
        orientation = BoardOrientation.White,
        onClick = {},
    )
    ToggleRow(
        title = stringResource(R.string.blind_mode_training_mode),
        on = state.isTrainingMode,
        onChange = onTrainingModeToggled,
        subtitle = if (state.isTrainingMode) stringResource(R.string.blind_mode_training_description)
        else stringResource(R.string.blind_mode_rated_description),
        last = true,
    )
    SideSelectionControls(
        selectedSide = state.selectedSide,
        onSideSelected = onSideSelected,
        modifier = Modifier.padding(bottom = Design.dimensions.spacing.xxl)
    )
    PlayButton(onClick = onPlayClicked)
}
