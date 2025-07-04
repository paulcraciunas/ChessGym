package com.paulcraciunas.screens.loading.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.common.theme.LoadingTheme

@Composable
internal fun DownloadConfirmationDialog(
    onCancelled: () -> Unit,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onCancelled,
        modifier = modifier,
        shape = RoundedCornerShape(LoadingTheme.dimensions.factCardRadius),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LoadingTheme.dimensions.dialogContentSpacing)
            ) {
                Icon(
                    painter = painterResource(R.drawable.download_icon),
                    contentDescription = null,
                    modifier = Modifier.size(LoadingTheme.dimensions.dialogIconSize),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = stringResource(R.string.database_download_dialog_title),
                    style = LoadingTheme.typography.dialogTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Text(
                text = stringResource(R.string.database_download_dialog_body),
                style = LoadingTheme.typography.dialogBody,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmed,
                shape = RoundedCornerShape(LoadingTheme.dimensions.buttonRadius)
            ) {
                Text(
                    text = stringResource(R.string.landing_download_button),
                    style = LoadingTheme.typography.dialogButton
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onCancelled,
                shape = RoundedCornerShape(LoadingTheme.dimensions.buttonRadius)
            ) {
                Text(
                    text = stringResource(android.R.string.cancel),
                    style = LoadingTheme.typography.dialogButton
                )
            }
        }
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
