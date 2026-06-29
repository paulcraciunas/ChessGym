package com.paulcraciunas.screens.loading.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.ChessGymDialog
import com.paulcraciunas.screens.common.design.components.DialogIconTone

@Composable
fun DownloadConfirmationDialog(
    approximateSize: String,
    onCancelled: () -> Unit,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChessGymDialog(
        onDismissRequest = onCancelled,
        modifier = modifier,
        title = {
            IconTitle(
                title = stringResource(R.string.database_download_dialog_title),
                icon = ImageVector.vectorResource(id = R.drawable.download_icon),
                tone = DialogIconTone.Accent,
            )
        },
        body = {
            Message(text = stringResource(R.string.database_download_dialog_body, approximateSize))
        },
        buttons = {
            Paired(
                confirmText = stringResource(R.string.generic_download),
                onConfirm = onConfirmed,
                dismissText = stringResource(android.R.string.cancel),
                onDismiss = onCancelled,
            )
        },
    )
}
