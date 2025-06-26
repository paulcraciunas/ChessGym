package com.paulcraciunas.screens.loading.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.paulcraciunas.screens.loading.vm.LoadingState

@Composable
fun LoadingScreen(
    onComplete: () -> Unit,
    onDownload: () -> Unit,
    onDownloadConfirmation: (Boolean) -> Unit,
    onPermissionReceived: (Boolean) -> Unit,
    uiState: LoadingState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is LoadingState.Ready -> LandingCard(
                onDownload = onDownload,
                onDownloadConfirmation = onDownloadConfirmation,
                onPermissionResponse = onPermissionReceived,
                state = uiState
            )

            is LoadingState.Downloading -> DownloadProgressCard(progress = uiState.progress)
            is LoadingState.Complete -> onComplete()
        }
    }
}
