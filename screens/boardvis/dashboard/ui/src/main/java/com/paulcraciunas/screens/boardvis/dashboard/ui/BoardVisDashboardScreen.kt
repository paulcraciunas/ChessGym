package com.paulcraciunas.screens.boardvis.dashboard.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.boardvis.dashboard.vm.BoardVisDashboardUiState
import com.paulcraciunas.screens.boardvis.dashboard.vm.BoardVisMode
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.AppBarAlignment
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.controls.FindTheSquareCard
import com.paulcraciunas.screens.common.controls.MoveThePieceCard
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
fun BoardVisDashboardScreen(
    state: BoardVisDashboardUiState,
    onModeSelected: (BoardVisMode) -> Unit,
    modifier: Modifier = Modifier,
    onDrawerToggle: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            AppBar(
                titleAlign = AppBarAlignment.Center,
                navButton = { Home(onClick = onDrawerToggle) }
            )
        },
        modifier = modifier.testTag { BoardVisDashboardTags.SCREEN }
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingContent(Modifier.padding(innerPadding))
            else -> DashboardContent(
                state = state,
                onModeSelected = onModeSelected,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun DashboardContent(
    state: BoardVisDashboardUiState,
    onModeSelected: (BoardVisMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                horizontal = 20.dp,
                vertical = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                DashboardHeader()
            }

            item {
                FindTheSquareCard(
                    highScore = state.findSquareHighScore,
                    onClick = { onModeSelected(BoardVisMode.FindTheSquare) },
                    modifier = Modifier.testTag { BoardVisDashboardTags.Cards.FIND_THE_SQUARE },
                )
            }

            item {
                MoveThePieceCard(
                    onClick = { onModeSelected(BoardVisMode.MoveThePiece) },
                    modifier = Modifier.testTag { BoardVisDashboardTags.Cards.MOVE_THE_PIECE },
                )
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.boardvis_dashboard_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.boardvis_dashboard_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview("BoardVisDashboard")
@Preview("BoardVisDashboard (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun BoardVisDashboardScreenPreview() {
    ChessGymTheme {
        BoardVisDashboardScreen(
            state = BoardVisDashboardUiState(
                findSquareHighScore = 35,
                isLoading = false
            ),
            onModeSelected = {},
        )
    }
}
