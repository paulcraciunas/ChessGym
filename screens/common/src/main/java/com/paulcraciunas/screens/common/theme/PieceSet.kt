package com.paulcraciunas.screens.common.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.paulcraciunas.global.resources.R

@Immutable
data class Pieces(
    val pawn: Int = error("Undefined"),
    val rook: Int = error("Undefined"),
    val bishop: Int = error("Undefined"),
    val knight: Int = error("Undefined"),
    val queen: Int = error("Undefined"),
    val king: Int = error("Undefined"),
)

@Immutable
data class PieceSet(
    val white: Pieces = Pieces(),
    val black: Pieces = Pieces()
)

val DefaultWhitePieces = Pieces(
    pawn = R.drawable.pawn_white,
    rook = R.drawable.rook_white,
    bishop = R.drawable.bishop_white,
    knight = R.drawable.knight_white,
    queen = R.drawable.queen_white,
    king = R.drawable.king_white,
)

val DefaultBlackPieces = Pieces(
    pawn = R.drawable.pawn_black,
    rook = R.drawable.rook_black,
    bishop = R.drawable.bishop_black,
    knight = R.drawable.knight_black,
    queen = R.drawable.queen_black,
    king = R.drawable.king_black,
)

val DefaultPieceSet = PieceSet(white = DefaultWhitePieces, black = DefaultBlackPieces)

val LocalPieceSet = staticCompositionLocalOf { PieceSet() }

val PieceIcons: PieceSet
    @Composable
    @ReadOnlyComposable
    get() = LocalPieceSet.current
