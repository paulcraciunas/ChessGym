package com.paulcraciunas.screens.common.dialogs

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.theme.ChessGymTheme

sealed class AbandonConfirmationType {
    @StringRes
    abstract fun getTitle(): Int
    @StringRes
    abstract fun getMessage(): Int

    object Puzzle : AbandonConfirmationType() {
        override fun getTitle(): Int = R.string.abandon_puzzle_title
        override fun getMessage(): Int = R.string.abandon_puzzle_message
    }
    object Game : AbandonConfirmationType() {
        override fun getTitle(): Int = R.string.abandon_game_title
        override fun getMessage(): Int = R.string.abandon_game_message
    }
}

@Composable
fun AbandonConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    type: AbandonConfirmationType = AbandonConfirmationType.Puzzle,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(type.getTitle()),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Text(
                text = stringResource(type.getMessage()),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.abandon_puzzle_confirm),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.abandon_puzzle_cancel),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun AbandonPuzzleConfirmationDialogPreview() {
    ChessGymTheme {
        AbandonConfirmationDialog(
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun AbandonGameConfirmationDialogPreview() {
    ChessGymTheme {
        AbandonConfirmationDialog(
            onConfirm = {},
            onDismiss = {},
            type = AbandonConfirmationType.Game
        )
    }
}
