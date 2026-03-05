package com.paulcraciunas.screens.boardvis.squares.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.screens.common.controls.PlayButton
import com.paulcraciunas.screens.common.controls.SideSelection
import com.paulcraciunas.screens.common.controls.SideSelectionControls
import com.paulcraciunas.screens.common.theme.ChessGymTheme

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
        if (!isPlaying) {
            SideSelectionControls(
                selectedSide = selectedSide,
                onSideSelected = onSideSelected,
            )

            PlayButton(onClick = onPlayClicked)
        }
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
