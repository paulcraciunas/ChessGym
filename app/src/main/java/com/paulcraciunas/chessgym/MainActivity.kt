package com.paulcraciunas.chessgym

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paulcraciunas.chessgym.navigation.NavGraph
import com.paulcraciunas.chessgym.navigation.NavGraphViewModel
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.settings.application.api.AppSettings
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val navGraphViewModel: NavGraphViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Keep splash screen until app settings check is complete
        splashScreen.setKeepOnScreenCondition {
            navGraphViewModel.uiState.value.isLoading
        }

        enableEdgeToEdge()
        setContent {
            val navState by navGraphViewModel.uiState.collectAsStateWithLifecycle()
            val isDarkTheme = when (navState.lightMode) {
                AppSettings.LightMode.Light -> false
                AppSettings.LightMode.Dark -> true
                AppSettings.LightMode.System -> isSystemInDarkTheme()
            }
            ChessGymTheme(darkMode = isDarkTheme) {
                NavGraph(Modifier.fillMaxSize())
            }
        }
    }
}
