package com.paulcraciunas.chessgym.ui.screens.loading

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
internal fun LoadingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoadingViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsState()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState.value) {
            is LoadingState.Loading -> CircularProgressIndicator() //TODO Paul: this will be replaced with splash screen
            is LoadingState.Ready -> LandingCard(
                onDownload = viewModel::onDownload,
                onDownloadConfirmation = viewModel::onDownloadConfirmation,
                onPermissionResponse = viewModel::onPermissionReceived,
                state = state
            )

            is LoadingState.Downloading -> DownloadProgressCard(progress = state.progress)
            is LoadingState.Complete -> CompletionCard(onComplete)
        }
    }
}
