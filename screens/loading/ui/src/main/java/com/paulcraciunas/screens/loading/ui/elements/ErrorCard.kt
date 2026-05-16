package com.paulcraciunas.screens.loading.ui.elements

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.loading.vm.LoadingState

private val ERROR_SLOT_MIN_HEIGHT = 72.dp

@Composable
internal fun ErrorCard(
    error: LoadingState.Error,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = ERROR_SLOT_MIN_HEIGHT),
        contentAlignment = Alignment.Center,
    ) {
        if (error != LoadingState.Error.None) {
            ErrorCardDetails(error = error)
        }
    }
}

@Composable
private fun ErrorCardDetails(
    error: LoadingState.Error,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Design.colors.danger.copy(alpha = 0.1f), Design.shapes.buttonOutline)
            .padding(Design.dimensions.spacing.xxl),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.lg),
            modifier = Modifier.fillMaxWidth(),
        ) {
            error.iconRes()?.let { iconRes ->
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = Design.colors.danger,
                    modifier = Modifier.size(Design.dimensions.sizes.icon),
                )
            }
            Text(
                text = stringResource(error.res()),
                style = Design.typography.bodyMedium,
                color = Design.colors.danger,
                textAlign = TextAlign.Center,
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
    LoadingState.Error.ConsentRequired -> R.string.loading_error_consent_required
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
