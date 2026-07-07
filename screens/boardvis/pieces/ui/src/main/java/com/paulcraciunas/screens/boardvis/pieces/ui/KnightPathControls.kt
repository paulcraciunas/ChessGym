package com.paulcraciunas.screens.boardvis.pieces.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.ChessGymChip
import com.paulcraciunas.screens.common.design.components.ChipStyle
import com.paulcraciunas.screens.common.design.components.ChipTone
import com.paulcraciunas.screens.common.design.components.Eyebrow
import com.paulcraciunas.screens.common.design.components.PlayButton
import com.paulcraciunas.screens.common.design.components.RefreshButton
import com.paulcraciunas.screens.common.design.components.Title
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
internal fun KnightPathSetupControls(
    onPlayClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xxl)
    ) {
        PlayButton(onClick = onPlayClicked)
        Text(
            text = stringResource(R.string.boardvis_knight_path_rules),
            style = Design.typography.bodyMedium,
            color = Design.colors.inkSoft,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Design.dimensions.spacing.xxl)
        )
    }
}

@Composable
internal fun KnightPathPlayingControls(
    currentScore: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Design.dimensions.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Eyebrow(text = stringResource(R.string.generic_current_score))
        Text(
            text = "$currentScore",
            style = Design.textStyles.displayNumericLarge,
            color = Design.colors.primary,
        )
    }
}

@Composable
internal fun KnightPathGameOverControls(
    finalScore: Int,
    isNewHighScore: Boolean,
    previousHighScore: Int,
    wasWrongMove: Boolean,
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.lg)
    ) {
        Text(
            text = if (wasWrongMove) {
                stringResource(R.string.boardvis_knight_path_wrong_move)
            } else {
                stringResource(R.string.boardvis_game_over)
            },
            style = Design.typography.displayMedium,
            color = Design.colors.ink,
        )
        Title(
            text = stringResource(R.string.boardvis_final_score, finalScore),
            color = Design.colors.primary,
        )
        if (isNewHighScore) {
            ChessGymChip(
                text = stringResource(R.string.generic_new_high_score),
                tone = ChipTone.Accent,
                style = ChipStyle.Default,
                leadingIcon = Icons.Default.Star,
            )
        } else {
            Eyebrow(text = stringResource(R.string.boardvis_high_score, previousHighScore))
        }
        RefreshButton(onClick = onPlayAgain)
    }
}

@Preview("KnightPathSetupControls", showBackground = true)
@Preview("KnightPathSetupControls (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun KnightPathSetupControlsPreview() {
    ChessGymTheme {
        KnightPathSetupControls(
            onPlayClicked = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview("KnightPathPlayingControls", showBackground = true)
@Composable
private fun KnightPathPlayingControlsPreview() {
    ChessGymTheme {
        KnightPathPlayingControls(
            currentScore = 8,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview("KnightPathGameOverControls - New High Score", showBackground = true)
@Composable
private fun KnightPathGameOverNewHighScorePreview() {
    ChessGymTheme {
        KnightPathGameOverControls(
            finalScore = 15,
            isNewHighScore = true,
            previousHighScore = 42,
            wasWrongMove = false,
            onPlayAgain = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview("KnightPathGameOverControls - Wrong Move", showBackground = true)
@Composable
private fun KnightPathGameOverWrongMovePreview() {
    ChessGymTheme {
        KnightPathGameOverControls(
            finalScore = 8,
            isNewHighScore = false,
            previousHighScore = 9,
            wasWrongMove = true,
            onPlayAgain = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
