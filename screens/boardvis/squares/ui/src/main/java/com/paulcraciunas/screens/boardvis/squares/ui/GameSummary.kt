package com.paulcraciunas.screens.boardvis.squares.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.ChessGymChip
import com.paulcraciunas.screens.common.design.components.ChipStyle
import com.paulcraciunas.screens.common.design.components.ChipTone
import com.paulcraciunas.screens.common.design.components.Eyebrow
import com.paulcraciunas.screens.common.design.components.RefreshButton
import com.paulcraciunas.screens.common.design.components.Title
import com.paulcraciunas.screens.common.design.components.borderSoft
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
internal fun GameSummary(
    score: Int,
    isNewHighScore: Boolean,
    previousHighScore: Int,
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(Design.shapes.card)
            .border(borderSoft(), Design.shapes.card)
            .background(Design.colors.surface)
            .padding(Design.dimensions.spacing.xgut)
            .testTag { FindTheSquareTags.GAME_SUMMARY },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.lg)
    ) {
        Text(
            text = stringResource(R.string.boardvis_game_over),
            style = Design.typography.headlineSmall,
            color = Design.colors.ink
        )
        Title(
            text = stringResource(R.string.boardvis_final_score, score),
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

        RefreshButton(
            onClick = onPlayAgain,
            modifier = Modifier.testTag { FindTheSquareTags.PLAY_AGAIN_BUTTON },
        )
    }
}

@Preview("GameSummary - New High Score")
@Preview("GameSummary - New High Score (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun GameSummaryNewHighScorePreview() {
    ChessGymTheme {
        GameSummary(
            score = 35,
            isNewHighScore = true,
            previousHighScore = 30,
            onPlayAgain = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview("GameSummary - No High Score")
@Composable
private fun GameSummaryNoHighScorePreview() {
    ChessGymTheme {
        GameSummary(
            score = 20,
            isNewHighScore = false,
            previousHighScore = 35,
            onPlayAgain = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
