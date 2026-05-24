package com.paulcraciunas.screens.common

import androidx.compose.runtime.staticCompositionLocalOf
import com.paulcraciunas.settings.application.api.AppSettings

val LocalAppSettings = staticCompositionLocalOf { AppSettings.default() }
