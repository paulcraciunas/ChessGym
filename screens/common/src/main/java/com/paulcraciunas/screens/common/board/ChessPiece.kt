package com.paulcraciunas.screens.common.board

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.design.theme.pieces.ChessGymPieceSet
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
fun ChessPiece(
    piece: Piece,
    side: Side,
    modifier: Modifier = Modifier,
) {
    val scale = piece.scaleFactor()

    Image(
        painter = painterResource(piece.resource(side)),
        contentDescription = stringResource(id = piece.contentDescription()),
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
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
private fun Piece.scaleFactor(): Float = when (this) {
    Piece.Pawn -> Design.dimensions.scales.piecePawn
    else -> Design.dimensions.scales.pieceDefault
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
            Side.entries.forEach { side ->
                Piece.entries.forEach { piece ->
                    Box(
                        modifier = Modifier.size(60.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        ChessPiece(
                            piece = piece,
                            side = side,
                        )
                    }
                }
            }
        }
    }
}
