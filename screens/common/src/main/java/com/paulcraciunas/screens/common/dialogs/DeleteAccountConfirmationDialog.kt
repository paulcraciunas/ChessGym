package com.paulcraciunas.screens.common.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.ChessGymDialog
import com.paulcraciunas.screens.common.design.components.DialogIconTone
import com.paulcraciunas.screens.common.design.components.annotatedTextResource
import com.paulcraciunas.screens.common.testTag

@Composable
fun DeleteAccountConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChessGymDialog(
        onDismissRequest = onDismiss,
        title = {
            IconTitle(
                title = stringResource(R.string.delete_account_dialog_title),
                icon = Icons.Default.Warning,
                tone = DialogIconTone.Danger,
            )
        },
        buttons = {
            Paired(
                confirmText = stringResource(R.string.delete_account_dialog_confirm),
                onConfirm = onConfirm,
                dismissText = stringResource(R.string.dialog_cancel),
                onDismiss = onDismiss,
                isDestructive = true,
            )
        },
        modifier = modifier.testTag { DeleteAccountDialogTags.DIALOG },
    ) {
        Column {
            Message(text = annotatedTextResource(R.string.delete_account_dialog_body))
            Danger(
                title = stringResource(R.string.delete_account_dialog_consequences_title),
                items = listOf(
                    stringResource(R.string.delete_account_dialog_consequence_puzzles),
                    stringResource(R.string.delete_account_dialog_consequence_streaks),
                    stringResource(R.string.delete_account_dialog_consequence_games),
                ),
            )
        }
    }
}
