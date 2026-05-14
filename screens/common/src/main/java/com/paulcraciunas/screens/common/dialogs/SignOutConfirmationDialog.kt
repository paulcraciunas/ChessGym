package com.paulcraciunas.screens.common.dialogs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.ChessGymDialog
import com.paulcraciunas.screens.common.design.components.DialogIconTone
import com.paulcraciunas.screens.common.testTag

@Composable
fun SignOutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChessGymDialog(
        onDismissRequest = onDismiss,
        title = {
            IconTitle(
                title = stringResource(R.string.sign_out_dialog_title),
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                tone = DialogIconTone.Accent,
            )
        },
        buttons = {
            Paired(
                confirmText = stringResource(R.string.sign_out_dialog_confirm),
                onConfirm = onConfirm,
                dismissText = stringResource(R.string.dialog_cancel),
                onDismiss = onDismiss,
            )
        },
        modifier = modifier.testTag { SignOutDialogTags.DIALOG },
    ) {
        Message(text = stringResource(R.string.sign_out_dialog_message))
    }
}
