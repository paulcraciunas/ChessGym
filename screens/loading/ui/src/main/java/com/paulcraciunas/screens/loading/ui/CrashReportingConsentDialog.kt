package com.paulcraciunas.screens.loading.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.ChessGymDialog
import com.paulcraciunas.screens.common.design.components.DialogIconTone
import com.paulcraciunas.screens.common.design.components.annotatedTextResource

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
            Message(text = annotatedTextResource(R.string.crash_consent_dialog_body))
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
