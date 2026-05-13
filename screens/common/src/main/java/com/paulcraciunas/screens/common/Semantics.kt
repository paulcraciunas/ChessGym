package com.paulcraciunas.screens.common

import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver

object SemanticsKeys {
    val IsDarkTheme = SemanticsPropertyKey<Boolean>("IsDarkTheme")
}

var SemanticsPropertyReceiver.isDarkTheme by SemanticsKeys.IsDarkTheme
