package com.paulcraciunas.screens.blindmode.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.blindmode.vm.BlindModeScreenInteractor
import com.paulcraciunas.screens.blindmode.vm.BlindModeUiState
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard
import com.paulcraciunas.screens.common.controls.PlayButton
import com.paulcraciunas.screens.common.controls.SideSelectionControls
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.SpacerSize
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.blind_mode_training_mode),
            style = Design.typography.titleMedium,
            color = Design.colors.ink,
        )
        ChessGymSpacer(size = SpacerSize.LARGE)
        Switch(
            checked = state.isTrainingMode,
            onCheckedChange = interactions::onTrainingModeToggled,
        )
    }

    if (state.isTrainingMode) {
        Text(
            text = stringResource(R.string.blind_mode_training_description),
            style = Design.typography.bodySmall,
            color = Design.colors.inkSoft,
        )
    }
    ChessGymSpacer(size = SpacerSize.XXLARGE)
    PlayButton(
        onClick = interactions::onPlayClicked,
        text = R.string.blind_mode_play,
    )
}
