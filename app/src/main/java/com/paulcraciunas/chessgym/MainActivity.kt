package com.paulcraciunas.chessgym

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paulcraciunas.chessgym.ui.GameViewModel
import com.paulcraciunas.chessgym.ui.board.BoardOrientation
import com.paulcraciunas.chessgym.ui.board.ChessBoard
import com.paulcraciunas.chessgym.ui.model.BoardViewDataBuilder
import com.paulcraciunas.chessgym.ui.theme.ChessGymTheme
import com.paulcraciunas.settings.user.UserSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

//TODO Paul: This is only temporary. Delete this and reimplement it properly
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var userSettings: UserSettings
    private val gameViewModel: GameViewModel by viewModels()
//    private val landingViewModel: LandingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO Paul: move this somewhere else
        // Also maybe handle the result?!
//        if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                ActivityCompat.requestPermissions(this, arrayOf(POST_NOTIFICATIONS), 1)
//            }
//        }
        enableEdgeToEdge()
        setContent {
            ChessGymTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                ModalNavigationDrawer(
                    drawerContent = {
                        AppDrawer(
                            drawerState = drawerState,
                            onSignIn = {},
                            onHome = {},
                            onSettings = {},
                            onAbout = {},
                            closeDrawer = { scope.launch { drawerState.close() } }
                        )
                    },
                    drawerState = drawerState
                ) {
                    Scaffold(
                        topBar = {
                            AppBar(
                                onHome = {
                                    scope.launch {
                                        if (drawerState.isClosed) {
                                            drawerState.open()
                                        } else {
                                            drawerState.close()
                                        }
                                    }
                                }
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        // Trigger loading once (LaunchedEffect ensures this runs only once per key)
                        LaunchedEffect(1200) {
                            gameViewModel.loadPuzzle(1200)
                        }
                        val puzzle by gameViewModel.puzzleState.collectAsState()
                        val boardState by gameViewModel.boardState.collectAsStateWithLifecycle()
                        val orientation by gameViewModel.orientationState.collectAsStateWithLifecycle()

                        if (puzzle != null) {
                            ChessBoard(
                                board = boardState,
                                orientation = orientation,
                                onClick = { rank, file -> gameViewModel.onClick(rank, file) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        } else {
                            CircularProgressIndicator()
                        }

//                        LandingScreen(
//                            settings = userSettings,
//                            viewModel = landingViewModel,
//                            modifier = Modifier.padding(innerPadding)
//                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ChessGymTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            ChessBoard(
                board = BoardViewDataBuilder().build(),
                orientation = BoardOrientation.White,
                onClick = { _, _ -> },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
