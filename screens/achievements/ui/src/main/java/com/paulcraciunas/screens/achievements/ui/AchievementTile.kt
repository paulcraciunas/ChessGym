package com.paulcraciunas.screens.achievements.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.achievements.vm.AchievementsUiState.AchievementState
import com.paulcraciunas.screens.common.achievements.displayName
import com.paulcraciunas.screens.common.achievements.tierColor
import com.paulcraciunas.screens.common.achievements.tierName
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
    TrophyShelfTile2(
        title = title,
        description = description,
        hue = hue,
        valueNow = item.currentProgress.toInt(),
        valueMax = if (item is AchievementState.Incomplete) item.nextThreshold.toInt() else item.currentProgress.toInt(),
        earned = item.isCompleted(),
        onClick = onClick,
        modifier = modifier,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.military_medal_icon),
                contentDescription = null,
                modifier = Modifier.size(Design.dimensions.spacing.section),
                tint = Design.colors.onPrimary,
            )
        },
    )
}
