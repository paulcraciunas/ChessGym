package com.paulcraciunas.chessgym

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paulcraciunas.chessgym.ui.GameViewModel
import com.paulcraciunas.chessgym.ui.board.BoardOrientation
import com.paulcraciunas.chessgym.ui.board.ChessBoard
import com.paulcraciunas.chessgym.ui.model.BoardViewDataBuilder
import com.paulcraciunas.chessgym.ui.theme.ChessGymTheme

class MainActivity : ComponentActivity() {
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChessGymTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val boardState by gameViewModel.boardState.collectAsStateWithLifecycle()
                    ChessBoard(
                        board = boardState,
                        orientation = BoardOrientation.White,
                        onClick = { rank, file -> gameViewModel.onClick(rank, file) },
                        modifier = Modifier.padding(innerPadding)
                    )
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
                onClick = { _, _ ->  },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}