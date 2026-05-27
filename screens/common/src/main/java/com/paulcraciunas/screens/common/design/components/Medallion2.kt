package com.paulcraciunas.screens.common.design.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
    icon: @Composable (() -> Unit),
) {
    val progressFraction = remember(valueNow, valueMax) {
        (valueNow.toFloat() / valueMax.coerceAtLeast(1)).coerceIn(0f, 1f)
    }
    val progressColor = if (earned) Design.colors.accent else hue
    val earnedLabel = stringResource(R.string.achievement_earned_label)
    val progressText = remember(earned, valueNow, valueMax, earnedLabel) {
        if (earned) earnedLabel else "$valueNow / $valueMax"
    }
    val progressTextColor = if (earned) Design.colors.accent else Design.colors.inkMuted

    Column(
        modifier = modifier
            .shadow(Design.dimensions.elevation.sm, Design.shapes.card)
            .background(Design.colors.surface, Design.shapes.card)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .border(borderSoft())
            .padding(horizontal = Design.dimensions.spacing.lg, vertical = Design.dimensions.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xs),
    ) {
        AchievementMedallion2(
            size = Design.dimensions.sizes.medallionLg,
            earned = earned,
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
            progress = progressFraction,
            color = progressColor,
        )
        Text(
            text = progressText,
            color = progressTextColor,
            style = Design.textStyles.monoSmall,
        )
    }
}

@Composable
fun AchievementMedallion2(
    modifier: Modifier = Modifier,
    size: Dp = Design.dimensions.sizes.medallionLg,
    earned: Boolean = false,
    icon: @Composable (() -> Unit),
) {
    val bg = Design.colors.primaryDeep
    val outerBorderColor = if (earned) Design.colors.accent else Design.colors.border
    val outerBorderWidth = if (earned) 2.dp else 1.dp

    Box(
        modifier = modifier
            .size(size)
            .drawBehind {
                drawCircle(
                    color = bg,
                    style = Fill
                )
                val outerWidthPx = outerBorderWidth.toPx()
                drawCircle( // outer border
                    color = outerBorderColor,
                    radius = (size.toPx() - outerWidthPx) / 2f,
                    style = Stroke(width = outerWidthPx)
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        icon()
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
                icon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(Design.dimensions.sizes.icon)
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
                icon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(Design.dimensions.sizes.icon)
                    )
                },
                earned = true,
                modifier = Modifier.width(Design.dimensions.sizes.trophyTile),
            )
        }
    }
}
