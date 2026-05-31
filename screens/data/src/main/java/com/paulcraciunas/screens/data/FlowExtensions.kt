package com.paulcraciunas.screens.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

inline fun <Base, reified Specific: Base> MutableStateFlow<Base>.updateAs(crossinline function: (Specific) -> Base) = update {
    if (it is Specific) function(it) else it
}

inline fun <reified Specific> MutableStateFlow<*>.runAs(block: (Specific) -> Unit) {
    val state = value
    if (state !is Specific) return
    block(state)
}
