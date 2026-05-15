package com.paulcraciunas.screens.puzzles.rush.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.RefreshButton
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
internal fun ReadyControls(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Design.dimensions.spacing.xxl, vertical = Design.dimensions.spacing.lg),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.puzzle_rush_play_to_start),
            style = Design.typography.titleMedium,
            color = Design.colors.inkSoft,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun FinishedRushControls(
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Design.dimensions.spacing.xxl, vertical = Design.dimensions.spacing.sm),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RefreshButton(onClick = onPlayAgain, modifier = modifier)
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ReadyControlsPreview() {
    ChessGymTheme {
        ReadyControls()
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun FinishedRushControlsPreview() {
    ChessGymTheme {
        FinishedRushControls(onPlayAgain = {})
    }
}
