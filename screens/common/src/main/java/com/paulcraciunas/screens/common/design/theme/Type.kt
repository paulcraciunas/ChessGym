package com.paulcraciunas.screens.common.design.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import com.paulcraciunas.screens.common.design.theme.walnut.WalnutTextStyles
import com.paulcraciunas.screens.common.design.theme.walnut.WalnutTypography

/** Pre-set styles you'll reach for outside Material's slots. */
@Immutable
data class DefaultTextStyles(
    val label: TextStyle,
    /** Uppercase, tracked, semi-bold label sat above titles. */
    val eyebrow: TextStyle,
    val title: TextStyle,
    val sectionHeader: TextStyle,
    /** Big serif numerals — ratings, scores, "Up Next" hero. */
    val displayNumericLarge: TextStyle,
    val displayNumeric: TextStyle,
    val displayNumericSmall: TextStyle,
    val monoSmall: TextStyle,
    val monoTimer: TextStyle,
    val footer: TextStyle,
)

/**
 * Provides the extended [Typography] palette across the tree.
 * Read it via `LocalChessGymTypography.current` inside any composable.
 */
internal val LocalChessGymTypography = staticCompositionLocalOf<Typography> {
    error("ChessGymTheme not provided. Wrap your content in ChessGymTheme { ... }.")
}
internal val LocalChessGymTextStyles = staticCompositionLocalOf<DefaultTextStyles> {
    error("ChessGymTheme not provided. Wrap your content in ChessGymTheme { ... }.")
}

internal fun getChessGymTypography(): Typography = WalnutTypography
internal fun getDefaultTextStyles(): DefaultTextStyles = WalnutTextStyles
