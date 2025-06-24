package com.paulcraciunas.screens.common.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

enum class BoardTheme {
    Wood,
    Grey
}

@Composable
fun ChessGymTheme(
    boardTheme: BoardTheme = BoardTheme.Wood,
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val boardPalette = when (boardTheme) {
        BoardTheme.Wood -> WoodenBoardPalette
        BoardTheme.Grey -> GreyBoardPalette
    }
    val pieces = DefaultPieceSet // We don't currently have multiple piece sets
    val tokens = DefaultTokens // We don't currently have multiple sets of tokens

    CompositionLocalProvider(
        LocalBoardPalette provides boardPalette,
        LocalPieceSet provides pieces,
        LocalTokens provides tokens
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
