package com.paulcraciunas.screens.common

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag

@Stable
inline fun Modifier.testTag(crossinline tag: () -> String): Modifier =
    if (BuildConfig.DEBUG || BuildConfig.ENABLE_TEST_TAGS) {
        semantics(properties = { testTag = tag() })
    } else {
        this
    }
