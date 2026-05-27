package com.paulcraciunas.screens.common.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

enum class ChessGymCardStyle { HIGHLIGHT, MUTED }

/**
 * The standard ChessGym card surface — soft cream/walnut background, hairline
 * border, sm shadow.
 */
@Composable
fun ChessGymCard(
    modifier: Modifier = Modifier,
    style: ChessGymCardStyle = ChessGymCardStyle.HIGHLIGHT,
    contentPadding: PaddingValues = PaddingValues(Design.dimensions.spacing.xxl),
    content: @Composable () -> Unit,
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
        Box(modifier = Modifier.padding(contentPadding)) { content() }
    }
}

/**
 * Full-bleed elevated card — for hero rows like the Home profile block.
 */
@Composable
fun ChessGymHeroCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(Design.dimensions.spacing.xxxl),
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = Design.shapes.card,
        color = Design.colors.bg,
        shadowElevation = Design.dimensions.elevation.md,
        tonalElevation = Design.dimensions.elevation.md,
        border = Design.colors.softBorderStroke,
    ) {
        Box(modifier = Modifier.padding(contentPadding)) { content() }
    }
}

/** Brass-tipped section header — uppercase title + hairline rule + optional trailing slot. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Design.dimensions.spacing.xl, bottom = Design.dimensions.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SectionHeaderTitle(text = title)
        HairlineDivider(
            modifier = Modifier
                .padding(horizontal = Design.dimensions.spacing.sm)
                .weight(1f)
        )
        if (trailing != null) {
            Box(Modifier.padding(start = Design.dimensions.spacing.sm)) { trailing() }
        }
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
    Box(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = Design.shapes.buttonOutline,
            color = Design.colors.surfaceAlt,
            border = Design.colors.softBorderStroke,
        ) {
            Row(
                modifier = Modifier.height(IntrinsicSize.Min)
                    .padding(
                        vertical = Design.dimensions.spacing.xl,
                        horizontal = Design.dimensions.spacing.md
                    ),
            ) {
                ChessGymSpacer(size = SpacerSize.SMALL)
                Spacer(modifier = Modifier.width(Design.dimensions.spacing.xs))
                if (style == FactBlockStyle.Info) {
                    IconBadge(
                        imageVector = Icons.Default.Info,
                        style = IconStyle.Circle,
                        borderType = IconBorderType.None,
                        tint = IconTintType.Accent,
                        size = IconSize.Small
                    )
                }
                Column(
                    modifier = Modifier.padding(
                        horizontal = Design.dimensions.spacing.lg,
                    )
                ) {
                    Eyebrow(text = title, type = when (style) {
                        FactBlockStyle.Info -> EyebrowType.Muted
                        FactBlockStyle.Danger -> EyebrowType.Danger
                    })
                    items.forEach { line ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = Design.dimensions.spacing.xs),
                        ) {
                            if (style != FactBlockStyle.Info) {
                                Box(
                                    modifier = Modifier
                                        .size(Design.dimensions.sizes.bulletPoint)
                                        .background(Design.colors.danger, Design.shapes.circle)
                                )
                                ChessGymSpacer()
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
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(Design.shapes.buttonOutline)
        ) {
            Box(
                modifier = Modifier
                    .width(Design.dimensions.spacing.xs)
                    .fillMaxHeight()
                    .background(when (style) {
                        FactBlockStyle.Info -> Design.colors.accent
                        FactBlockStyle.Danger -> Design.colors.danger
                    })
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChessGymCardPreview() {
    ChessGymTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ChessGymCard {
                Text(text = "This is a highlight card", color = Design.colors.ink)
            }
            Spacer(modifier = Modifier.size(16.dp))
            ChessGymCard(style = ChessGymCardStyle.MUTED) {
                Text(text = "This is a muted card", color = Design.colors.ink)
            }
            Spacer(modifier = Modifier.size(16.dp))
            ChessGymHeroCard {
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
            SectionHeader(
                title = "Analysis",
                trailing = { Text("View All", color = Design.colors.primary) }
            )
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
            modifier = Modifier.padding(16.dp),
            style = FactBlockStyle.Danger,
        )
    }
}
