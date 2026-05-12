package com.paulcraciunas.screens.common.design.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.paulcraciunas.screens.common.design.theme.Design

/**
 * Uppercase, tracked, semi-bold label sat above titles and section heads.
 *
 *   Eyebrow("Profile")
 *   Eyebrow("Up Next", color = cg.accent)
 */
@Composable
fun Eyebrow(
    text: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
) {
    Text(
        text = text.uppercase(),
        color = color ?: Design.colors.inkMuted,
        style = Design.textStyles.eyebrow,
        modifier = modifier,
    )
}
