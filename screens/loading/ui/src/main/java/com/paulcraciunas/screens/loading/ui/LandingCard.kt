package com.paulcraciunas.screens.loading.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.common.theme.LoadingTheme
import com.paulcraciunas.screens.loading.vm.LoadingState

@Composable
internal fun LandingCard(
    onDownload: () -> Unit,
    onDownloadConfirmation: (Boolean) -> Unit,
    onPermissionResponse: (Boolean) -> Unit,
    state: LoadingState.Ready,
    modifier: Modifier = Modifier
) {
    val canDownload = state.error != LoadingState.Error.NoInternet

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = LoadingTheme.dimensions.horizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LoadingTheme.dimensions.landingContentSpacing)
        ) {
            // App Title
            Text(
                text = stringResource(R.string.app_name),
                style = LoadingTheme.typography.appTitleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(LoadingTheme.dimensions.titleSpacing))

            // Landing Title
            Text(
                text = stringResource(R.string.landing_download_title),
                style = LoadingTheme.typography.landingTitle,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            // Description
            Text(
                text = stringResource(R.string.landing_download_explanation),
                style = LoadingTheme.typography.landingDescription,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Error Display
            if (state.error != LoadingState.Error.None) {
                ErrorCard(error = state.error)
            }

            // Download Button
            Button(
                onClick = onDownload,
                enabled = canDownload,
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(LoadingTheme.dimensions.buttonRadius)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(LoadingTheme.dimensions.buttonSpacing)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.download_icon),
                        contentDescription = stringResource(R.string.generic_download),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(state.error.asDownloadRes()),
                        style = LoadingTheme.typography.buttonText
                    )
                }
            }

            Spacer(modifier = Modifier.height(LoadingTheme.dimensions.bottomSpacing))

            // App name at bottom
            Text(
                text = stringResource(R.string.app_name),
                style = LoadingTheme.typography.appTitleSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }

    // Dialogs
    when (state.dialog) {
        LoadingState.Dialog.Download -> DownloadConfirmationDialog(
            onCancelled = { onDownloadConfirmation(false) },
            onConfirmed = { onDownloadConfirmation(true) }
        )
        LoadingState.Dialog.Permission -> PermissionDialog(onPermissionResponse)
        LoadingState.Dialog.None -> {}
    }
}

@Composable
private fun ErrorCard(
    error: LoadingState.Error,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LoadingTheme.dimensions.errorCardRadius))
            .background(LoadingTheme.colors.error.copy(alpha = 0.1f))
            .border(
                width = 1.dp,
                color = LoadingTheme.colors.error.copy(alpha = 0.3f),
                shape = RoundedCornerShape(LoadingTheme.dimensions.errorCardRadius)
            )
            .padding(LoadingTheme.dimensions.errorCardPadding)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LoadingTheme.dimensions.itemSpacing)
        ) {
            error.iconRes()?.let { iconRes ->
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = LoadingTheme.colors.error,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                text = stringResource(error.res()),
                style = LoadingTheme.typography.errorText,
                color = LoadingTheme.colors.error,
                textAlign = TextAlign.Center
            )
        }
    }
}

@StringRes
private fun LoadingState.Error.res(): Int = when (this) {
    LoadingState.Error.DownloadFailed -> R.string.loading_error_download_failed
    LoadingState.Error.DecompressionFailed -> R.string.loading_error_decompression_failed
    LoadingState.Error.DatabaseWriteFailed -> R.string.loading_error_database_write_failed
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
    LoadingState.Error.DownloadFailed,
    LoadingState.Error.DecompressionFailed,
    LoadingState.Error.DatabaseWriteFailed,
    LoadingState.Error.GenericRuntime -> R.string.generic_retry
    else -> R.string.generic_download
}

@Preview("LandingCard - Normal")
@Preview("LandingCard - Normal (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun LandingCardPreview() {
    ChessGymTheme {
        LandingCard(
            onDownload = {},
            onDownloadConfirmation = {},
            onPermissionResponse = {},
            state = LoadingState.Ready(error = LoadingState.Error.None)
        )
    }
}

@Preview("LandingCard - With Error")
@Preview("LandingCard - With Error (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun LandingCardWithErrorPreview() {
    ChessGymTheme {
        LandingCard(
            onDownload = {},
            onDownloadConfirmation = {},
            onPermissionResponse = {},
            state = LoadingState.Ready(error = LoadingState.Error.NoInternet)
        )
    }
}

@Preview("ErrorCard")
@Composable
private fun ErrorCardPreview() {
    ChessGymTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ErrorCard(error = LoadingState.Error.NoInternet)
            ErrorCard(error = LoadingState.Error.NotEnoughDiskSpace)
            ErrorCard(error = LoadingState.Error.NoPermission)
        }
    }
}
