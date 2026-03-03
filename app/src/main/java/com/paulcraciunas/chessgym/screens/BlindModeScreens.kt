package com.paulcraciunas.chessgym.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paulcraciunas.screens.blindmode.ui.BlindModeScreen
import com.paulcraciunas.screens.blindmode.vm.BlindModeViewModel

@Composable
internal fun BlindMode(
    showBorders: Boolean,
    highlightLegalMoves: Boolean,
    onDrawerToggle: () -> Unit,
) {
    val vm: BlindModeViewModel = hiltViewModel()
    val blindModeState by vm.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { vm.onStop() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { vm.onStart() }

    BlindModeScreen(
        uiState = blindModeState,
        showBorders = showBorders,
        highlightLegalMoves = highlightLegalMoves,
        onDrawerToggle = onDrawerToggle,
        interactions = vm,
    )
}
