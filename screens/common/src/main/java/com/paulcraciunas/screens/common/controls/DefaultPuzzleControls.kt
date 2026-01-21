package com.paulcraciunas.screens.common.controls

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
fun DefaultPuzzleControls(
    hintEnabled: Boolean,
    toMove: Side,
    onHintRequested: () -> Unit,
    onAbandonRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Playing state: hint and abandon buttons + your move indicator
        OutlinedIconButton(onClick = onHintRequested, enabled = hintEnabled) {
            Icon(
                painter = painterResource(R.drawable.lightbulb_icon),
                contentDescription = stringResource(R.string.puzzle_hint),
                tint = if (!hintEnabled) {
                    MaterialTheme.colorScheme.outline
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(24.dp)
            )
        }
        OutlinedIconButton(onClick = onAbandonRequested) {
            Icon(
                painter = painterResource(R.drawable.flag_icon),
                contentDescription = stringResource(R.string.puzzle_abandon),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
        }
        YourMoveIndicator(toMove = toMove)
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
