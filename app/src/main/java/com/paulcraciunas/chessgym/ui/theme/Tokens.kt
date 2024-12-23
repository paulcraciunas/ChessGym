package com.paulcraciunas.chessgym.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class Tokens(
    val scaleFactorPawn: Float = error("Missing token"),
    val scaleFactorDefault: Float = error("Missing token"),
)

val DefaultTokens = Tokens(
    scaleFactorPawn = 0.65f,
    scaleFactorDefault = 0.8f,
)

val LocalTokens = staticCompositionLocalOf { Tokens() }

val GlobalTokens: Tokens
    @Composable
    @ReadOnlyComposable
    get() = LocalTokens.current
