package com.paulcraciunas.chessgym

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                        val boardState by gameViewModel.boardState.collectAsStateWithLifecycle()
                        val orientation by gameViewModel.orientationState.collectAsStateWithLifecycle()
                        ChessBoard(
                            board = boardState,
                            orientation = orientation,
                            onClick = { rank, file -> gameViewModel.onClick(rank, file) },
                            modifier = Modifier.padding(innerPadding)
                        )
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
