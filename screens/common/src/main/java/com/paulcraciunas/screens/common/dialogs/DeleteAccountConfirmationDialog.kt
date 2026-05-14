package com.paulcraciunas.screens.common.dialogs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.ChessGymDialog
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
            )
        },
        confirmButton = {
            Standard(
                text = stringResource(R.string.delete_account_dialog_confirm),
                onClick = onConfirm,
                isDestructive = true,
                modifier = Modifier.testTag { DeleteAccountDialogTags.CONFIRM },
            )
        },
        dismissButton = {
            Standard(
                text = stringResource(R.string.dialog_cancel),
                onClick = onDismiss,
                modifier = Modifier.testTag { DeleteAccountDialogTags.DISMISS },
            )
        },
        modifier = modifier.testTag { DeleteAccountDialogTags.DIALOG },
    ) {
        WarningMessage(
            text = stringResource(R.string.delete_account_dialog_message),
            explanation = stringResource(R.string.delete_account_dialog_explanation),
        )
    }
}
