package com.paulcraciunas.screens.common.design.theme.pieces

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import com.paulcraciunas.screens.common.design.theme.pieces.standard.DefaultPieceSet

@Immutable
data class ChessGymPieces(
    val pawn: Int = error("Undefined"),
    val rook: Int = error("Undefined"),
    val bishop: Int = error("Undefined"),
    val knight: Int = error("Undefined"),
    val queen: Int = error("Undefined"),
    val king: Int = error("Undefined"),
)

@Immutable
data class ChessGymPieceSet(
    val white: ChessGymPieces = ChessGymPieces(),
    val black: ChessGymPieces = ChessGymPieces()
)

internal val LocalChessGymPieces = staticCompositionLocalOf<ChessGymPieceSet> {
    error("ChessGymTheme not provided. Wrap your content in ChessGymTheme { ... }.")
}

internal fun getChessGymPieces(): ChessGymPieceSet = DefaultPieceSet
