package com.paulcraciunas.screens.common

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class UiSettings(
    val lightMode: Mode,
    val autoPromote: Boolean,
    val autoNextPuzzle: Boolean,
    val showBorders: Boolean,
    val enableVibrations: Boolean,
    val highlightLegalMoves: Boolean,
    val enableAnimations: Boolean,
    val playSounds: Boolean,
) {
    enum class Mode {
        Light,
        Dark,
        System
    }

    companion object {
        fun default() = UiSettings(
            lightMode = Mode.System,
            autoPromote = true,
            autoNextPuzzle = true,
            showBorders = true,
            enableVibrations = true,
            highlightLegalMoves = true,
            enableAnimations = true,
            playSounds = true,
        )
    }
}

val LocalUiSettings = staticCompositionLocalOf<UiSettings> {
    error("ChessGymTheme not provided. Wrap your content in ChessGymTheme { ... }.")
}
