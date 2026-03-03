package com.paulcraciunas.screens.blindmode.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.blindmode.vm.BlindModeScreenInteractor
import com.paulcraciunas.screens.blindmode.vm.BlindModeUiState
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard
import com.paulcraciunas.screens.common.controls.PlayButton
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

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.blind_mode_training_mode),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = state.isTrainingMode,
            onCheckedChange = interactions::onTrainingModeToggled,
        )
    }

    if (!state.isTrainingMode) {
        Text(
            text = stringResource(R.string.blind_mode_training_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    PlayButton(
        onClick = interactions::onPlayClicked,
        text = R.string.blind_mode_play,
    )
}
