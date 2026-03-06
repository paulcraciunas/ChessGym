package com.paulcraciunas.screens.common.controls

import androidx.annotation.StringRes
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
fun YourMoveIndicator(
    toMove: Side,
    modifier: Modifier = Modifier,
    @StringRes textRes: Int = 0,
) {
    val displayText = if (textRes != 0) {
        stringResource(textRes)
    } else {
        stringResource(
            if (toMove == Side.WHITE) R.string.rated_puzzle_find_best_move_white
            else R.string.rated_puzzle_find_best_move_black
        )
    }

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
            text = displayText,
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

@Preview(showBackground = true)
@Composable
private fun YourMoveIndicatorCustomTextPreview() {
    ChessGymTheme {
        YourMoveIndicator(
            toMove = Side.WHITE,
            textRes = R.string.blind_mode_your_turn,
        )
    }
}
