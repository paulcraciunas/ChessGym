package com.paulcraciunas.screens.achievements.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.paulcraciunas.screens.achievements.vm.AchievementsUiState.AchievementState
import com.paulcraciunas.screens.common.achievements.displayName
import com.paulcraciunas.screens.common.achievements.iconRes
import com.paulcraciunas.screens.common.achievements.outlineRes
import com.paulcraciunas.screens.common.achievements.tierColor
import com.paulcraciunas.screens.common.achievements.tierName
import com.paulcraciunas.screens.common.design.components.AchievementMedallion2
import com.paulcraciunas.screens.common.design.components.TrophyShelfTile2
import com.paulcraciunas.screens.common.design.theme.Design

@Composable
internal fun AchievementTile(
    item: AchievementState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = item.achievement.tierName(item.displayTier())
    val description = item.achievement.displayName()
    val hue = item.currentTier.tierColor()
    val iconPainter = painterResource(item.achievement.iconRes)
    val outlinePainter = item.currentTier?.let { painterResource(it.outlineRes) }

    TrophyShelfTile2(
        title = title,
        description = description,
        hue = hue,
        valueNow = item.currentProgress.toInt(),
        valueMax = if (item is AchievementState.Incomplete) item.nextThreshold.toInt() else item.currentProgress.toInt(),
        earned = item.isCompleted(),
        onClick = onClick,
        modifier = modifier,
        medallion = {
            AchievementMedallion2(
                iconPainter = iconPainter,
                iconTint = Design.colors.inkMuted,
                outlinePainter = outlinePainter,
                outlineTint = Design.colors.achievementTierBronze,
            )
        },
    )
}
