package com.paulcraciunas.chessgym.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.ChildAppBar
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard
import com.paulcraciunas.screens.common.controls.CapturedPieces
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.dialogs.PromotionDialog
import com.paulcraciunas.screens.common.model.PuzzleData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugPuzzleScreen(
    uiState: DebugPuzzleUiState,
    onNavigateBack: () -> Unit,
    onLoadPuzzle: (Int) -> Unit,
    onSquareClicked: (Locus) -> Unit,
    onPromote: (Piece) -> Unit,
) {
    var puzzleIdText by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = { ChildAppBar(onBack = onNavigateBack, title = "Debug: Load Puzzle") },
        containerColor = Design.colors.primarySoft,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
        ) {
            when (uiState) {
                is DebugPuzzleUiState.Loading -> LoadingContent(modifier = Modifier.weight(1f))
                is DebugPuzzleUiState.Error -> ErrorContent(message = uiState.message, modifier = Modifier.weight(1f))
                is DebugPuzzleUiState.Idle -> IdleContent(modifier = Modifier.weight(1f))
                is DebugPuzzleUiState.BoardState -> BoardContent(
                    uiState = uiState,
                    onSquareClicked = onSquareClicked,
                    onPromote = onPromote,
                    modifier = Modifier.weight(1f),
                )
            }
            PuzzleIdInput(
                puzzleIdText = puzzleIdText,
                onPuzzleIdChanged = { puzzleIdText = it },
                onLoadPuzzle = {
                    puzzleIdText.toIntOrNull()?.let { onLoadPuzzle(it) }
                },
                isLoadEnabled = puzzleIdText.toIntOrNull() != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun BoardContent(
    uiState: DebugPuzzleUiState.BoardState,
    onSquareClicked: (Locus) -> Unit,
    onPromote: (Piece) -> Unit,
    modifier: Modifier = Modifier,
) {
    val data: PuzzleData = uiState.data
    val isFinished = uiState is DebugPuzzleUiState.Finished

    Column(modifier = modifier) {
        CapturedPieces(capturedPieces = data.captured.byOpponent, side = data.player, modifier = Modifier.fillMaxWidth())
        ChessBoard(
            board = data.boardData,
            orientation = BoardOrientation.fromSide(data.player),
            onClick = if (!isFinished) onSquareClicked else { _ -> },
            modifier = Modifier.fillMaxWidth()
        )
        CapturedPieces(capturedPieces = data.captured.byPlayer, side = data.player.other(), modifier = Modifier.fillMaxWidth())
        if (isFinished) {
            Text(
                text = if (uiState.isSuccess) "Puzzle solved!" else "Puzzle failed",
                style = MaterialTheme.typography.titleMedium,
                color = if (uiState.isSuccess) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(8.dp),
            )
        }

        if (uiState is DebugPuzzleUiState.Playing && uiState.promotion != null) {
            PromotionDialog(
                side = data.player,
                onPieceChosen = onPromote,
            )
        }
    }
}

@Composable
private fun IdleContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Enter a puzzle ID below to load and test it",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun ErrorContent(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun PuzzleIdInput(
    puzzleIdText: String,
    onPuzzleIdChanged: (String) -> Unit,
    onLoadPuzzle: () -> Unit,
    isLoadEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = puzzleIdText,
            onValueChange = onPuzzleIdChanged,
            label = { Text("Puzzle ID") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onLoadPuzzle,
            enabled = isLoadEnabled,
        ) {
            Text("Load")
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}
