package com.paulcraciunas.chessgym.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paulcraciunas.chessgym.main.MainScreen
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

    // short circuit
    val isProvisioned = remember { uiState.puzzlesDownloaded }
    if (isProvisioned) {
        MainScreen(modifier = modifier)
        return
    }

    var phase by remember { mutableStateOf(TransitionPhase.Loading) }
    LaunchedEffect(uiState.puzzlesDownloaded) {
        if (uiState.puzzlesDownloaded && phase == TransitionPhase.Loading) {
            phase = TransitionPhase.FadingIn
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (phase) {
            TransitionPhase.Loading,
            TransitionPhase.FadingIn -> ShowLoadingScreen()
            TransitionPhase.FadingOut,
            TransitionPhase.Complete -> MainScreen(modifier = Modifier.fillMaxSize())
        }
        if (phase == TransitionPhase.FadingIn || phase == TransitionPhase.FadingOut) {
            ChessboardOverlay(
                onFullCoverageReached = { phase = TransitionPhase.FadingOut },
                onAnimationComplete = { phase = TransitionPhase.Complete }
            )
        }
    }
}

@Composable
private fun ShowLoadingScreen() {
    val vm: LoadingViewModel = hiltViewModel()
    val loadingState by vm.uiState.collectAsStateWithLifecycle()
    LoadingScreen(
        onDownload = vm::onDownload,
        onTierSelected = vm::onTierSelected,
        onDownloadConfirmation = vm::onDownloadConfirmation,
        onCrashConsentResponse = vm::onCrashConsentResponse,
        uiState = loadingState,
    )
}

private enum class TransitionPhase {
    Loading,
    FadingIn,
    FadingOut,
    Complete,
}