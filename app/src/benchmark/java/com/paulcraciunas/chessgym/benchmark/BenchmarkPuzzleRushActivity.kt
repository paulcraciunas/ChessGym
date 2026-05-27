package com.paulcraciunas.chessgym.benchmark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.puzzles.rush.ui.PuzzleRushScreen
import com.paulcraciunas.screens.puzzles.rush.vm.PuzzleRushViewModel
import com.paulcraciunas.settings.application.api.AppSettings

class BenchmarkPuzzleRushActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChessGymTheme {
                BenchmarkPuzzleRush(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics { testTagsAsResourceId = true },
                )
            }
        }
    }
}

@Composable
private fun BenchmarkPuzzleRush(modifier: Modifier = Modifier) {
    val dependencies: BenchmarkPuzzleRushDependencies = remember { BenchmarkPuzzleRushDependencies() }
    val viewModel: PuzzleRushViewModel = viewModel(
        factory = BenchmarkPuzzleRushViewModelFactory(dependencies),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings: AppSettings by dependencies.appSettingsRepository.appSettings.collectAsStateWithLifecycle(
        initialValue = AppSettings.default(),
    )
    PuzzleRushScreen(
        uiState = uiState,
        showBorders = settings.showBorders,
        highlightLegalMoves = settings.highlightLegalMoves,
        enableAnimations = settings.enableAnimations,
        interactions = viewModel,
        modifier = modifier,
    )
}
