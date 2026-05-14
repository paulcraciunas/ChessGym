package com.paulcraciunas.screens.common.dialogs

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.ChessGymDialog
import com.paulcraciunas.screens.common.testTag

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
    ChessGymDialog(
        onDismissRequest = onDismiss,
        title = { SimpleTitle(title = stringResource(type.getTitle())) },
        buttons = {
            Paired(
                confirmText = stringResource(R.string.abandon_puzzle_confirm),
                onConfirm = onConfirm,
                dismissText = stringResource(R.string.abandon_puzzle_cancel),
                onDismiss = onDismiss,
                isDestructive = true,
            )
        },
        modifier = Modifier.testTag { AbandonConfirmationDialogTags.DIALOG },
    ) {
        Message(text = stringResource(type.getMessage()))
    }
}
