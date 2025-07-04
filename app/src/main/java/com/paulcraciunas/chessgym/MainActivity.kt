package com.paulcraciunas.chessgym

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.paulcraciunas.chessgym.navigation.NavGraph
import com.paulcraciunas.chessgym.navigation.NavGraphViewModel
import com.paulcraciunas.screens.common.theme.ChessGymTheme
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
            ChessGymTheme {
                NavGraph(Modifier.fillMaxSize())
            }
        }
    }
}
