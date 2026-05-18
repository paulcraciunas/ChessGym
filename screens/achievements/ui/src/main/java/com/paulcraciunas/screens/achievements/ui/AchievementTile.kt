package com.paulcraciunas.screens.achievements.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.achievements.vm.AchievementsUiState.AchievementState
import com.paulcraciunas.screens.common.achievements.displayName
import com.paulcraciunas.screens.common.achievements.tierColor
import com.paulcraciunas.screens.common.achievements.tierName
import com.paulcraciunas.screens.common.design.components.TrophyShelfTile
import com.paulcraciunas.screens.common.design.theme.Design

@Composable
internal fun AchievementTile(
    item: AchievementState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TrophyShelfTile(
        title = item.achievement.tierName(item.displayTier()),
        description = item.achievement.displayName(),
        hue = item.currentTier.tierColor(),
        valueNow = item.currentProgress.toInt(),
        valueMax = if (item is AchievementState.Incomplete) item.nextThreshold.toInt() else item.currentProgress.toInt(),
        earned = item.isCompleted(),
        locked = !item.hasProgress(),
        onClick = onClick,
        modifier = modifier,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.military_medal_icon),
                contentDescription = null,
                modifier = Modifier.size(Design.dimensions.spacing.section),
                tint = if (item.currentTier != null) {
                    Design.colors.primary
                } else {
                    Design.colors.inkMuted
                },
            )
        },
    )
}
