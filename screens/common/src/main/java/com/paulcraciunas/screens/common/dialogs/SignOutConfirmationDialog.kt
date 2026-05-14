package com.paulcraciunas.screens.common.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.ChessGymDialog
import com.paulcraciunas.screens.common.testTag

@Composable
fun SignOutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChessGymDialog(
        onDismissRequest = onDismiss,
        title = { SimpleTitle(title = stringResource(R.string.sign_out_dialog_title)) },
        confirmButton = {
            Standard(
                text = stringResource(R.string.dialog_ok),
                onClick = onConfirm,
                modifier = Modifier.testTag { SignOutDialogTags.CONFIRM },
            )
        },
        dismissButton = {
            Standard(
                text = stringResource(R.string.dialog_cancel),
                onClick = onDismiss,
                modifier = Modifier.testTag { SignOutDialogTags.DISMISS },
            )
        },
        modifier = modifier.testTag { SignOutDialogTags.DIALOG },
    ) {
        Message(text = stringResource(R.string.sign_out_dialog_message))
    }
}
