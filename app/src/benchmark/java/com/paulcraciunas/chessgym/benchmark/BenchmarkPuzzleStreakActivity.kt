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
import com.paulcraciunas.settings.application.api.AppSettings
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.puzzles.streak.ui.PuzzleStreakScreen
import com.paulcraciunas.screens.puzzles.streak.ui.PuzzleStreakScreen2
import com.paulcraciunas.screens.puzzles.streak.vm.PuzzleStreakViewModel
import com.paulcraciunas.screens.puzzles.streak.vm.PuzzleStreakViewModel2

class BenchmarkPuzzleStreakActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val version: Int = intent.getIntExtra(EXTRA_VERSION, VERSION_2)
        setContent {
            ChessGymTheme {
                val rootModifier = Modifier
                    .fillMaxSize()
                    .semantics { testTagsAsResourceId = true }
                when (version) {
                    VERSION_1 -> BenchmarkPuzzleStreakV1(modifier = rootModifier)
                    else -> BenchmarkPuzzleStreakV2(modifier = rootModifier)
                }
            }
        }
    }

    companion object {
        const val EXTRA_VERSION: String = "version"
        const val VERSION_1: Int = 1
        const val VERSION_2: Int = 2
    }
}

@Composable
private fun BenchmarkPuzzleStreakV1(modifier: Modifier = Modifier) {
    val dependencies: BenchmarkDependencies = remember { BenchmarkDependencies() }
    val viewModel: PuzzleStreakViewModel = viewModel(factory = BenchmarkViewModelFactory.v1(dependencies))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings: AppSettings by dependencies.appSettingsRepository.appSettings.collectAsStateWithLifecycle(
        initialValue = AppSettings.default(),
    )
    PuzzleStreakScreen(
        uiState = uiState,
        showBorders = settings.showBorders,
        highlightLegalMoves = settings.highlightLegalMoves,
        enableAnimations = settings.enableAnimations,
        interactions = viewModel,
        modifier = modifier,
    )
}

@Composable
private fun BenchmarkPuzzleStreakV2(modifier: Modifier = Modifier) {
    val dependencies: BenchmarkDependencies = remember { BenchmarkDependencies() }
    val viewModel: PuzzleStreakViewModel2 = viewModel(factory = BenchmarkViewModelFactory.v2(dependencies))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PuzzleStreakScreen2(
        uiState = uiState,
        onSquareClicked = { viewModel.onSquareClicked(it) },
        onPromote = { viewModel.onPromote(it) },
        onHintRequested = { viewModel.onHintRequested() },
        onAbandon = { viewModel.onAbandon() },
        onAbandonConfirmed = { viewModel.onAbandonConfirmed() },
        onAbandonDismissed = { viewModel.onAbandonDismissed() },
        onNewStreak = { viewModel.onNewStreak() },
        onNextPuzzle = { viewModel.onNextPuzzle() },
        onDismissSummary = { viewModel.onDismissSummary() },
        modifier = modifier,
    )
}
