package com.paulcraciunas.screens.achievements.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.domain.api.achievements.AchievementNotification
import com.paulcraciunas.domain.api.achievements.AchievementNotificationManager
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.achievements.tierName
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

    Box(modifier = modifier.fillMaxWidth()) {
        AnimatedContent(
            targetState = activeNotification,
            transitionSpec = {
                (slideInVertically { -it } togetherWith slideOutVertically { -it })
                    .using(SizeTransform(clip = false))
            },
            label = "AchievementBannerTransition"
        ) { notification ->
            if (notification != null) {
                AchievementBanner(notification = notification)
            } else {
                Spacer(modifier = Modifier)
            }
        }
    }
}

@Composable
private fun AchievementBanner(
    notification: AchievementNotification,
    modifier: Modifier = Modifier,
) {
    val name = notification.achievement.tierName(notification.tier)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.military_medal_icon),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(
                    R.string.achievement_banner_unlocked,
                    name,
                    notification.tier.number,
                ),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
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
