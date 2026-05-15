package com.paulcraciunas.screens.boardvis.pieces.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.IconButton
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
import com.paulcraciunas.screens.common.design.components.PlayButton
import com.paulcraciunas.screens.common.design.components.PrimaryPillButton
import com.paulcraciunas.screens.common.design.components.ToggleRow
import com.paulcraciunas.screens.common.design.components.borderSoft
import com.paulcraciunas.screens.common.design.theme.Design
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
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xxl)
    ) {
        ToggleRow(
            title = stringResource(R.string.boardvis_move_piece_training_mode),
            on = isTrainingMode,
            onChange = onTrainingModeToggled,
            last = true,
        )
        PieceSelectionRow(
            selectedPiece = selectedPiece,
            enabled = isTrainingMode,
            onPieceSelected = onPieceSelected
        )
        PlayButton(onClick = onPlayClicked)
        // Rules text
        Text(
            text = stringResource(R.string.boardvis_move_piece_rules),
            style = Design.typography.bodyMedium,
            color = Design.colors.inkSoft,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Design.dimensions.spacing.xxl)
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
        horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.lg),
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
        !enabled -> Design.colors.borderSoft
        isSelected -> Design.colors.primary
        else -> Design.colors.borderSoft
    }

    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(Design.dimensions.sizes.avatar)
            .clip(Design.shapes.circle)
            .background(Color.Black.copy(alpha = enabled.alpha))
            .border(
                width = Design.dimensions.spacing.xxs,
                color = borderColor,
                shape = Design.shapes.circle,
            )
    ) {
        ChessPiece(
            piece = piece,
            side = Side.WHITE,
            modifier = Modifier.size(Design.dimensions.sizes.iconButton),
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
            .padding(Design.dimensions.spacing.xgut),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.lg)
    ) {
        Text(
            text = stringResource(R.string.boardvis_move_piece_moves_remaining, movesRemaining),
            style = Design.typography.titleLarge,
            color = Design.colors.primary
        )

        Text(
            text = stringResource(R.string.boardvis_move_piece_current_score, currentScore),
            style = Design.typography.titleMedium,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = Design.colors.ink
        )
    }
}

@Composable
internal fun MoveThePieceGameOverControls(
    finalScore: Int,
    isNewHighScore: Boolean,
    wasCaptured: Boolean,
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(Design.shapes.card)
            .border(borderSoft(), Design.shapes.card)
            .background(Design.colors.surface)
            .padding(Design.dimensions.spacing.xgut),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.lg)
    ) {
        Text(
            text = if (wasCaptured) {
                stringResource(R.string.boardvis_move_piece_captured)
            } else {
                stringResource(R.string.boardvis_game_over)
            },
            style = Design.typography.headlineSmall,
            color = Design.colors.ink
        )

        Text(
            text = stringResource(R.string.boardvis_final_score, finalScore),
            style = Design.typography.titleLarge,
            color = Design.colors.ink
        )

        if (isNewHighScore) {
            Text(
                text = stringResource(R.string.generic_new_high_score),
                style = Design.typography.titleMedium,
                color = Design.colors.primary
            )
        }

        PrimaryPillButton(
            text = stringResource(R.string.boardvis_play_again),
            onClick = onPlayAgain,
            leadingIcon = Icons.Filled.Refresh,
        )
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
            wasCaptured = true,
            onPlayAgain = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
