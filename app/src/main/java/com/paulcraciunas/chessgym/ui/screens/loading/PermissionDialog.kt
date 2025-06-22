package com.paulcraciunas.chessgym.ui.screens.loading

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.paulcraciunas.chessgym.R

@Composable
internal fun PermissionDialog(
    onUpdatePermission: (Boolean) -> Unit,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val context = LocalContext.current
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { onUpdatePermission(it) }
        )
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            onUpdatePermission(true)
        }

        AlertDialog(
            onDismissRequest = { onUpdatePermission(false) },
            title = {
                Text(text = stringResource(R.string.enable_notifications_dialog_title))
            },
            text = {
                Text(text = stringResource(R.string.enable_notifications_dialog_body))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                ) {
                    Text(stringResource(R.string.enable_notifications_dialog_accept))
                }
            },
            dismissButton = {
                TextButton(onClick = { onUpdatePermission(false) }) {
                    Text(stringResource(R.string.enable_notifications_dialog_refuse))
                }
            }
        )
    } else {
        onUpdatePermission(true)
    }
}
