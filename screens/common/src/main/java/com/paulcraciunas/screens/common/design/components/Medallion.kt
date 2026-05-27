package com.paulcraciunas.screens.common.design.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

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
    onClick: (() -> Unit)? = null,
    icon: @Composable (() -> Unit),
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier.width(Design.dimensions.sizes.trophyTile),
        shape = RoundedCornerShape(Design.radii.lg),
        color = Design.colors.surface,
        border = Design.colors.softBorderStroke,
        shadowElevation = Design.dimensions.elevation.sm,
        tonalElevation = Design.dimensions.elevation.sm,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = Design.dimensions.spacing.lg, vertical = Design.dimensions.spacing.xl)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xs),
        ) {
            AchievementMedallion(
                hue = hue,
                size = Design.dimensions.sizes.medallionLg,
                earned = earned,
                locked = locked,
                icon = icon,
            )
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
                progress = (valueNow.toFloat() / valueMax.coerceAtLeast(1)),
                color = if (earned) Design.colors.accent else hue,
            )
            Text(
                text = if (earned) stringResource(R.string.achievement_earned_label) else "$valueNow / $valueMax",
                color = if (earned) Design.colors.accent else Design.colors.inkMuted,
                style = Design.textStyles.monoSmall,
            )
        }
    }
}

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
        contentAlignment = Alignment.Center,
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

@Preview(name = "TrophyShelfTile")
@Preview(name = "TrophyShelfTile (Dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun TrophyShelfTilePreview() {
    ChessGymTheme {
        Row(
            modifier = Modifier
                .background(Design.colors.bg)
                .padding(Design.dimensions.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.md)
        ) {
            TrophyShelfTile(
                title = "Grandmaster",
                description = "Reach a rating of 2500",
                hue = Color(0xFFFFD700),
                valueNow = 2450,
                valueMax = 2500,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(Design.dimensions.sizes.icon)
                    )
                }
            )
            TrophyShelfTile(
                title = "Blindfold Master",
                description = "Win a blindfold game",
                hue = Color(0xFF2196F3),
                valueNow = 1,
                valueMax = 1,
                locked = true,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(Design.dimensions.sizes.icon)
                    )
                },
                earned = true,
            )
        }
    }
}
