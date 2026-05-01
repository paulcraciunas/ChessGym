package com.paulcraciunas.screens.common.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class AlphaTokens(
    val highEmphasis: Float = error("Missing token"),
    val mediumEmphasis: Float = error("Missing token"),
    val disabled: Float = error("Missing token"),
)

val DefaultAlpha = AlphaTokens(
    highEmphasis = 1f,
    mediumEmphasis = 0.6f,
    disabled = 0.38f,
)

val LocalAlpha = staticCompositionLocalOf { AlphaTokens() }

val GlobalAlphaTokens: AlphaTokens
    @Composable
    @ReadOnlyComposable
    get() = LocalAlpha.current

@Stable
inline val Boolean.alpha: Float
    @Composable
    get() = if (this) GlobalAlphaTokens.highEmphasis else GlobalAlphaTokens.mediumEmphasis

