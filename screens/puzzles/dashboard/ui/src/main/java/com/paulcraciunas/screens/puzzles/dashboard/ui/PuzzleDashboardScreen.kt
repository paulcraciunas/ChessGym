package com.paulcraciunas.screens.puzzles.dashboard.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.AppBarAlignment
import com.paulcraciunas.screens.common.Footer
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.backgroundColor
import com.paulcraciunas.screens.common.controls.FailedPuzzlesCard
import com.paulcraciunas.screens.common.controls.PuzzleRushCard
import com.paulcraciunas.screens.common.controls.PuzzleStreakCard
import com.paulcraciunas.screens.common.controls.RatedPuzzleCard
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.puzzles.dashboard.vm.PuzzleDashboardUiState
import com.paulcraciunas.screens.puzzles.dashboard.vm.PuzzleMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzleDashboardScreen(
    state: PuzzleDashboardUiState,
    onPuzzleModeSelected: (PuzzleMode) -> Unit,
    modifier: Modifier = Modifier,
    onDrawerToggle: () -> Unit = {},
) {
    val bgColor = Design.colors.primarySoft
    Scaffold(
        topBar = {
            AppBar(
                titleAlign = AppBarAlignment.Center,
                navButton = { Home(onClick = onDrawerToggle) }
            )
        },
        modifier = modifier
            .testTag { PuzzleDashboardTags.SCREEN }
            .semantics { this.backgroundColor = bgColor }
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
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            horizontal = 20.dp,
            vertical = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxSize()
            .background(Design.colors.primarySoft)
    ) {
        item {
            DashboardHeader()
        }

        item {
            RatedPuzzleCard(
                userRating = state.userRating,
                onClick = { onPuzzleModeSelected(PuzzleMode.RatedPuzzle) },
                modifier = Modifier.testTag { PuzzleDashboardTags.Cards.RATED_PUZZLE },
            )
        }

        item {
            PuzzleRushCard(
                onClick = { onPuzzleModeSelected(PuzzleMode.PuzzleRush) },
                modifier = Modifier.testTag { PuzzleDashboardTags.Cards.PUZZLE_RUSH },
            )
        }

        item {
            PuzzleStreakCard(
                currentStreak = state.currentStreakCount,
                onClick = { onPuzzleModeSelected(PuzzleMode.PuzzleStreak) },
                modifier = Modifier.testTag { PuzzleDashboardTags.Cards.PUZZLE_STREAK },
            )
        }

        item {
            FailedPuzzlesCard(
                failedCount = state.failedPuzzlesCount,
                onClick = { onPuzzleModeSelected(PuzzleMode.FailedPuzzles) },
                modifier = Modifier.testTag { PuzzleDashboardTags.Cards.FAILED_PUZZLES },
            )
        }

        item {
            Footer()
        }
    }
}

@Composable
private fun DashboardHeader(
    modifier: Modifier = Modifier,
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
                currentStreakCount = 7,
                isLoading = false
            ),
            onPuzzleModeSelected = {},
        )
    }
}
