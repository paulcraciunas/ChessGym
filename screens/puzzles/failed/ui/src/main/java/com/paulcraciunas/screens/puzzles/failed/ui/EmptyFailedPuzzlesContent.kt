package com.paulcraciunas.screens.puzzles.failed.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.LocalUiSettings
import com.paulcraciunas.screens.common.UiSettings
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.puzzles.failed.vm.FailedPuzzlesUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EmptyFailedPuzzlesContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(Design.dimensions.spacing.section),
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

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun EmptyPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            FailedPuzzlesScreen(
                uiState = FailedPuzzlesUiState.Empty,
            )
        }
    }
}
