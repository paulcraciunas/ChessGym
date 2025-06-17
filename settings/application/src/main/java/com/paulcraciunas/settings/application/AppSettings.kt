package com.paulcraciunas.settings.application

data class AppSettings(
    val puzzlesDownloaded: Boolean,
    val playSoundOnMove: Boolean,
    val preferredTheme: Theme,
    val lightMode: LightMode,
    val autoPromote: Boolean,
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
