package com.paulcraciunas.screens.achievements.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.achievements.vm.AchievementsUiState.AchievementState
import com.paulcraciunas.screens.common.achievements.description
import com.paulcraciunas.screens.common.achievements.displayName
import com.paulcraciunas.screens.common.achievements.tierName
import com.paulcraciunas.screens.common.extensions.GlobalAlphaTokens
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
internal fun AchievementCard(
    item: AchievementState,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    var isExpanded by remember { mutableStateOf(false) }

    val tierColor = item.currentTier.color()
    val containerColor = if (item.isCompleted()) {
        tierColor.copy(alpha = 0.08f).compositeOver(MaterialTheme.colorScheme.surface)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val onSurface = MaterialTheme.colorScheme.onSurface
    val mutedSurfaceColor = onSurface.copy(alpha = GlobalAlphaTokens.disabled)
    val primary = MaterialTheme.colorScheme.primary

    val iconTint = if (item.currentTier != null) primary else mutedSurfaceColor

    val progressText = when (item) {
        is AchievementState.Incomplete -> "${item.currentProgress} / ${item.nextThreshold}"
        is AchievementState.Complete -> null
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .then(if (item.unseen) Modifier.unseenGlow(shape, tierColor) else Modifier),
        shape = shape,
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .then(
                    if (!item.isCompleted()) {
                        Modifier.clickable { isExpanded = !isExpanded }
                    } else {
                        Modifier
                    }
                )
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(tierColor.copy(alpha = GlobalAlphaTokens.disabled)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.military_medal_icon),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = iconTint
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    AchievementHeader(
                        title = item.achievement.tierName(item.displayTier()),
                        titleColor = if (item.hasProgress()) onSurface else onSurface.copy(alpha = 0.4f),
                        progressText = progressText,
                        tierColor = tierColor,
                        isCompleted = item.isCompleted(),
                    )

                    Text(
                        text = item.achievement.displayName(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (item.hasProgress()) primary else mutedSurfaceColor,
                        fontWeight = FontWeight.Medium,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AchievementProgressBar(
                        progress = item.progress(),
                        mainColor = tierColor,
                        isCompleted = item.isCompleted(),
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded && !item.isCompleted(),
                enter = expandVertically(tween(300)) + fadeIn(tween(150, delayMillis = 150)),
                exit = shrinkVertically(tween(200)) + fadeOut(tween(150)),
            ) {
                Text(
                    text = item.achievement.description(),
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurface.copy(alpha = GlobalAlphaTokens.mediumEmphasis),
                    modifier = Modifier.padding(top = 12.dp, start = 68.dp),
                )
            }
        }
    }
}

@Composable
private fun AchievementHeader(
    title: String,
    titleColor: Color,
    progressText: String?,
    tierColor: Color,
    isCompleted: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = titleColor,
        )
        if (progressText != null) {
            Text(
                text = progressText,
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.5.sp),
                color = if (isCompleted) tierColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun AchievementProgressBar(
    progress: Float,
    mainColor: Color,
    isCompleted: Boolean,
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        shown = true
    }
    val targetProgress = when {
        !shown -> 0f
        isCompleted -> 1f
        else -> progress.coerceIn(0f, 1f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "AchievementProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape)
            .background(trackColor)
    ) {
        val progressBrush = if (isCompleted) {
            Brush.linearGradient(listOf(mainColor, mainColor))
        } else {
            val gradientStart = lerp(trackColor, mainColor, 0.3f)
            Brush.horizontalGradient(listOf(mainColor, gradientStart))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isCompleted) 1f else animatedProgress.coerceAtLeast(0.02f))
                .fillMaxHeight()
                .background(progressBrush)
        )
    }
}

@Composable
private fun Modifier.unseenGlow(shape: Shape, color: Color): Modifier {
    val transition = rememberInfiniteTransition(label = "glow")
    val animatedAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    return this.border(
        width = 2.dp,
        color = color.copy(alpha = animatedAlpha),
        shape = shape
    )
}

private val ColorBronze = Color(0xFFCD7F32)
private val ColorSilver = Color(0xFFB0C4DE)
private val ColorGold = Color(0xFFFFD700)
private val ColorEmerald = Color(0xFF2ECC71)
private val ColorDiamond = Color(0xFFB9F2FF)

@Composable
private fun Achievement.Tier?.color() = when (this) {
    Achievement.Tier.ONE -> ColorBronze
    Achievement.Tier.TWO -> ColorSilver
    Achievement.Tier.THREE -> ColorGold
    Achievement.Tier.FOUR -> ColorEmerald
    Achievement.Tier.FIVE -> ColorDiamond
    null -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
}

@Preview
@Composable
private fun AchievementCardPreview() {
    ChessGymTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AchievementCard(
                    item = AchievementState.Earned(
                        achievement = Achievement.RATED_PUZZLES_SOLVED,
                        unseen = true,
                        currentTier = Achievement.Tier.TWO,
                        currentProgress = 50,
                        nextThreshold = 100,
                    )
                )

                AchievementCard(
                    item = AchievementState.Complete(
                        achievement = Achievement.STREAK_SESSIONS,
                        unseen = false,
                    )
                )

                AchievementCard(
                    item = AchievementState.Unearned(
                        achievement = Achievement.FIND_SQUARE_SESSIONS,
                        currentProgress = 10,
                        nextThreshold = 25,
                    )
                )

                AchievementCard(
                    item = AchievementState.Unearned(
                        achievement = Achievement.BLIND_MODE_WINS,
                        currentProgress = 0,
                        nextThreshold = 1,
                    )
                )
            }
        }
    }
}
