package com.paulcraciunas.screens.puzzles.dashboard.ui

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
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.AppBarAlignment
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.puzzles.dashboard.vm.PuzzleDashboardUiState
import com.paulcraciunas.screens.puzzles.dashboard.vm.PuzzleMode

@Composable
fun PuzzleDashboardScreen(
    state: PuzzleDashboardUiState,
    onPuzzleModeSelected: (PuzzleMode) -> Unit,
    modifier: Modifier = Modifier,
    onDrawerToggle: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            AppBar(titleAlign = AppBarAlignment.Center) {
                Home(onClick = onDrawerToggle)
            }
        },
        modifier = modifier
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingContent(Modifier.padding(innerPadding))
            else -> DashboardContent(
                state = state,
                onPuzzleModeSelected = onPuzzleModeSelected,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun DashboardContent(
    state: PuzzleDashboardUiState,
    onPuzzleModeSelected: (PuzzleMode) -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                DashboardHeader()
            }
            
            item {
                RatedPuzzleCard(
                    userRating = state.userRating,
                    onClick = { onPuzzleModeSelected(PuzzleMode.RatedPuzzle) }
                )
            }
            
            item {
                PuzzleRushCard(
                    onClick = { onPuzzleModeSelected(PuzzleMode.PuzzleRush) }
                )
            }

            item {
                PuzzleStreakCard(
                    // TODO Paul: integrate me
                    currentStreak = 0,
                    onClick = { onPuzzleModeSelected(PuzzleMode.PuzzleStreak) }
                )
            }
            
            item {
                FailedPuzzlesCard(
                    failedCount = state.failedPuzzlesCount,
                    onClick = { onPuzzleModeSelected(PuzzleMode.FailedPuzzles) }
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
            text = stringResource(R.string.puzzle_dashboard_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.puzzle_dashboard_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview("PuzzleDashboard")
@Preview("PuzzleDashboard (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PuzzleDashboardScreenPreview() {
    ChessGymTheme {
        PuzzleDashboardScreen(
            state = PuzzleDashboardUiState(
                userRating = 1547,
                failedPuzzlesCount = 12,
                isLoading = false
            ),
            onPuzzleModeSelected = {},
        )
    }
}
