package com.paulcraciunas.screens.common.controls

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.theme.ChessGymTheme

enum class SideSelection {
    WHITE,
    BLACK,
    RANDOM;
}

@Composable
fun SideSelectionControls(
    selectedSide: SideSelection,
    onSideSelected: (SideSelection) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
    }
}

@Preview("FindTheSquareControls - White selected")
@Preview("FindTheSquareControls - White selected (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun FindTheSquareControlsWhitePreview() {
    ChessGymTheme {
        SideSelectionControls(
            selectedSide = SideSelection.WHITE,
            onSideSelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview("FindTheSquareControls - Random selected")
@Composable
private fun FindTheSquareControlsRandomPreview() {
    ChessGymTheme {
        SideSelectionControls(
            selectedSide = SideSelection.RANDOM,
            onSideSelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
