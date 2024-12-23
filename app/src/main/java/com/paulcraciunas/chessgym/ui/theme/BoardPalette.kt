package com.paulcraciunas.chessgym.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class BoardPalette(
    val boardEdge: Color = Color.Unspecified,
    val boardText: Color = Color.Unspecified,
    val boardSquareWhite: Color = Color.Unspecified,
    val boardSquareBlack: Color = Color.Unspecified,
    val boardSquareSelected: Color = Color.Unspecified,
    val boardMoveAvailable: Color = Color.Unspecified,
    val boardMovePrevious: Color = Color.Unspecified,
)

val WoodenBoardPalette = BoardPalette(
    boardEdge = Color(color = 0xFF293342),
    boardText = Color(color = 0xFF6880A4),
    boardSquareWhite = Color(color = 0xFFE8BB64),
    boardSquareBlack = Color(color = 0xFF9E5B27),
    boardSquareSelected = Color(color = 0x99999999),
    boardMoveAvailable = Color(color = 0xDD999999),
    boardMovePrevious = Color(color = 0x8800CA99),
)

val GreyBoardPalette = BoardPalette(
    boardEdge = Color(color = 0xFF293342),
    boardText = Color(color = 0xFF6880A4),
    boardSquareWhite = Color(color = 0xFF999999),
    boardSquareBlack = Color(color = 0xFF625F5E),
    boardSquareSelected = Color(color = 0xFF9E5B27),
    boardMoveAvailable = Color(color = 0xFF9E5B27),
    boardMovePrevious = Color(color = 0x8800CA99),
)

val LocalBoardPalette = staticCompositionLocalOf { BoardPalette() }

val BoardColors: BoardPalette
    @Composable
    @ReadOnlyComposable
    get() = LocalBoardPalette.current
