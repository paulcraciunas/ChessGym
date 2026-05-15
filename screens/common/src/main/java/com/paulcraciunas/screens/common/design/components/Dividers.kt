package com.paulcraciunas.screens.common.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

/** A 1px hairline divider in the theme's divider colour. */
@Composable
fun HairlineDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(1.dp)
            .background(Design.colors.divider)
    )
}

/** A small brass tick centred between two hairlines. */
@Composable
fun StylizedDivider(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        HairlineDivider(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .padding(horizontal = Design.dimensions.spacing.sm)
                .size(Design.dimensions.sizes.bulletPoint)
                .background(Design.colors.accent.copy(alpha = 0.6f), Design.shapes.soft)
        )
        HairlineDivider(modifier = Modifier.weight(1f))
    }
}

/** An eyebrow centred between two hairlines. */
@Composable
fun EyebrowDivider(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        HairlineDivider(modifier = Modifier.weight(1f))
        Eyebrow(text = text, modifier = Modifier.padding(horizontal = Design.dimensions.spacing.sm))
        HairlineDivider(modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true)
@Composable
private fun HairlineDividerPreview() {
    ChessGymTheme {
        HairlineDivider(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun StylizedDividerPreview() {
    ChessGymTheme {
        StylizedDivider(modifier = Modifier.padding(16.dp))
    }
}


@Preview(showBackground = true)
@Composable
private fun EyebrowDividerPreview() {
    ChessGymTheme {
        EyebrowDivider(text = "Or with email", modifier = Modifier.padding(16.dp))
    }
}
