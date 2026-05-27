package com.paulcraciunas.chessgym.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paulcraciunas.screens.blindmode.ui.BlindModeScreen
import com.paulcraciunas.screens.blindmode.vm.BlindModeViewModel
import com.paulcraciunas.screens.common.LocalUiSettings

@Composable
internal fun BlindMode(onDrawerToggle: () -> Unit) {
    val settings = LocalUiSettings.current
    val vm: BlindModeViewModel = hiltViewModel()
    val blindModeState by vm.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { vm.onStop() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { vm.onStart() }

    BlindModeScreen(
        uiState = blindModeState,
        showBorders = settings.showBorders,
        highlightLegalMoves = settings.highlightLegalMoves,
        enableAnimations = settings.enableAnimations,
        onDrawerToggle = onDrawerToggle,
        interactions = vm,
    )
}
