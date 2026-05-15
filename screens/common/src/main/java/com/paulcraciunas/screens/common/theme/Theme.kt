package com.paulcraciunas.screens.common.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import com.paulcraciunas.screens.common.design.theme.DefinedTheme
import com.paulcraciunas.screens.common.design.theme.LocalChessGymColors
import com.paulcraciunas.screens.common.design.theme.LocalChessGymDimensions
import com.paulcraciunas.screens.common.design.theme.LocalChessGymRadii
import com.paulcraciunas.screens.common.design.theme.LocalChessGymShapes
import com.paulcraciunas.screens.common.design.theme.LocalChessGymTextStyles
import com.paulcraciunas.screens.common.design.theme.LocalChessGymTypography
import com.paulcraciunas.screens.common.design.theme.createChessGymColorScheme
import com.paulcraciunas.screens.common.design.theme.getChessGymColors
import com.paulcraciunas.screens.common.design.theme.getChessGymDimensions
import com.paulcraciunas.screens.common.design.theme.getChessGymRadii
import com.paulcraciunas.screens.common.design.theme.getChessGymShapes
import com.paulcraciunas.screens.common.design.theme.getChessGymTypography
import com.paulcraciunas.screens.common.design.theme.getDefaultTextStyles
import com.paulcraciunas.screens.common.design.theme.getStandardShapes
import com.paulcraciunas.screens.common.design.theme.pieces.LocalChessGymPieces
import com.paulcraciunas.screens.common.design.theme.pieces.getChessGymPieces
import com.paulcraciunas.screens.common.extensions.DefaultAlpha
import com.paulcraciunas.screens.common.extensions.LocalAlpha
import com.paulcraciunas.screens.common.isDarkTheme

object ThemeTags {
    const val ROOT = "chess_gym_theme_root"
}

@Composable
fun ChessGymTheme(
    definedTheme: DefinedTheme = DefinedTheme.Walnut,
    darkMode: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val chessGymColors = getChessGymColors(definedTheme = definedTheme, darkMode = darkMode)
    val chessGymColorScheme = createChessGymColorScheme(definedTheme = definedTheme, darkMode = darkMode)
    val chessGymTypography = getChessGymTypography()
    val chessGymTextStyles = getDefaultTextStyles()
    val chessGymDimensions = getChessGymDimensions()
    val chessGymShapes = getChessGymShapes()
    val chessGymStandardShapes = getStandardShapes()
    val chessGymRadii = getChessGymRadii()
    val pieces = getChessGymPieces()

    val alpha = DefaultAlpha
    val loadingTypography = createLoadingTypography()

    CompositionLocalProvider(
        LocalChessGymColors provides chessGymColors,
        LocalChessGymTypography provides chessGymTypography,
        LocalChessGymTextStyles provides chessGymTextStyles,
        LocalChessGymDimensions provides chessGymDimensions,
        LocalChessGymShapes provides chessGymShapes,
        LocalChessGymRadii provides chessGymRadii,
        LocalChessGymPieces provides pieces,
        LocalAlpha provides alpha,
        LocalLoadingColors provides LoadingColors(),
        LocalLoadingTypography provides loadingTypography,
        LocalLoadingDimensions provides LoadingDimensions(),
        LocalLoadingAlphas provides LoadingAlphas(),
        LocalLoadingBorders provides LoadingBorders()
    ) {
        MaterialTheme(
            colorScheme = chessGymColorScheme,
            typography = chessGymTypography,
            shapes = chessGymStandardShapes,
        ) {
            Box(
                modifier = Modifier
                    .testTag(ThemeTags.ROOT)
                    .semantics { isDarkTheme = chessGymColors.isDark },
            ) {
                content()
            }
        }
    }
}
