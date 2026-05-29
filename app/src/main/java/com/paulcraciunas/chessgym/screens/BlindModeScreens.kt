package com.paulcraciunas.chessgym.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paulcraciunas.screens.blindmode.ui.BlindModeScreen
import com.paulcraciunas.screens.blindmode.vm.BlindModeViewModel

@Composable
internal fun BlindMode(onDrawerToggle: () -> Unit) {
    val vm: BlindModeViewModel = hiltViewModel()
    val blindModeState by vm.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { vm.onStop() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { vm.onStart() }

    BlindModeScreen(
        uiState = blindModeState,
        onDrawerToggle = onDrawerToggle,
        onTrainingModeToggled = vm::onTrainingModeToggled,
        onSideSelected = vm::onSideSelected,
        onPlayClicked = vm::onPlayClicked,
        onSquareClicked = vm::onSquareClicked,
        onPromote = vm::onPromote,
        onResign = vm::onResign,
        onReveal = vm::onReveal,
        onPlayAgain = vm::onPlayAgain,
        onBackPressed = vm::onBackPressed,
        onAbandonConfirmed = vm::onAbandonConfirmed,
        onAbandonDismissed = vm::onAbandonDismissed,
    )
}
