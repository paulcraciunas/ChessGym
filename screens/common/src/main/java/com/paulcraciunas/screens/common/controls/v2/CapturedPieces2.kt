package com.paulcraciunas.screens.common.controls.v2

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
fun CapturedPieces2(
    capturedPieces: String,
    side: Side,
    modifier: Modifier = Modifier,
) {
    val color = side.toBinaryColor()
    Text(
        text = capturedPieces,
        fontSize = 18.sp,
        color = color,
        modifier = modifier
            .height(Design.dimensions.sizes.navBarIconHeight)
            .padding(horizontal = Design.dimensions.spacing.xxl),
    )
}

@Composable
private fun Side.toBinaryColor() = when (this) {
    Side.WHITE -> Design.colors.pieceLight
    Side.BLACK -> Design.colors.pieceDark
}

@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun CapturedPiecesWhitePreview() {
    ChessGymTheme {
        CapturedPieces2(
            capturedPieces = listOf(
                Piece.Queen,
                Piece.Rook,
                Piece.Bishop,
                Piece.Knight,
                Piece.Pawn,
                Piece.Pawn,
                Piece.Pawn
            ).joinToString("") { it.unicode },
            side = Side.WHITE
        )
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun CapturedPiecesBlackPreview() {
    ChessGymTheme {
        CapturedPieces2(
            capturedPieces = listOf(
                Piece.Rook,
                Piece.Bishop,
                Piece.Knight,
                Piece.Pawn,
                Piece.Pawn
            ).joinToString("") { it.unicode },
            side = Side.BLACK
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CapturedPiecesEmptyPreview() {
    ChessGymTheme {
        CapturedPieces2(
            capturedPieces = "",
            side = Side.WHITE
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CapturedPiecesFullSetPreview() {
    ChessGymTheme {
        CapturedPieces2(
            capturedPieces = listOf(
                Piece.King,
                Piece.Queen,
                Piece.Rook,
                Piece.Rook,
                Piece.Bishop,
                Piece.Bishop,
                Piece.Knight,
                Piece.Knight,
                Piece.Pawn,
                Piece.Pawn,
                Piece.Pawn,
                Piece.Pawn,
                Piece.Pawn,
                Piece.Pawn,
                Piece.Pawn,
                Piece.Pawn
            ).joinToString("") { it.unicode },
            side = Side.BLACK
        )
    }
}
