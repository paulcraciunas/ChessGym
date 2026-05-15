package com.paulcraciunas.screens.common.controls

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
fun CapturedPieces(
    capturedPieces: List<Piece>,
    side: Side,
    modifier: Modifier = Modifier
) {
    val normalizedSide = side.normalize()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Design.dimensions.sizes.navBarIconHeight)
            .padding(horizontal = Design.dimensions.spacing.xxl),
        contentAlignment = Alignment.CenterStart
    ) {
        if (capturedPieces.isNotEmpty()) {
            Text(
                text = capturedPieces.joinToString("") { piece ->
                    piece.toUnicodeChar(normalizedSide)
                },
                style = Design.typography.bodyLarge.copy(fontSize = 18.sp),
                color = Design.colors.ink,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun Piece.toUnicodeChar(side: Side): String = when (this) {
    Piece.King -> if (side == Side.WHITE) "♔" else "♚"
    Piece.Queen -> if (side == Side.WHITE) "♕" else "♛"
    Piece.Rook -> if (side == Side.WHITE) "♖" else "♜"
    Piece.Bishop -> if (side == Side.WHITE) "♗" else "♝"
    Piece.Knight -> if (side == Side.WHITE) "♘" else "♞"
    Piece.Pawn -> if (side == Side.WHITE) "♙" else "♟"
}

@Composable
@Stable
private fun Side.normalize(): Side = if (!Design.colors.isDark) this else this.other()

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun CapturedPiecesWhitePreview() {
    ChessGymTheme {
        CapturedPieces(
            capturedPieces = listOf(
                Piece.Queen,
                Piece.Rook,
                Piece.Bishop,
                Piece.Knight,
                Piece.Pawn,
                Piece.Pawn,
                Piece.Pawn
            ),
            side = Side.WHITE
        )
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun CapturedPiecesBlackPreview() {
    ChessGymTheme {
        CapturedPieces(
            capturedPieces = listOf(
                Piece.Rook,
                Piece.Bishop,
                Piece.Knight,
                Piece.Pawn,
                Piece.Pawn
            ),
            side = Side.BLACK
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CapturedPiecesEmptyPreview() {
    ChessGymTheme {
        CapturedPieces(
            capturedPieces = emptyList(),
            side = Side.WHITE
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CapturedPiecesFullSetPreview() {
    ChessGymTheme {
        CapturedPieces(
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
            ),
            side = Side.BLACK
        )
    }
}
