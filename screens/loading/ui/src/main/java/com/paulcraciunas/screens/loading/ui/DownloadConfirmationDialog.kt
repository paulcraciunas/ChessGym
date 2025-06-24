package com.paulcraciunas.screens.loading.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.global.resources.R

@Composable
internal fun DownloadConfirmationDialog(
    onCancelled: () -> Unit,
    onConfirmed: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onCancelled() },
        icon = {
            Icon(
                painter = painterResource(R.drawable.download_icon),
                contentDescription = null
            )
        },
        title = {
            Text(text = stringResource(R.string.database_download_dialog_title))
        },
        text = {
            Text(text = stringResource(R.string.database_download_dialog_body))
        },
        confirmButton = {
            TextButton(onClick = { onConfirmed() }) {
                Text(stringResource(R.string.landing_download_button))
            }
        },
        dismissButton = {
            TextButton(onClick = { onCancelled() }) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}
