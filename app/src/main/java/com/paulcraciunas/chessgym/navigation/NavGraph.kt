package com.paulcraciunas.chessgym.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

    AnimatedContent(
        targetState = uiState.puzzlesDownloaded,
        transitionSpec = {
            fadeIn(animationSpec = tween(500)) + slideInHorizontally { it / 4 } togetherWith
                    fadeOut(animationSpec = tween(300)) + slideOutHorizontally { -it / 4 }
        },
        label = "nav_transition",
    ) { downloaded ->
        if (!downloaded) {
            val vm: LoadingViewModel = hiltViewModel()
            val loadingState by vm.uiState.collectAsStateWithLifecycle()
            LoadingScreen(
                onDownload = vm::onDownload,
                onTierSelected = vm::onTierSelected,
                onDownloadConfirmation = vm::onDownloadConfirmation,
                onPermissionReceived = vm::onPermissionReceived,
                onCrashConsentResponse = vm::onCrashConsentResponse,
                uiState = loadingState,
            )
        } else {
            MainScreen(modifier = modifier)
        }
    }
}
