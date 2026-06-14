package com.paulcraciunas.screens.data

import androidx.compose.runtime.Immutable

@Immutable
data class RemainingTime(
    val value: String = "03:00",
    val danger: Boolean = false,
)
