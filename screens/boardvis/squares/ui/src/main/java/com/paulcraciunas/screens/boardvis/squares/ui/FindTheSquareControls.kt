package com.paulcraciunas.screens.boardvis.squares.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.boardvis.squares.vm.SideSelection
import com.paulcraciunas.screens.common.controls.PlayButton
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.common.theme.GlobalTokens

@Composable
internal fun FindTheSquareControls(
    selectedSide: SideSelection,
    isPlaying: Boolean,
    onSideSelected: (SideSelection) -> Unit,
    onPlayClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Side selection buttons
        if (!isPlaying) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SideSelectionButton(
                    iconRes = R.drawable.king_white,
                    side = Side.WHITE,
                    contentDescription = stringResource(R.string.boardvis_play_as_white),
                    isSelected = selectedSide == SideSelection.WHITE,
                    onClick = { onSideSelected(SideSelection.WHITE) }
                )
                SideSelectionButton(
                    iconRes = R.drawable.king_black,
                    side = Side.BLACK,
                    contentDescription = stringResource(R.string.boardvis_play_as_black),
                    isSelected = selectedSide == SideSelection.BLACK,
                    onClick = { onSideSelected(SideSelection.BLACK) }
                )
                SideSelectionButton(
                    iconRes = R.drawable.side_select,
                    side = Side.WHITE,
                    contentDescription = stringResource(R.string.boardvis_play_random),
                    isSelected = selectedSide == SideSelection.RANDOM,
                    onClick = { onSideSelected(SideSelection.RANDOM) }
                )
            }

            PlayButton(onClick = onPlayClicked)
        }
    }
}

@Composable
private fun SideSelectionButton(
    @DrawableRes iconRes: Int,
    side: Side,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(if (side == Side.BLACK) Color.White else Color.Black)
            .border(
                width = 4.dp,
                color = borderColor,
                shape = CircleShape
            )
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = modifier
                .fillMaxSize(GlobalTokens.scaleFactorDefault)
                .aspectRatio(1f)
        )
    }
}

@Preview("FindTheSquareControls - White selected")
@Preview("FindTheSquareControls - White selected (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun FindTheSquareControlsWhitePreview() {
    ChessGymTheme {
        FindTheSquareControls(
            selectedSide = SideSelection.WHITE,
            isPlaying = false,
            onSideSelected = {},
            onPlayClicked = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview("FindTheSquareControls - Random selected")
@Composable
private fun FindTheSquareControlsRandomPreview() {
    ChessGymTheme {
        FindTheSquareControls(
            selectedSide = SideSelection.RANDOM,
            isPlaying = false,
            onSideSelected = {},
            onPlayClicked = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
