package com.paulcraciunas.screens.common.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

enum class ChessGymCardStyle { HIGHLIGHT, MUTED }

/**
 * Shared layout-agnostic styling base.
 * Marked inline to prevent any lambda overhead.
 */
@Composable
private inline fun BaseCardChessGymCard(
    modifier: Modifier,
    style: ChessGymCardStyle,
    crossinline content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = Design.shapes.card,
        color = if (style == ChessGymCardStyle.HIGHLIGHT) Design.colors.bg else Design.colors.surfaceAlt,
        contentColor = Design.colors.ink,
        tonalElevation = Design.dimensions.elevation.sm,
        shadowElevation = if (style == ChessGymCardStyle.HIGHLIGHT) Design.dimensions.elevation.sm else Design.dimensions.elevation.none,
        border = Design.colors.softBorderStroke,
    ) {
        content()
    }
}

/**
 * The standard ChessGym card surface — soft cream/walnut background, hairline
 * border, sm shadow.
 */
@Composable
fun ChessGymColumnCard(
    modifier: Modifier = Modifier,
    style: ChessGymCardStyle = ChessGymCardStyle.HIGHLIGHT,
    contentPadding: PaddingValues = PaddingValues.Zero,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    BaseCardChessGymCard(modifier = modifier, style = style) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            content = content
        )
    }
}

/**
 * The standard ChessGym card surface — soft cream/walnut background, hairline
 * border, sm shadow.
 */
@Composable
fun ChessGymRowCard(
    modifier: Modifier = Modifier,
    style: ChessGymCardStyle = ChessGymCardStyle.HIGHLIGHT,
    contentPadding: PaddingValues = PaddingValues.Zero,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit,
) {
    BaseCardChessGymCard(modifier = modifier, style = style) {
        Row(
            modifier = Modifier.padding(contentPadding),
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment,
            content = content
        )
    }
}

/**
 * Full-bleed elevated card — for hero rows like the Home profile block.
 */
@Composable
fun ChessGymElevatedCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues.Zero,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = Design.shapes.card,
        color = Design.colors.bg,
        shadowElevation = Design.dimensions.elevation.md,
        tonalElevation = Design.dimensions.elevation.md,
        border = Design.colors.softBorderStroke,
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            content = content
        )
    }
}

/** Brass-tipped section header — uppercase title + hairline rule. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    val lineColor = Design.colors.divider
    val strokeWidthPx = with(LocalDensity.current) { 1.dp.toPx() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Design.dimensions.spacing.xl, bottom = Design.dimensions.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SectionHeaderTitle(text = title)
        Spacer(
            modifier = Modifier
                .padding(horizontal = Design.dimensions.spacing.sm)
                .weight(1f)
                .drawBehind {
                    drawLine(
                        color = lineColor,
                        start = Offset(0f, size.height / 2f),
                        end = Offset(size.width, size.height / 2f),
                        strokeWidth = strokeWidthPx
                    )
                }
        )
    }
}

enum class FactBlockStyle { Info, Danger }

@Composable
fun FactBlock(
    title: String,
    items: List<String>,
    modifier: Modifier = Modifier,
    style: FactBlockStyle = FactBlockStyle.Info,
) {
    val shape = Design.shapes.buttonOutline
    val stripeColor = when (style) {
        FactBlockStyle.Info -> Design.colors.accent
        FactBlockStyle.Danger -> Design.colors.danger
    }
    val offsetValue = Design.dimensions.elevation.md

    Surface(
        modifier = modifier
            .background(color = stripeColor, shape = shape)
            .offset { IntOffset(x = offsetValue.roundToPx(), y = 0) },
        shape = shape,
        color = Design.colors.surfaceAlt,
        border = Design.colors.softBorderStroke,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(
                    vertical = Design.dimensions.spacing.xl,
                ),
            verticalAlignment = Alignment.Top,
        ) {
            if (style == FactBlockStyle.Info) {
                IconBadge(
                    imageVector = Icons.Default.Info,
                    style = IconStyle.Circle,
                    borderType = IconBorderType.None,
                    tint = IconTintType.Accent,
                    size = IconSize.Small,
                    modifier = Modifier.padding(start = Design.dimensions.spacing.sm)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Design.dimensions.spacing.lg)
            ) {
                Eyebrow(
                    text = title,
                    type = when (style) {
                        FactBlockStyle.Info -> EyebrowType.Muted
                        FactBlockStyle.Danger -> EyebrowType.Danger
                    }
                )
                items.forEach { line ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = Design.dimensions.spacing.xs),
                    ) {
                        if (style != FactBlockStyle.Info) {
                            Box(
                                modifier = Modifier
                                    .padding(end = Design.dimensions.spacing.sm)
                                    .size(Design.dimensions.sizes.bulletPoint)
                                    .background(Design.colors.danger, Design.shapes.circle)
                            )
                        }
                        Text(
                            text = line,
                            color = Design.colors.inkSoft,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChessGymCardPreview() {
    ChessGymTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ChessGymRowCard {
                Text(text = "This is a highlight card", color = Design.colors.ink)
            }
            Spacer(modifier = Modifier.size(16.dp))
            ChessGymColumnCard(style = ChessGymCardStyle.MUTED) {
                Text(text = "This is a muted card", color = Design.colors.ink)
            }
            Spacer(modifier = Modifier.size(16.dp))
            ChessGymElevatedCard {
                Text(text = "This is a hero card", color = Design.colors.ink)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionHeaderPreview() {
    ChessGymTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(title = "Puzzles")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InfoBlockPreview() {
    ChessGymTheme {
        FactBlock(
            title = "Danger Zone",
            items = listOf("Delete Account", "Clear History"),
            modifier = Modifier.padding(16.dp),
            style = FactBlockStyle.Info,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DangerBlockPreview() {
    ChessGymTheme {
        FactBlock(
            title = "Danger Zone",
            items = listOf("Delete Account", "Clear History"),
            style = FactBlockStyle.Danger,
        )
    }
}
