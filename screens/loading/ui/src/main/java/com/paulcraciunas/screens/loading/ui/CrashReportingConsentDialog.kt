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
fun CrashReportingConsentDialog(
    onAccepted: () -> Unit,
    onDeclined: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChessGymDialog(
        onDismissRequest = {},
        modifier = modifier,
        title = {
            IconTitle(
                title = stringResource(R.string.crash_consent_dialog_title),
                icon = ImageVector.vectorResource(id = R.drawable.icon_shield),
                tone = DialogIconTone.Accent,
            )
        },
        body = {
            Message(text = stringResource(R.string.crash_consent_dialog_body))
        },
        buttons = {
            Paired(
                confirmText = stringResource(R.string.generic_agree),
                onConfirm = onAccepted,
                dismissText = stringResource(R.string.generic_not_now),
                onDismiss = onDeclined,
            )
        },
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
