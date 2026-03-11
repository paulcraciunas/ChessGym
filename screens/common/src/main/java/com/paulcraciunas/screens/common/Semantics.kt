package com.paulcraciunas.screens.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver

object SemanticsKeys {
    val BackgroundColor = SemanticsPropertyKey<Color>("BackgroundColor")
}

var SemanticsPropertyReceiver.backgroundColor by SemanticsKeys.BackgroundColor
