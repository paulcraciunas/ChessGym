package com.paulcraciunas.screens.loading.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.ChessGymDialog
import com.paulcraciunas.screens.common.design.components.DialogIconTone
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
fun DownloadConfirmationDialog(
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
            Message(text = stringResource(R.string.database_download_dialog_body))
        },
        buttons = {
            Paired(
                confirmText = stringResource(R.string.landing_download_button),
                onConfirm = onConfirmed,
                dismissText = stringResource(android.R.string.cancel),
                onDismiss = onCancelled,
            )
        },
    )
}

@Preview("Download Confirmation Dialog")
@Preview("Download Confirmation Dialog (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun DownloadConfirmationDialogPreview() {
    ChessGymTheme {
        DownloadConfirmationDialog(
            onCancelled = {},
            onConfirmed = {}
        )
    }
}
