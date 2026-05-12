package com.paulcraciunas.screens.common.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paulcraciunas.screens.common.design.theme.Design

/**
 * The standard ChessGym card surface — soft cream/walnut background, hairline
 * border, sm shadow. Shape matches the Walnut design exploration.
 */
@Composable
fun ChessGymCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(Design.dimensions.spacing.xxl),
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = Design.shapes.card,
        color = Design.colors.bg,
        tonalElevation = Design.dimensions.elevation.sm,
        shadowElevation = Design.dimensions.elevation.sm,
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
            .padding(top = Design.dimensions.spacing.xxxl, bottom = Design.dimensions.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Eyebrow(
            text = title,
            color = Design.colors.primary
        )
        HairlineDivider(
            modifier = Modifier
                .padding(horizontal = Design.dimensions.spacing.sm)
                .fillMaxWidth()
        )
        if (trailing != null) {
            Box(Modifier.padding(start = Design.dimensions.spacing.sm)) { trailing() }
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
