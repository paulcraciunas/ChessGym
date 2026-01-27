package com.paulcraciunas.screens.boardvis.pieces.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.boardvis.pieces.vm.MoveThePieceUiState
import com.paulcraciunas.screens.common.board.ChessPiece
import com.paulcraciunas.screens.common.controls.PlayButton
import com.paulcraciunas.screens.common.controls.RefreshButton
import com.paulcraciunas.screens.common.extensions.alpha
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
internal fun MoveThePieceSetupControls(
    isTrainingMode: Boolean,
    selectedPiece: Piece,
    onTrainingModeToggled: (Boolean) -> Unit,
    onPieceSelected: (Piece) -> Unit,
    onPlayClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Training mode toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.boardvis_move_piece_training_mode),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = isTrainingMode,
                onCheckedChange = onTrainingModeToggled
            )
        }

        PieceSelectionRow(
            selectedPiece = selectedPiece,
            enabled = isTrainingMode,
            onPieceSelected = onPieceSelected
        )

        PlayButton(onClick = onPlayClicked)

        // Rules text
        Text(
            text = stringResource(R.string.boardvis_move_piece_rules),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun PieceSelectionRow(
    selectedPiece: Piece,
    enabled: Boolean,
    onPieceSelected: (Piece) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MoveThePieceUiState.GAME_PIECES.forEach { piece ->
            PieceSelectionButton(
                piece = piece,
                isSelected = selectedPiece == piece,
                enabled = enabled,
                onClick = { onPieceSelected(piece) }
            )
        }
    }
}

@Composable
private fun PieceSelectionButton(
    piece: Piece,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = when {
        !enabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = enabled.alpha))
            .border(
                width = 4.dp,
                color = borderColor,
                shape = CircleShape
            )
    ) {
        ChessPiece(
            piece = piece,
            side = Side.WHITE,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
internal fun MoveThePiecePlayingControls(
    movesRemaining: Int,
    currentScore: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Moves remaining
        Text(
            text = stringResource(R.string.boardvis_move_piece_moves_remaining, movesRemaining),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Current score
        Text(
            text = stringResource(R.string.boardvis_move_piece_current_score, currentScore),
            style = MaterialTheme.typography.titleMedium,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
internal fun MoveThePieceGameOverControls(
    finalScore: Int,
    isNewHighScore: Boolean,
    previousHighScore: Int,
    wasCaptured: Boolean,
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (wasCaptured) {
                stringResource(R.string.boardvis_move_piece_captured)
            } else {
                stringResource(R.string.boardvis_game_over)
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = stringResource(R.string.boardvis_final_score, finalScore),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (isNewHighScore) {
            Text(
                text = stringResource(R.string.generic_new_high_score),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Text(
                text = stringResource(R.string.boardvis_high_score, previousHighScore),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        RefreshButton(onClick = onPlayAgain)
    }
}

@Preview("MoveThePieceSetupControls", showBackground = true)
@Preview("MoveThePieceSetupControls (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun MoveThePieceSetupControlsPreview() {
    ChessGymTheme {
        MoveThePieceSetupControls(
            isTrainingMode = true,
            selectedPiece = Piece.Rook,
            onTrainingModeToggled = {},
            onPieceSelected = {},
            onPlayClicked = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview("MoveThePieceSetupControls - Training Disabled")
@Composable
private fun MoveThePieceSetupControlsDisabledPreview() {
    ChessGymTheme {
        MoveThePieceSetupControls(
            isTrainingMode = false,
            selectedPiece = Piece.Rook,
            onTrainingModeToggled = {},
            onPieceSelected = {},
            onPlayClicked = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview("MoveThePiecePlayingControls")
@Composable
private fun MoveThePiecePlayingControlsPreview() {
    ChessGymTheme {
        MoveThePiecePlayingControls(
            movesRemaining = 2,
            currentScore = 8,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview("MoveThePieceGameOverControls - New High Score")
@Composable
private fun MoveThePieceGameOverNewHighScorePreview() {
    ChessGymTheme {
        MoveThePieceGameOverControls(
            finalScore = 15,
            isNewHighScore = true,
            previousHighScore = 12,
            wasCaptured = false,
            onPlayAgain = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview("MoveThePieceGameOverControls - Captured")
@Composable
private fun MoveThePieceGameOverCapturedPreview() {
    ChessGymTheme {
        MoveThePieceGameOverControls(
            finalScore = 8,
            isNewHighScore = false,
            previousHighScore = 15,
            wasCaptured = true,
            onPlayAgain = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
