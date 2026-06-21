package com.paulcraciunas.screens.common.design.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.screens.common.achievements.iconRes
import com.paulcraciunas.screens.common.achievements.outlineRes
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
fun TieredAchievement(
    achievement: Achievement,
    currentTier: Achievement.Tier?,
    modifier: Modifier = Modifier,
    tint: Color = Design.colors.primary,
) {
    val iconPainter = painterResource(achievement.iconRes)
    val outlinePainter = currentTier?.let { painterResource(it.outlineRes) }

    Canvas(modifier = modifier) {
        outlinePainter?.let {
            drawShield(
                painter = it,
                tier = currentTier,
                tint = tint,
            )
        }
        drawIcon(
            painter = iconPainter,
            tier = currentTier ?: Achievement.Tier.ONE,
            tint = tint,
        )
    }
}

private fun DrawScope.drawIcon(
    painter: Painter,
    tier: Achievement.Tier,
    tint: Color,
) {
    val scaleX = size.width / painter.intrinsicSize.width * iconScale
    val scaleY = size.width / painter.intrinsicSize.width * iconScale

    val offsetX = (size.width - (painter.intrinsicSize.width * scaleX)) / 2
    val offsetY = (size.height - (painter.intrinsicSize.height * scaleY)) / 2 + tier.iconOffsetY

    withTransform({
        translate(left = offsetX, top = offsetY)
        scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero)
    }) {
        with(painter) {
            draw(size = painter.intrinsicSize, colorFilter = ColorFilter.tint(tint))
        }
    }
}

private fun DrawScope.drawShield(
    painter: Painter,
    tier: Achievement.Tier,
    tint: Color,
) {
    val scaleX = size.width / painter.intrinsicSize.width * tier.shieldScaleX
    val scaleY = size.height / painter.intrinsicSize.height * tier.shieldScaleY

    val offsetX = (size.width - (painter.intrinsicSize.width * scaleX)) / 2
    val offsetY = size.height - (painter.intrinsicSize.height * scaleY)

    withTransform({
        translate(left = offsetX, top = offsetY)
        scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero)
    }) {
        with(painter) {
            draw(size = painter.intrinsicSize, colorFilter = ColorFilter.tint(tint))
        }
    }
}

private val Achievement.Tier.shieldScaleX: Float
    get() = if (this >= Achievement.Tier.FOUR) 1f else 0.8f

private val Achievement.Tier.shieldScaleY: Float
    get() = if (this == Achievement.Tier.FIVE) 1f else 0.9f

private const val iconScale: Float = 0.45f

private val Achievement.Tier.iconOffsetY: Float
    get() = if (this >= Achievement.Tier.FOUR) -10f else 0f

@Preview(name = "TieredAchievement")
@Composable
private fun TieredAchievementPreview() {
    ChessGymTheme {
        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.lg),
                modifier = Modifier
                    .background(Design.colors.bg)
                    .padding(Design.dimensions.spacing.md),
            ) {
                TieredAchievement(
                    achievement = Achievement.RATED_PUZZLES_SOLVED,
                    currentTier = Achievement.Tier.FIVE,
                    modifier = Modifier.size(Design.dimensions.sizes.achievementIcon),
                )
                TieredAchievement(
                    achievement = Achievement.RATED_WIN_STREAK,
                    currentTier = Achievement.Tier.FOUR,
                    modifier = Modifier.size(Design.dimensions.sizes.achievementIcon),
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.lg),
                modifier = Modifier
                    .background(Design.colors.bg)
                    .padding(Design.dimensions.spacing.md),
            ) {
                TieredAchievement(
                    achievement = Achievement.KNIGHT_PATH_SESSIONS,
                    currentTier = Achievement.Tier.FOUR,
                    modifier = Modifier.size(Design.dimensions.sizes.achievementIcon),
                )
                TieredAchievement(
                    achievement = Achievement.STREAK_LEGEND,
                    currentTier = Achievement.Tier.THREE,
                    modifier = Modifier.size(Design.dimensions.sizes.achievementIcon),
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.lg),
                modifier = Modifier
                    .background(Design.colors.bg)
                    .padding(Design.dimensions.spacing.md),
            ) {
                TieredAchievement(
                    achievement = Achievement.DAILY_GRINDER,
                    currentTier = Achievement.Tier.THREE,
                    modifier = Modifier.size(Design.dimensions.sizes.achievementIcon),
                )
                TieredAchievement(
                    achievement = Achievement.BOARD_VISION,
                    currentTier = Achievement.Tier.TWO,
                    modifier = Modifier.size(Design.dimensions.sizes.achievementIcon),
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.lg),
                modifier = Modifier
                    .background(Design.colors.bg)
                    .padding(Design.dimensions.spacing.md),
            ) {
                TieredAchievement(
                    achievement = Achievement.BLIND_STRATEGIST,
                    currentTier = Achievement.Tier.TWO,
                    modifier = Modifier.size(Design.dimensions.sizes.achievementIcon),
                )
                TieredAchievement(
                    achievement = Achievement.TIME_INVESTED,
                    currentTier = Achievement.Tier.ONE,
                    modifier = Modifier.size(Design.dimensions.sizes.achievementIcon),
                )
            }
        }
    }
}
