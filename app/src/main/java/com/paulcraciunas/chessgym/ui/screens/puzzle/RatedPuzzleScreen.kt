package com.paulcraciunas.chessgym.ui.screens.puzzle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paulcraciunas.chessgym.ui.board.BoardOrientation
import com.paulcraciunas.chessgym.ui.board.ChessBoard
import com.paulcraciunas.chessgym.ui.model.BoardViewDataBuilder
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.global.resources.R

@Composable
internal fun RatedPuzzleScreen(
    modifier: Modifier = Modifier,
    viewModel: RatedPuzzleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.puzzle != null -> {
                PuzzleContent(
                    uiState = uiState,
                    onSquareClicked = viewModel::onSquareClicked,
                    onHint = viewModel::requestHint,
                    onAbandon = viewModel::abandonPuzzle,
                    onNext = viewModel::playAnotherPuzzle,
                )
            }
        }
    }
}

@Composable
private fun PuzzleContent(
    uiState: RatedPuzzleUiState,
    onSquareClicked: (at: Locus) -> Unit,
    onHint: () -> Unit,
    onAbandon: () -> Unit,
    onNext: () -> Unit,
) {
    val puzzle = uiState.puzzle ?: return

    // Puzzle Info
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.rated_puzzle_rating_title, puzzle.rating),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(
                    if (puzzle.player == Side.WHITE) R.string.rated_puzzle_white_to_move else R.string.rated_puzzle_black_to_move
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    // Chess Board
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            ChessBoard(
                board = puzzle.board.let { BoardViewDataBuilder().apply { loadBoard(it) }.build() },
                orientation = if (puzzle.player == Side.WHITE) BoardOrientation.White else BoardOrientation.Black,
                onClick = { rank, file -> onSquareClicked(Locus(file = file, rank = rank)) },
                modifier = Modifier.padding(8.dp)
            )
            Text(
                text = stringResource(R.string.rated_puzzle_moves, uiState.moves),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    // Action Buttons
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        OutlinedButton(
            onClick = onHint,
            enabled = !uiState.isCompleted,
        ) {
            Icon(
                painter = painterResource(R.drawable.lightbulb_icon),
                contentDescription = stringResource(R.string.rated_puzzle_hint_description)
            )
        }
        Button(
            onClick = onAbandon,
            enabled = !uiState.isCompleted,
        ) {
            Icon(
                painter = painterResource(R.drawable.flag_icon),
                contentDescription = stringResource(R.string.rated_puzzle_abandon_description)
            )
        }
        if (uiState.isCompleted) {
            Button(
                onClick = onNext,
            ) {
                Icon(
                    painter = painterResource(R.drawable.play_icon),
                    contentDescription = stringResource(R.string.rated_puzzle_next_description)
                )
            }
        }
    }
}
