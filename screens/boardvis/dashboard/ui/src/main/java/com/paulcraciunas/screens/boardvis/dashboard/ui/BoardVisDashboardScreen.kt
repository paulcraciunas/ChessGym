package com.paulcraciunas.screens.boardvis.dashboard.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.boardvis.dashboard.vm.BoardVisDashboardUiState
import com.paulcraciunas.screens.boardvis.dashboard.vm.BoardVisMode
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.TopLevelAppBar
import com.paulcraciunas.screens.common.controls.FindTheSquareCard
import com.paulcraciunas.screens.common.controls.Header
import com.paulcraciunas.screens.common.controls.MoveThePieceCard
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardVisDashboardScreen(
    state: BoardVisDashboardUiState,
    onModeSelected: (BoardVisMode) -> Unit,
    modifier: Modifier = Modifier,
    onDrawerToggle: () -> Unit = {},
) {
    Scaffold(
        topBar = { TopLevelAppBar(onHome = onDrawerToggle) },
        modifier = modifier.testTag { BoardVisDashboardTags.SCREEN },
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
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            horizontal = Design.dimensions.spacing.gut,
            vertical = Design.dimensions.spacing.xgut,
        ),
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.lg),
        modifier = modifier
            .fillMaxSize()
            .background(Design.colors.primarySoft),
    ) {
        item {
            Header(
                eyebrowRes = R.string.boardvis_dashboard_eyebrow,
                titleRes = R.string.boardvis_dashboard_title,
                subtitleRes = R.string.boardvis_dashboard_subtitle,
            )
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
