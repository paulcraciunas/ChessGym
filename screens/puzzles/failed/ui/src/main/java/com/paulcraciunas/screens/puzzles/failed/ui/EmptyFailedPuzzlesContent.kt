package com.paulcraciunas.screens.puzzles.failed.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.puzzles.failed.vm.FailedPuzzlesUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EmptyFailedPuzzlesContent(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(R.string.puzzle_mode_failed_title),
                navButton = { Back(onClick = onNavigateBack) },
            )
        },
        modifier = modifier.testTag { FailedPuzzlesScreenTags.EMPTY },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Design.colors.primarySoft)
                .padding(innerPadding)
                .padding(Design.dimensions.spacing.section),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.failed_puzzles_empty_title),
                style = Design.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = Design.colors.ink,
            )
            ChessGymSpacer(size = SpacerSize.XXLARGE)
            Text(
                text = stringResource(R.string.failed_puzzles_empty_description),
                style = Design.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Design.colors.inkSoft,
            )
        }
    }
}

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun EmptyPreview() {
    ChessGymTheme {
        FailedPuzzlesScreen(
            uiState = FailedPuzzlesUiState.Empty,
            showBorders = true,
            highlightLegalMoves = false,
            enableAnimations = false
        )
    }
}
