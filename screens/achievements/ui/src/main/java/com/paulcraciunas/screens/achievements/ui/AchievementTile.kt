package com.paulcraciunas.screens.achievements.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.achievements.vm.AchievementsUiState.AchievementState
import com.paulcraciunas.screens.common.achievements.displayName
import com.paulcraciunas.screens.common.achievements.tierColor
import com.paulcraciunas.screens.common.achievements.tierName
import com.paulcraciunas.screens.common.design.components.LinearProgress
import com.paulcraciunas.screens.common.design.components.TieredAchievement
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
internal fun AchievementTile(
    state: AchievementState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val valueNow = state.currentProgress.toInt()
    val valueMax = if (state is AchievementState.Incomplete) state.nextThreshold.toInt() else state.currentProgress.toInt()
    val title = state.achievement.tierName(state.displayTier())
    val description = state.achievement.displayName()
    val hue = state.currentTier.tierColor()
    val progressFraction = (valueNow.toFloat() / valueMax.coerceAtLeast(1)).coerceIn(0f, 1f)
    val earnedLabel = stringResource(R.string.achievement_earned_label)
    val progressText = remember(state.isCompleted(), valueNow, valueMax, earnedLabel) {
        if (state.isCompleted()) earnedLabel else "$valueNow / $valueMax"
    }
    Column(
        modifier = modifier
            .shadow(Design.dimensions.elevation.sm, Design.shapes.card)
            .background(Design.colors.bg, Design.shapes.card)
            .clickable { onClick() }
            .border(Design.colors.softBorderStroke),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xs),
    ) {
        TieredAchievement(
            achievement = state.achievement,
            currentTier = state.currentTier,
            modifier = Modifier
                .padding(top = Design.dimensions.spacing.lg)
                .size(Design.dimensions.sizes.achievementIcon),
        )
        Text(
            text = title,
            color = Design.colors.ink,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text = description,
            color = Design.colors.inkMuted,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
        )
        LinearProgress(
            progress = progressFraction,
            color = hue,
            modifier = Modifier.padding(horizontal = Design.dimensions.spacing.lg)
                .fillMaxWidth(),
        )
        Text(
            text = progressText,
            color = hue,
            style = Design.textStyles.monoSmall,
            modifier = Modifier.padding(bottom = Design.dimensions.spacing.lg),
        )
    }
}

@Preview(name = "AchievementTile")
@Preview(name = "AchievementTile (Dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun AchievementTileCompletePreview() {
    ChessGymTheme {
        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.lg),
                modifier = Modifier
                    .background(Design.colors.bg)
                    .padding(Design.dimensions.spacing.md),
            ) {
                AchievementTile(
                    state = AchievementState.Complete(
                        achievement = Achievement.RATED_PUZZLES_SOLVED,
                        unseen = true,
                    ),
                    onClick = {},
                    modifier = Modifier.weight(1f),
                )
                AchievementTile(
                    state = AchievementState.Complete(
                        achievement = Achievement.RATED_WIN_STREAK,
                        unseen = true,
                    ),
                    onClick = {},
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier
                    .background(Design.colors.bg)
                    .padding(Design.dimensions.spacing.md),
            ) {
                AchievementTile(
                    state = AchievementState.Complete(
                        achievement = Achievement.KNIGHT_PATH_SESSIONS,
                        unseen = false,
                    ),
                    onClick = {},
                    modifier = Modifier.weight(1f),
                )
                AchievementTile(
                    state = AchievementState.Earned(
                        achievement = Achievement.STREAK_LEGEND,
                        unseen = false,
                        currentTier = Achievement.Tier.FOUR,
                        currentProgress = 75,
                        nextThreshold = 100,
                    ),
                    onClick = {},
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Preview(name = "AchievementTile")
@Preview(name = "AchievementTile (Dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun AchievementTileEarnedPreview() {
    ChessGymTheme {
        Row(
            modifier = Modifier
                .background(Design.colors.bg)
                .padding(Design.dimensions.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.md)
        ) {
            AchievementTile(
                state = AchievementState.Earned(
                    achievement = Achievement.DAILY_GRINDER,
                    unseen = false,
                    currentTier = Achievement.Tier.TWO,
                    currentProgress = 12,
                    nextThreshold = 21,
                ),
                onClick = {},
                modifier = Modifier.weight(1f),
            )
            AchievementTile(
                state = AchievementState.Earned(
                    achievement = Achievement.DAILY_GRINDER,
                    unseen = false,
                    currentTier = Achievement.Tier.FOUR,
                    currentProgress = 12,
                    nextThreshold = 21,
                ),
                onClick = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview(name = "AchievementTile")
@Preview(name = "AchievementTile (Dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun AchievementTileUnearnedPreview() {
    ChessGymTheme {
        Row(
            modifier = Modifier
                .background(Design.colors.bg)
                .padding(Design.dimensions.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.md)
        ) {
            AchievementTile(
                state = AchievementState.Unearned(
                    achievement = Achievement.STREAK_SESSIONS,
                    currentProgress = 1,
                    nextThreshold = 5,
                ),
                onClick = {},
                modifier = Modifier.weight(1f),
            )
            AchievementTile(
                state = AchievementState.Unearned(
                    achievement = Achievement.RATED_PUZZLES_SOLVED,
                    currentProgress = 2,
                    nextThreshold = 5,
                ),
                onClick = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}
