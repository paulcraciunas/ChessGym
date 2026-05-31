package com.paulcraciunas.screens.common.design.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.paulcraciunas.screens.common.design.theme.walnut.WalnutDarkColors
import com.paulcraciunas.screens.common.design.theme.walnut.WalnutLightColors

/**
 * Walnut palette — light & dark.
 *
 * The tokens map onto the design exploration's Walnut theme. They're richer than
 * Material 3's [androidx.compose.material3.ColorScheme] so we keep them as a
 * separate [ChessGymColors] type.
 */
@Immutable
data class ChessGymColors(
    // Surfaces
    val bg: Color,
    val bgTint: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val border: Color,
    val borderSoft: Color,
    val divider: Color,

    // Ink
    val ink: Color,
    val inkSoft: Color,
    val inkMuted: Color,
    val inkSubtle: Color,
    val onPrimary: Color,

    // Brand
    val primary: Color,
    val primaryDisabled: Color,
    val primarySoft: Color,
    val primarySoftDisabled: Color,
    val primaryDeep: Color,
    val accent: Color,
    val accentSoft: Color,

    // Status
    val success: Color,
    val danger: Color,

    // Board
    val boardLight: Color,
    val boardDark: Color,
    val pieceLight: Color,
    val pieceDark: Color,
    val boardEdge: Color,
    val boardText: Color,
    val boardSquareSelected: Color,
    val boardMoveAvailable: Color,
    val boardMovePrevious: Color,

    // Chips & misc
    val chipSolvedBg: Color,
    val chipSolvedBorder: Color,
    val chipSolvedInk: Color,
    val chipAccentInk: Color,

    val medallionDepth: Color,
    val rate: Color,
    val donate: Color,
    val achievementTierBronze: Color,
    val achievementTierSilver: Color,
    val achievementTierGold: Color,
    val achievementTierEmerald: Color,
    val achievementTierDiamond: Color,

    val isDark: Boolean,
) {
    val borderNone: BorderStroke = BorderStroke(0.dp, borderSoft)
    val softBorderStroke: BorderStroke = BorderStroke(1.dp, borderSoft)
    val primaryBorderStroke: BorderStroke = BorderStroke(1.dp, primary)
    val surfaceBorderStroke: BorderStroke = BorderStroke(2.dp, surface)
}

/**
 * Provides the extended [ChessGymColors] palette across the tree.
 * Read it via `LocalChessGymColors.current` inside any composable.
 */
internal val LocalChessGymColors = staticCompositionLocalOf<ChessGymColors> {
    error("ChessGymTheme not provided. Wrap your content in ChessGymTheme { ... }.")
}

internal fun getChessGymColors(
    definedTheme: DefinedTheme = DefinedTheme.Walnut,
    darkMode: Boolean,
): ChessGymColors = when (definedTheme) {
    DefinedTheme.Walnut -> if (darkMode) WalnutDarkColors else WalnutLightColors
}

internal fun createChessGymColorScheme(
    definedTheme: DefinedTheme = DefinedTheme.Walnut,
    darkMode: Boolean,
): ColorScheme {
    val chessGymColors = getChessGymColors(definedTheme = definedTheme, darkMode = darkMode)
    val materialScheme = if (darkMode) {
        darkColorScheme(
            primary = chessGymColors.primary,
            onPrimary = chessGymColors.onPrimary,
            primaryContainer = chessGymColors.primarySoft,
            onPrimaryContainer = chessGymColors.primary,
            secondary = chessGymColors.accent,
            onSecondary = chessGymColors.bg,
            secondaryContainer = chessGymColors.accentSoft,
            onSecondaryContainer = chessGymColors.accent,
            tertiary = chessGymColors.accent,
            onTertiary = chessGymColors.bg,
            background = chessGymColors.bg,
            onBackground = chessGymColors.ink,
            surface = chessGymColors.surface,
            onSurface = chessGymColors.ink,
            surfaceVariant = chessGymColors.surfaceAlt,
            onSurfaceVariant = chessGymColors.inkSoft,
            outline = chessGymColors.border,
            outlineVariant = chessGymColors.borderSoft,
            error = chessGymColors.danger,
            onError = chessGymColors.onPrimary,
        )
    } else {
        lightColorScheme(
            primary = chessGymColors.primary,
            onPrimary = chessGymColors.onPrimary,
            primaryContainer = chessGymColors.primarySoft,
            onPrimaryContainer = chessGymColors.primary,
            secondary = chessGymColors.accent,
            onSecondary = chessGymColors.onPrimary,
            secondaryContainer = chessGymColors.accentSoft,
            onSecondaryContainer = chessGymColors.chipAccentInk,
            tertiary = chessGymColors.accent,
            onTertiary = chessGymColors.onPrimary,
            background = chessGymColors.bg,
            onBackground = chessGymColors.ink,
            surface = chessGymColors.surface,
            onSurface = chessGymColors.ink,
            surfaceVariant = chessGymColors.surfaceAlt,
            onSurfaceVariant = chessGymColors.inkSoft,
            outline = chessGymColors.border,
            outlineVariant = chessGymColors.borderSoft,
            error = chessGymColors.danger,
            onError = chessGymColors.onPrimary,
        )
    }
    return materialScheme
}
