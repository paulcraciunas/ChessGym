package com.paulcraciunas.screens.tools.dashboard.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.AppBarAlignment
import com.paulcraciunas.screens.common.Footer
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.backgroundColor
import com.paulcraciunas.screens.common.controls.AnalysisCard
import com.paulcraciunas.screens.common.controls.ClockCard
import com.paulcraciunas.screens.common.controls.ImportGameCard
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.tools.dashboard.vm.ToolsDashboardUiState
import com.paulcraciunas.screens.tools.dashboard.vm.ToolsMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsDashboardScreen(
    state: ToolsDashboardUiState,
    onModeSelected: (ToolsMode) -> Unit,
    modifier: Modifier = Modifier,
    onDrawerToggle: () -> Unit = {},
) {
    val bgColor = MaterialTheme.colorScheme.background
    Scaffold(
        topBar = {
            AppBar(
                titleAlign = AppBarAlignment.Center,
                navButton = { Home(onClick = onDrawerToggle) }
            )
        },
        modifier = modifier
            .testTag { ToolsDashboardTags.SCREEN }
            .semantics { this.backgroundColor = bgColor }
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingContent(Modifier.padding(innerPadding))
            else -> DashboardContent(
                onModeSelected = onModeSelected,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun DashboardContent(
    onModeSelected: (ToolsMode) -> Unit,
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            DashboardHeader()
        }

        item {
            ClockCard(
                onClick = { onModeSelected(ToolsMode.Clock) },
                modifier = Modifier.testTag { ToolsDashboardTags.Cards.CLOCK },
            )
        }

        item {
            AnalysisCard(
                onClick = { onModeSelected(ToolsMode.Analysis) },
                modifier = Modifier.testTag { ToolsDashboardTags.Cards.ANALYSIS },
            )
        }

        item {
            ImportGameCard(
                onClick = { onModeSelected(ToolsMode.ImportGame) },
                modifier = Modifier.testTag { ToolsDashboardTags.Cards.IMPORT_GAME },
            )
        }

        item {
            Footer()
        }
    }
}

@Preview("ToolsDashboard")
@Preview("ToolsDashboard (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ToolsDashboardScreenPreview() {
    ChessGymTheme {
        ToolsDashboardScreen(
            state = ToolsDashboardUiState(isLoading = false),
            onModeSelected = {},
        )
    }
}
