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
internal fun CrashReportingConsentDialog(
    onAccepted: () -> Unit,
    onDeclined: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = {},
        modifier = modifier,
        shape = RoundedCornerShape(LoadingTheme.dimensions.factCardRadius),
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LoadingTheme.dimensions.dialogContentSpacing)
            ) {
                Icon(
                    painter = painterResource(R.drawable.icon_shield),
                    contentDescription = null,
                    modifier = Modifier.size(LoadingTheme.dimensions.dialogIconSize),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = stringResource(R.string.crash_consent_dialog_title),
                    style = LoadingTheme.typography.dialogTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Text(
                text = stringResource(R.string.crash_consent_dialog_body),
                style = LoadingTheme.typography.dialogBody,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onAccepted,
                shape = RoundedCornerShape(LoadingTheme.dimensions.buttonRadius)
            ) {
                Text(
                    text = stringResource(R.string.crash_consent_dialog_accept),
                    style = LoadingTheme.typography.dialogButton
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDeclined,
                shape = RoundedCornerShape(LoadingTheme.dimensions.buttonRadius)
            ) {
                Text(
                    text = stringResource(R.string.crash_consent_dialog_refuse),
                    style = LoadingTheme.typography.dialogButton
                )
            }
        }
    )
}

@Preview("Crash Reporting Consent Dialog")
@Preview("Crash Reporting Consent Dialog (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun CrashReportingConsentDialogPreview() {
    ChessGymTheme {
        CrashReportingConsentDialog(
            onAccepted = {},
            onDeclined = {}
        )
    }
}
