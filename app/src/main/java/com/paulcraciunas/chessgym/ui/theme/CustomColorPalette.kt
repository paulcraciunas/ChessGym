package com.paulcraciunas.chessgym.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CustomColorPalette(
    val boardSquareWhite: Color = Color.Unspecified,
    val boardSquareWhiteSelected: Color = Color.Unspecified,
    val boardSquareBlack: Color = Color.Unspecified,
    val boardSquareBlackSelected: Color = Color.Unspecified,
    val boardMoveAvailable: Color = Color.Unspecified,
)

private val YellowPale = Color(color = 0xFFF0D9B5)
private val BrownPale = Color(color = 0xFFB58863)
private val DarkGreen = Color(color = 0x8023630B)
private val BrownGrey = Color(color = 0x88EEC585)
private val OrangePale = Color(color = 0xFFCA783A)

val LightCustomColorPalette = CustomColorPalette(
    boardSquareWhite = YellowPale,
    boardSquareWhiteSelected = BrownGrey,
    boardSquareBlack = BrownPale,
    boardSquareBlackSelected = OrangePale,
    boardMoveAvailable = DarkGreen,
)

// TODO Paul: customize dark mode colours
val DarkCustomColorPalette = CustomColorPalette(
    boardSquareWhite = YellowPale,
    boardSquareWhiteSelected = BrownGrey,
    boardSquareBlack = BrownPale,
    boardSquareBlackSelected = OrangePale,
    boardMoveAvailable = DarkGreen,
)

val LocalCustomColorPalette = staticCompositionLocalOf { CustomColorPalette() }
