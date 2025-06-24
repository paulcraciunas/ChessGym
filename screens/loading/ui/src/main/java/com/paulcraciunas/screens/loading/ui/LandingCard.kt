package com.paulcraciunas.screens.loading.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.loading.vm.LoadingState

@Composable
internal fun LandingCard(
    onDownload: () -> Unit,
    onDownloadConfirmation: (Boolean) -> Unit,
    onPermissionResponse: (Boolean) -> Unit,
    state: LoadingState.Ready,
    modifier: Modifier = Modifier
) {
    val canDownload = state.error != LoadingState.Error.NoInternet && state.error != LoadingState.Error.NotEnoughDiskSpace

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.landing_download_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.landing_download_explanation),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (state.error != LoadingState.Error.None) {
                state.error.iconRes()?.let {
                    Icon(
                        painter = painterResource(it),
                        contentDescription = null
                    )
                }
                Text(
                    text = stringResource(state.error.res()),
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            OutlinedButton(
                onClick = onDownload,
                enabled = canDownload,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Absolute.Left
                ) {
                    Icon(
                        painter = painterResource(R.drawable.download_icon),
                        contentDescription = stringResource(R.string.rated_puzzle_hint_description)
                    )
                    Text(
                        text = stringResource(state.error.asDownloadRes()),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }

    when (state.dialog) {
        LoadingState.Dialog.Download -> DownloadConfirmationDialog(
            onCancelled = { onDownloadConfirmation(false) },
            onConfirmed = { onDownloadConfirmation(true) }
        )

        LoadingState.Dialog.Permission -> PermissionDialog(onPermissionResponse)
        LoadingState.Dialog.None -> {}
    }
}

@StringRes
private fun LoadingState.Error.res(): Int = when (this) {
    LoadingState.Error.NoPermission -> R.string.loading_error_permission
    LoadingState.Error.NoInternet -> R.string.loading_error_no_internet
    LoadingState.Error.NotEnoughDiskSpace -> R.string.loading_error_not_enough_disk_space
    else -> R.string.loading_error_generic
}

@DrawableRes
private fun LoadingState.Error.iconRes(): Int? = when (this) {
    LoadingState.Error.NoInternet -> R.drawable.icon_no_network
    LoadingState.Error.NotEnoughDiskSpace -> R.drawable.icon_no_disk_space
    else -> null
}

@StringRes
private fun LoadingState.Error.asDownloadRes(): Int = when (this) {
    LoadingState.Error.Other -> R.string.generic_retry
    else -> R.string.generic_download
}
