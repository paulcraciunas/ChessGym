package com.paulcraciunas.screens.puzzles.rush.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.screens.common.model.PuzzleResult
import com.paulcraciunas.screens.common.model.PuzzleData
import com.paulcraciunas.screens.common.model.PuzzleViewModelHelper

@Immutable
sealed class PuzzleRushUiState {
    data object Loading : PuzzleRushUiState()
    data object Failed : PuzzleRushUiState()

    @Immutable
    abstract class BoardState : PuzzleRushUiState() {
        abstract val data: PuzzleData
        abstract val timeRemainingSeconds: Int
        abstract val results: List<PuzzleResult>
    }

    @Immutable
    data class Ready(
        override val data: PuzzleData,
        override val timeRemainingSeconds: Int,
        override val results: List<PuzzleResult> = emptyList(),
    ) : BoardState()

    @Immutable
    data class Playing(
        override val data: PuzzleData,
        override val timeRemainingSeconds: Int,
        override val results: List<PuzzleResult>,
        val promotion: PuzzleViewModelHelper.Promotion2?,
    ) : BoardState()

    @Immutable
    data class Finished(
        override val data: PuzzleData,
        override val timeRemainingSeconds: Int,
        override val results: List<PuzzleResult>,
        val showSummaryDialog: Boolean,
        val isNewHighScore: Boolean,
    ) : BoardState()
}
