package com.paulcraciunas.chessgym.ui.board

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.chessgym.R
import com.paulcraciunas.chessgym.ui.theme.ChessGymTheme
import com.paulcraciunas.chessgym.ui.theme.GlobalTokens
import com.paulcraciunas.chessgym.ui.theme.PieceIcons
import com.paulcraciunas.chessgym.ui.theme.PieceSet
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Piece

@Composable
fun ChessPiece(
    piece: Piece,
    side: Side,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(piece.resource(side)),
        contentDescription = stringResource(id = piece.contentDescription()),
        modifier = modifier
            .fillMaxSize(piece.scaleFactor())
            .aspectRatio(1f)
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
    Piece.Pawn -> GlobalTokens.scaleFactorPawn
    else -> GlobalTokens.scaleFactorDefault
}

@Composable
@DrawableRes
private fun Piece.resource(of: Side): Int = when (this) {
    Piece.Pawn -> PieceIcons.of(of).pawn
    Piece.Rook -> PieceIcons.of(of).rook
    Piece.Knight -> PieceIcons.of(of).knight
    Piece.Bishop -> PieceIcons.of(of).bishop
    Piece.Queen -> PieceIcons.of(of).queen
    Piece.King -> PieceIcons.of(of).king
}

private fun PieceSet.of(side: Side) = if (side == Side.WHITE) white else black

@Preview(showBackground = true)
@Composable
fun PiecePreview() {
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
