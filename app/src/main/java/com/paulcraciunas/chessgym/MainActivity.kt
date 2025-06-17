package com.paulcraciunas.chessgym

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.chessgym.ui.board.BoardOrientation
import com.paulcraciunas.chessgym.ui.board.ChessBoard
import com.paulcraciunas.chessgym.ui.model.BoardViewDataBuilder
import com.paulcraciunas.chessgym.ui.theme.ChessGymTheme
import com.paulcraciunas.settings.user.UserSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var userSettings: UserSettings

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
                        Text(
                            text = stringResource(R.string.under_construction),
                            modifier = Modifier.padding(innerPadding)
                        )
                        CircularProgressIndicator()
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
