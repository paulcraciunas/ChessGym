package com.paulcraciunas.screens.loading.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.common.theme.LoadingTheme

@SuppressLint("InlinedApi")
@Composable
internal fun PermissionDialog(
    onUpdatePermission: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    PermissionDialogImpl(
        onUpdatePermission = onUpdatePermission,
        modifier = modifier,
        buildCheck = { Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU },
        hasPermission = { ContextCompat.checkSelfPermission(it, Manifest.permission.POST_NOTIFICATIONS) },
    )
}

// Created this so we can show previews
@SuppressLint("InlinedApi")
@Composable
private fun PermissionDialogImpl(
    onUpdatePermission: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    buildCheck: () -> Boolean = { Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU },
    hasPermission: (Context) -> Int = { ContextCompat.checkSelfPermission(it, Manifest.permission.POST_NOTIFICATIONS) },
) {
    if (buildCheck()) {
        val context = LocalContext.current
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { onUpdatePermission(it) }
        )

        if (hasPermission(context) == PackageManager.PERMISSION_GRANTED) {
            onUpdatePermission(true)
            return
        }

        AlertDialog(
            onDismissRequest = { onUpdatePermission(false) },
            modifier = modifier,
            shape = RoundedCornerShape(LoadingTheme.dimensions.factCardRadius),
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(LoadingTheme.dimensions.dialogContentSpacing)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(LoadingTheme.dimensions.dialogIconSize),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = stringResource(R.string.enable_notifications_dialog_title),
                        style = LoadingTheme.typography.dialogTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Text(
                    text = stringResource(R.string.enable_notifications_dialog_body),
                    style = LoadingTheme.typography.dialogBody,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                    shape = RoundedCornerShape(LoadingTheme.dimensions.buttonRadius)
                ) {
                    Text(
                        text = stringResource(R.string.enable_notifications_dialog_accept),
                        style = LoadingTheme.typography.dialogButton
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { onUpdatePermission(false) },
                    shape = RoundedCornerShape(LoadingTheme.dimensions.buttonRadius)
                ) {
                    Text(
                        text = stringResource(R.string.enable_notifications_dialog_refuse),
                        style = LoadingTheme.typography.dialogButton
                    )
                }
            }
        )
    } else {
        onUpdatePermission(true)
    }
}

@Preview("Permission Dialog")
@Preview("Permission Dialog (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PermissionDialogPreview() {
    ChessGymTheme {
        PermissionDialogImpl(
            onUpdatePermission = {},
            buildCheck = { true },
            hasPermission = { PackageManager.PERMISSION_DENIED }
        )
    }
}
