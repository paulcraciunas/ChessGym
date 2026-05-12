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
fun Title(
    text: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
) {
    Text(
        text = text.uppercase(),
        color = color ?: Design.colors.inkMuted,
        style = Design.typography.titleSmall,
        modifier = modifier,
    )
}

@Composable
fun SectionHeaderTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        color = Design.colors.primary,
        style = Design.textStyles.sectionHeader,
        modifier = modifier,
    )
}
