package com.paulcraciunas.screens.common.design.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
fun TrophyShelfTile2(
    title: String,
    description: String,
    hue: Color,
    valueNow: Int,
    valueMax: Int,
    modifier: Modifier = Modifier,
    earned: Boolean = false,
    onClick: (() -> Unit)? = null,
    medallion: @Composable () -> Unit,
) {
    val progressFraction = remember(valueNow, valueMax) {
        (valueNow.toFloat() / valueMax.coerceAtLeast(1)).coerceIn(0f, 1f)
    }
    val earnedLabel = stringResource(R.string.achievement_earned_label)
    val progressText = remember(earned, valueNow, valueMax, earnedLabel) {
        if (earned) earnedLabel else "$valueNow / $valueMax"
    }
    Column(
        modifier = modifier
            .shadow(Design.dimensions.elevation.sm, Design.shapes.card)
            .background(Design.colors.surface, Design.shapes.card)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .border(Design.colors.softBorderStroke)
            .padding(horizontal = Design.dimensions.spacing.lg, vertical = Design.dimensions.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xs),
    ) {
        medallion()
        Text(
            text = title,
            color = Design.colors.ink,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
            minLines = 2,
        )
        Text(
            text = description,
            color = Design.colors.inkMuted,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.height(Design.dimensions.sizes.medallionText),
        )
        LinearProgress(
            progress = progressFraction,
            color = hue,
        )
        Text(
            text = progressText,
            color = hue,
            style = Design.textStyles.monoSmall,
        )
    }
}

/**
 * Draws an achievement icon with an optional tier outline behind it.
 * Uses a single [Canvas] — no nested layout nodes — for efficient rendering
 * in lists/grids.
 *
 * When [outlinePainter] is `null` (unearned), only the icon is drawn at
 * [ICON_FRACTION_STANDALONE] of the container.
 * When [outlinePainter] is provided (earned), the outline fills the container
 * and the icon is drawn at [ICON_FRACTION_WITH_OUTLINE].
 */
@Composable
fun AchievementMedallion2(
    iconPainter: Painter,
    modifier: Modifier = Modifier,
    size: Dp = Design.dimensions.sizes.medallionLg,
    outlinePainter: Painter? = null,
    outlineTint: Color = Design.colors.primarySoft,
    iconTint: Color = Color.Unspecified,
) {
    val iconFraction = if (outlinePainter != null) ICON_FRACTION_WITH_OUTLINE else ICON_FRACTION_STANDALONE

    Canvas(modifier = modifier.size(size)) {
        outlinePainter?.let { drawPainterScaled(it, outlineTint) }
        drawPainterScaled(iconPainter, iconTint, scaleFraction = iconFraction)
    }
}

private fun DrawScope.drawPainterScaled(
    painter: Painter,
    tint: Color = Color.Unspecified,
    scaleFraction: Float = 1f,
) {
    val intrinsic = painter.intrinsicSize
    if (intrinsic == Size.Unspecified) return

    val targetWidth = size.width * scaleFraction
    val targetHeight = size.height * scaleFraction
    val scale = minOf(targetWidth / intrinsic.width, targetHeight / intrinsic.height)
    val drawSize = Size(intrinsic.width * scale, intrinsic.height * scale)
    val colorFilter = if (tint.isSpecified) ColorFilter.tint(tint) else null

    translate(
        left = (size.width - drawSize.width) / 2f,
        top = (size.height - drawSize.height) / 3f,
    ) {
        with(painter) {
            draw(size = drawSize, colorFilter = colorFilter)
        }
    }
}

private const val ICON_FRACTION_WITH_OUTLINE = 0.50f
private const val ICON_FRACTION_STANDALONE = 0.70f

@Preview(name = "TrophyShelfTile")
@Preview(name = "TrophyShelfTile (Dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun TrophyShelfTile2Preview() {
    ChessGymTheme {
        Row(
            modifier = Modifier
                .background(Design.colors.bg)
                .padding(Design.dimensions.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.md)
        ) {
            TrophyShelfTile2(
                title = "Grandmaster",
                description = "Reach a rating of 2500",
                hue = Color(0xFFFFD700),
                valueNow = 2450,
                valueMax = 2500,
                medallion = {
                    AchievementMedallion2(
                        iconPainter = painterResource(R.drawable.achievement_icon_03),
                        size = Design.dimensions.sizes.medallionLg,
                    )
                },
                modifier = Modifier.width(Design.dimensions.sizes.trophyTile),
            )
            TrophyShelfTile2(
                title = "Blindfold Master",
                description = "Win a blindfold game",
                hue = Color(0xFF2196F3),
                valueNow = 1,
                valueMax = 1,
                earned = true,
                medallion = {
                    AchievementMedallion2(
                        iconPainter = painterResource(R.drawable.achievement_icon_03),
                        outlinePainter = painterResource(R.drawable.achievement_outline_tier_3),
                        outlineTint = Color(0xFFFFD700),
                        size = Design.dimensions.sizes.medallionLg,
                    )
                },
                modifier = Modifier.width(Design.dimensions.sizes.trophyTile),
            )
        }
    }
}
