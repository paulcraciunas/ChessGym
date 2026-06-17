package com.paulcraciunas.chessgym

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paulcraciunas.chessgym.navigation.NavGraph
import com.paulcraciunas.chessgym.navigation.NavGraphViewModel
import com.paulcraciunas.screens.common.LocalUiSettings
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.settings.application.api.AppSettings
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val navGraphViewModel: NavGraphViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Keep splash screen until app settings check is complete
        splashScreen.setKeepOnScreenCondition {
            navGraphViewModel.uiState.value.isLoading
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            val navState by navGraphViewModel.uiState.collectAsStateWithLifecycle()
            val isDarkTheme = when (navState.lightMode) {
                AppSettings.LightMode.Light -> false
                AppSettings.LightMode.Dark -> true
                AppSettings.LightMode.System -> isSystemInDarkTheme()
            }
            LaunchedEffect(isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = insetStyle(isDarkTheme),
                    navigationBarStyle = insetStyle(isDarkTheme)
                )
            }
            ChessGymTheme(darkMode = isDarkTheme) {
                CompositionLocalProvider(LocalUiSettings provides navState.appSettings) {
                    NavGraph(Modifier.fillMaxSize())
                }
            }
        }
    }

    private fun insetStyle(isDarkTheme: Boolean): SystemBarStyle = if (isDarkTheme) {
        SystemBarStyle.dark(Color.Transparent.toArgb())
    } else {
        SystemBarStyle.light(Color.Transparent.toArgb(), Color.Transparent.toArgb())
    }
}
