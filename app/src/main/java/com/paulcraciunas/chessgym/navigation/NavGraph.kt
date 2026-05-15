package com.paulcraciunas.chessgym.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.paulcraciunas.chessgym.MainScreen
import com.paulcraciunas.screens.loading.ui.LoadingScreen
import com.paulcraciunas.screens.loading.vm.LoadingViewModel

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    viewModel: NavGraphViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        return
    }

    if (!uiState.puzzlesDownloaded) {
        val vm: LoadingViewModel = hiltViewModel()
        val loadingState by vm.uiState.collectAsStateWithLifecycle()
        LoadingScreen(
            onComplete = { },
            onDownload = vm::onDownload,
            onDownloadConfirmation = vm::onDownloadConfirmation,
            onPermissionReceived = vm::onPermissionReceived,
            onCrashConsentResponse = vm::onCrashConsentResponse,
            uiState = loadingState,
        )
    } else {
        MainScreen(modifier = modifier)
    }
}
