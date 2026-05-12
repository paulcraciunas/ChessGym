package com.paulcraciunas.screens.common.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.paulcraciunas.screens.common.design.theme.Design

/**
 * The circular metallic medallion used for achievements. A radial gradient on
 * the category [hue] gives it depth; an inner stroke catches a highlight.
 *
 * Provide [icon] as a small composable (Icon, Image, or your custom vector).
 */
@Composable
fun AchievementMedallion(
    hue: Color,
    modifier: Modifier = Modifier,
    size: Dp = Design.dimensions.sizes.medallionLg,
    earned: Boolean = false,
    locked: Boolean = false,
    icon: @Composable (() -> Unit),
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(hue.copy(alpha = if (earned) 1f else 0.7f), hue, Design.colors.medallionDepth),
                    ),
                    shape = Design.shapes.circle,
                )
                .border(
                    width = if (earned) 2.dp else 1.dp,
                    color = if (earned) Design.colors.accent else Design.colors.border,
                    shape = Design.shapes.circle,
                )
        )
        Box(
            Modifier
                .size(size * 0.7f)
                .border(1.dp, Color.White.copy(alpha = if (locked) 0.06f else 0.18f), Design.shapes.circle)
        )
        Box(contentAlignment = Alignment.Center) { icon() }
        if (earned) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(Design.dimensions.spacing.gut)
                    .background(Design.colors.accent, CircleShape)
                    .surfaceBorder(),
                contentAlignment = Alignment.Center,
            ) {
                Text("✓", color = Design.colors.onPrimary, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

/**
 * Vertical "trophy shelf" tile used in horizontally-scrolling category rows.
 * Width is fixed at 138dp by default to match the design exploration's snap.
 */
@Composable
fun TrophyShelfTile(
    title: String,
    description: String,
    hue: Color,
    valueNow: Int,
    valueMax: Int,
    modifier: Modifier = Modifier,
    earned: Boolean = false,
    locked: Boolean = false,
    icon: @Composable (() -> Unit),
) {
    Surface(
        modifier = modifier.width(138.dp),
        shape = RoundedCornerShape(Design.radii.lg),
        color = Design.colors.surface,
        border = borderSoft(),
        shadowElevation = Design.dimensions.elevation.sm,
        tonalElevation = Design.dimensions.elevation.sm,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = Design.dimensions.spacing.lg, vertical = Design.dimensions.spacing.xl)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AchievementMedallion(
                hue = hue,
                size = Design.dimensions.sizes.medallionLg,
                earned = earned,
                locked = locked,
                icon = icon,
            )
            ChessGymSpacer(size = SpacerSize.MEDIUM)
            Text(
                text = title,
                color = Design.colors.ink,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall,
            )
            ChessGymSpacer(size = SpacerSize.SMALL)
            Text(
                text = description,
                color = Design.colors.inkMuted,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.height(Design.dimensions.sizes.medallionText),
            )
            ChessGymSpacer(size = SpacerSize.MEDIUM)
            LinearProgress(
                progress = (valueNow.toFloat() / valueMax.coerceAtLeast(1)),
                color = if (earned) Design.colors.accent else hue,
                height = Design.dimensions.sizes.progressBar,
            )
            ChessGymSpacer(size = SpacerSize.SMALL)
            Text(
                text = if (earned) "EARNED" else "$valueNow / $valueMax",
                color = if (earned) Design.colors.accent else Design.colors.inkMuted,
                style = Design.textStyles.monoSmall,
            )
        }
    }
}
