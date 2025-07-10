package com.paulcraciunas.screens.puzzles.rated.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.board.ChessPiece
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
internal fun YourMoveIndicator(
    toMove: Side,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChessPiece(
            piece = Piece.King,
            side = toMove,
            modifier = Modifier.size(32.dp)
        )

        Text(
            text = stringResource(R.string.your_move),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun YourMoveIndicatorWhitePreview() {
    ChessGymTheme {
        YourMoveIndicator(toMove = Side.WHITE)
    }
}

@Preview(showBackground = true)
@Composable
private fun YourMoveIndicatorBlackPreview() {
    ChessGymTheme {
        YourMoveIndicator(toMove = Side.BLACK)
    }
}
