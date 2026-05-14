package com.paulcraciunas.screens.common.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        contentColor = if (style == ChessGymCardStyle.HIGHLIGHT) Design.colors.bg else Design.colors.surfaceAlt,
        tonalElevation = Design.dimensions.elevation.sm,
        shadowElevation = if (style == ChessGymCardStyle.HIGHLIGHT) Design.dimensions.elevation.sm else Design.dimensions.elevation.none,
        border = borderSoft(),
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
        border = borderSoft(),
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

@Composable
fun DangerBlock(
    title: String,
    items: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Design.colors.surfaceAlt, Design.shapes.buttonOutline)
            .border(borderSoft(), Design.shapes.buttonOutline)
            // 3dp left border accent
            .padding(start = Design.dimensions.spacing.xs)
            .background(Design.colors.danger, Design.shapes.borderAccent)
            // body
            .padding(start = Design.dimensions.spacing.none)
            .background(color = Design.colors.surfaceAlt, shape = Design.shapes.borderAccent)
            .padding(horizontal = Design.dimensions.spacing.lg, vertical = Design.dimensions.spacing.md),
    ) {
        Eyebrow(text = title, type = EyebrowType.Danger)
        items.forEach { line ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = Design.dimensions.spacing.xs),
            ) {
                Box(
                    modifier = Modifier
                        .size(Design.dimensions.sizes.bulletPoint)
                        .background(Design.colors.danger, CircleShape)
                )
                Spacer(modifier = Modifier.width(Design.dimensions.spacing.sm))
                Text(
                    text = line,
                    color = Design.colors.inkSoft,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/** A small brass tick centred between two hairlines. */
@Composable
fun BrassRule(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        HairlineDivider(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .padding(horizontal = Design.dimensions.spacing.sm)
                .size(4.dp)
                .background(Design.colors.accent.copy(alpha = 0.6f), Design.shapes.soft)
        )
        HairlineDivider(modifier = Modifier.weight(1f))
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
private fun DangerBlockPreview() {
    ChessGymTheme {
        DangerBlock(
            title = "Danger Zone",
            items = listOf("Delete Account", "Clear History"),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BrassRulePreview() {
    ChessGymTheme {
        BrassRule(modifier = Modifier.padding(16.dp))
    }
}
