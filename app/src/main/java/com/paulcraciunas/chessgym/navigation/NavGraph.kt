package com.paulcraciunas.chessgym.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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

    val isFirstTimeUser = remember { !uiState.puzzlesDownloaded }
    if (!isFirstTimeUser) {
        MainScreen(modifier = modifier)
        return
    }

    val transitionProgress = remember { Animatable(0f) }
    var showMainScreen by remember { mutableStateOf(false) }
    var isTransitioning by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.puzzlesDownloaded) {
        if (uiState.puzzlesDownloaded) {
            isTransitioning = true
            transitionProgress.animateTo(1f, tween(durationMillis = CLOSE_DURATION_MS))
            showMainScreen = true
            transitionProgress.animateTo(2f, tween(durationMillis = OPEN_DURATION_MS))
            transitionProgress.snapTo(0f)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (isTransitioning || showMainScreen) {
            MainScreen(modifier = Modifier)
        }

        if (!showMainScreen) {
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

        if (transitionProgress.value > 0f) {
            FadeSplashOverlay(progress = transitionProgress.value)
        }
    }
}

private const val CLOSE_DURATION_MS = 800
private const val OPEN_DURATION_MS = 1000
