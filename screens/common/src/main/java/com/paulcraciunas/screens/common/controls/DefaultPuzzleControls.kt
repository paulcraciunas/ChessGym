package com.paulcraciunas.screens.common.controls

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
fun DefaultPuzzleControls(
    hintEnabled: Boolean,
    toMove: Side,
    onHintRequested: () -> Unit,
    onAbandonRequested: () -> Unit,
    modifier: Modifier = Modifier,
    abandonEnabled: Boolean = true,
    @StringRes moveIndicatorTextRes: Int = 0,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Playing state: hint and abandon buttons + your move indicator
        OutlinedIconButton(
            onClick = onHintRequested,
            enabled = hintEnabled,
            modifier = Modifier.testTag { DefaultPuzzleControlsTags.HINT }
        ) {
            Icon(
                painter = painterResource(R.drawable.lightbulb_icon),
                contentDescription = stringResource(R.string.puzzle_hint),
                tint = if (!hintEnabled) {
                    Design.colors.border
                } else {
                    Design.colors.primary
                },
                modifier = Modifier.size(Design.dimensions.sizes.navBarIconHeight)
            )
        }
        OutlinedIconButton(
            onClick = onAbandonRequested,
            enabled = abandonEnabled,
            modifier = Modifier.testTag { DefaultPuzzleControlsTags.ABANDON }
        ) {
            Icon(
                painter = painterResource(R.drawable.flag_icon),
                contentDescription = stringResource(R.string.puzzle_abandon),
                tint = if (!abandonEnabled) {
                    Design.colors.border
                } else {
                    Design.colors.danger
                },
                modifier = Modifier.size(Design.dimensions.sizes.navBarIconHeight)
            )
        }
        YourMoveIndicator(toMove = toMove, textRes = moveIndicatorTextRes)
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PuzzleControlsPlayingPreview() {
    ChessGymTheme {
        DefaultPuzzleControls(
            hintEnabled = true,
            toMove = Side.WHITE,
            onHintRequested = {},
            onAbandonRequested = {},
        )
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PuzzleControlsHintUsedPreview() {
    ChessGymTheme {
        DefaultPuzzleControls(
            hintEnabled = false,
            toMove = Side.BLACK,
            onHintRequested = {},
            onAbandonRequested = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PuzzleControlsCustomTextPreview() {
    ChessGymTheme {
        DefaultPuzzleControls(
            hintEnabled = true,
            toMove = Side.WHITE,
            onHintRequested = {},
            onAbandonRequested = {},
            moveIndicatorTextRes = R.string.blind_mode_your_turn,
        )
    }
}
