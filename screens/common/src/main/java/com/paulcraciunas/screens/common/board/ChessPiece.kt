package com.paulcraciunas.screens.common.board

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.SidedPiece
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.design.theme.pieces.ChessGymPieceSet
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
fun ChessPiece(
    piece: SidedPiece,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
) {
    val scale = if (piece.piece == Piece.Pawn) 0.65f else 0.8f
    val piecePainter: Painter = painterResource(piece.piece.resource(piece.side))

    Image(
        painter = piecePainter,
        contentDescription = stringResource(piece.piece.contentDescription()),
        alpha = alpha,
        modifier = modifier.fillMaxSize(scale),
    )
}

@StringRes
private fun Piece.contentDescription(): Int = when (this) {
    Piece.Pawn -> R.string.pawn
    Piece.Rook -> R.string.rook
    Piece.Knight -> R.string.knight
    Piece.Bishop -> R.string.bishop
    Piece.Queen -> R.string.queen
    Piece.King -> R.string.king
}

@Composable
@DrawableRes
private fun Piece.resource(of: Side): Int = when (this) {
    Piece.Pawn -> Design.pieces.of(of).pawn
    Piece.Rook -> Design.pieces.of(of).rook
    Piece.Knight -> Design.pieces.of(of).knight
    Piece.Bishop -> Design.pieces.of(of).bishop
    Piece.Queen -> Design.pieces.of(of).queen
    Piece.King -> Design.pieces.of(of).king
}

private fun ChessGymPieceSet.of(side: Side) = if (side == Side.WHITE) white else black

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PiecePreview() {
    ChessGymTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SidedPiece.entries.forEach { piece ->
                Box(
                    modifier = Modifier.size(60.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    ChessPiece(
                        piece = piece,
                        alpha = 1f,
                    )
                }
            }
        }
    }
}
