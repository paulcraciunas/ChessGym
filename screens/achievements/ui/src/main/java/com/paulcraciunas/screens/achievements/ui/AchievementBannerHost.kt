package com.paulcraciunas.screens.achievements.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.domain.api.achievements.AchievementNotification
import com.paulcraciunas.domain.api.achievements.AchievementNotificationManager
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.achievements.tierName
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import kotlinx.coroutines.delay

private const val BANNER_DISPLAY_MILLIS = 3000L

@Composable
fun AchievementBannerHost(
    notificationManager: AchievementNotificationManager,
    modifier: Modifier = Modifier,
) {
    var activeNotification by remember { mutableStateOf<AchievementNotification?>(null) }

    LaunchedEffect(notificationManager) {
        notificationManager.notifications.collect { notification ->
            activeNotification = notification
            delay(BANNER_DISPLAY_MILLIS)
            activeNotification = null
            delay(500)
        }
    }

    var lastNotification by remember { mutableStateOf<AchievementNotification?>(null) }
    if (activeNotification != null) lastNotification = activeNotification

    Box(modifier = modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = activeNotification != null,
            enter = slideInVertically { -it },
            exit = slideOutVertically { -it } + fadeOut(),
        ) {
            lastNotification?.let { AchievementBanner(notification = it) }
        }
    }
}

@Composable
private fun AchievementBanner(
    notification: AchievementNotification,
    modifier: Modifier = Modifier,
) {
    val shape = Design.shapes.cardCompact
    val name = notification.achievement.tierName(notification.tier)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = Design.dimensions.spacing.xxl,
                vertical = Design.dimensions.spacing.sm,
            )
            .shadow(elevation = Design.dimensions.elevation.nav, shape = shape)
            .clip(shape)
            .background(Design.colors.accent)
            .padding(Design.dimensions.spacing.lg),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.lg),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.military_medal_icon),
                contentDescription = null,
                modifier = Modifier.size(Design.dimensions.spacing.section),
                tint = Design.colors.ink,
            )
            Text(
                text = stringResource(
                    R.string.achievement_banner_unlocked,
                    name,
                    notification.tier.number,
                ),
                style = Design.typography.bodyMedium,
                color = Design.colors.ink,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AchievementBannerPreview() {
    ChessGymTheme {
        AchievementBanner(
            notification = AchievementNotification(
                achievement = Achievement.RATED_PUZZLES_SOLVED,
                tier = Achievement.Tier.ONE
            )
        )
    }
}
