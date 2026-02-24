package com.paulcraciunas.settings.application.api

data class AppSettings(
    val puzzlesDownloaded: Boolean,
    val totalPuzzleCount: Int,
    val maxPuzzleRating: Int,
    val minPuzzleRating: Int,
    val playSoundOnMove: Boolean,
    val preferredTheme: Theme,
    val lightMode: LightMode,
    val autoPromote: Boolean,
    val showBorders: Boolean,
    val enableVibrations: Boolean,
    val highlightLegalMoves: Boolean,
    val enableAnimations: Boolean,
) {
    enum class Theme {
        Wood,
        Grey
    }

    enum class LightMode {
        Light,
        Dark,
        System
    }
}