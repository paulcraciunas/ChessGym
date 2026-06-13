package com.paulcraciunas.screens.loading.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.paulcraciunas.screens.loading.vm.DatabaseTier
import com.paulcraciunas.screens.loading.vm.LoadingState

@Composable
fun LoadingScreen(
    onDownload: () -> Unit,
    onTierSelected: (DatabaseTier) -> Unit,
    onDownloadConfirmation: (Boolean) -> Unit,
    onCrashConsentResponse: (Boolean) -> Unit,
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
                onTierSelected = onTierSelected,
                onDownloadConfirmation = onDownloadConfirmation,
                onCrashConsentResponse = onCrashConsentResponse,
                state = uiState
            )

            is LoadingState.Downloading -> DownloadProgressCard(
                progress = uiState.progress,
                factIndex = uiState.factIndex,
            )
            is LoadingState.Complete -> DownloadProgressCard(
                progress = LoadingState.Downloading.Progress(download = 100, unpack = 100),
            )
        }
    }
}
