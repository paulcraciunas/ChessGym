package com.paulcraciunas.settings.application.api

data class AppSettings(
    val puzzlesDownloaded: Boolean,
    val totalPuzzleCount: Int,
    val maxPuzzleRating: Int,
    val minPuzzleRating: Int,
    val playSoundOnMove: Boolean,
    val lightMode: LightMode,
    val autoPromote: Boolean,
    val autoNextPuzzle: Boolean,
    val showBorders: Boolean,
    val enableVibrations: Boolean,
    val highlightLegalMoves: Boolean,
    val enableAnimations: Boolean,
    val crashReportingConsent: Boolean,
) {
    enum class LightMode {
        Light,
        Dark,
        System
    }

    companion object {
        fun default() = AppSettings(
            puzzlesDownloaded = false,
            totalPuzzleCount = 0,
            maxPuzzleRating = 0,
            minPuzzleRating = 0,
            playSoundOnMove = true,
            lightMode = LightMode.System,
            autoPromote = true,
            autoNextPuzzle = false,
            showBorders = true,
            enableVibrations = true,
            highlightLegalMoves = true,
            enableAnimations = true,
            crashReportingConsent = false
        )
    }
}
