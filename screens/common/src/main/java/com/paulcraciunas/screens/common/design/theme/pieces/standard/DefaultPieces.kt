package com.paulcraciunas.screens.common.design.theme.pieces.standard

import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.theme.pieces.ChessGymPieceSet
import com.paulcraciunas.screens.common.design.theme.pieces.ChessGymPieces

internal val DefaultWhitePieces = ChessGymPieces(
    pawn = R.drawable.pawn_white,
    rook = R.drawable.rook_white,
    bishop = R.drawable.bishop_white,
    knight = R.drawable.knight_white,
    queen = R.drawable.queen_white,
    king = R.drawable.king_white,
)

internal val DefaultBlackPieces = ChessGymPieces(
    pawn = R.drawable.pawn_black,
    rook = R.drawable.rook_black,
    bishop = R.drawable.bishop_black,
    knight = R.drawable.knight_black,
    queen = R.drawable.queen_black,
    king = R.drawable.king_black,
)

internal val DefaultPieceSet = ChessGymPieceSet(white = DefaultWhitePieces, black = DefaultBlackPieces)
