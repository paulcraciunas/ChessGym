package com.paulcraciunas.screens.common.extensions

import androidx.compose.runtime.Stable

@Stable
inline val Boolean.alpha: Float
    get() = if (this) 1f else 0.6f
