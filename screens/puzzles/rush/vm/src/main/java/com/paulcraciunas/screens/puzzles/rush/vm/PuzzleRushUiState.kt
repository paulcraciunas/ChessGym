package com.paulcraciunas.screens.puzzles.rush.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.screens.common.model.PuzzleResult
import com.paulcraciunas.screens.common.model.PlayableData
import com.paulcraciunas.screens.common.model.Promotion

@Immutable
sealed class PuzzleRushUiState {
    data object Loading : PuzzleRushUiState()
    data object Failed : PuzzleRushUiState()

    @Immutable
    abstract class BoardState : PuzzleRushUiState() {
        abstract val data: PlayableData
        abstract val timeRemainingSeconds: Int
        abstract val results: List<PuzzleResult>
    }

    @Immutable
    data class Ready(
        override val data: PlayableData,
        override val timeRemainingSeconds: Int,
        override val results: List<PuzzleResult> = emptyList(),
    ) : BoardState()

    @Immutable
    data class Playing(
        override val data: PlayableData,
        override val timeRemainingSeconds: Int,
        override val results: List<PuzzleResult>,
        val promotion: Promotion?,
    ) : BoardState()

    @Immutable
    data class Finished(
        override val data: PlayableData,
        override val timeRemainingSeconds: Int,
        override val results: List<PuzzleResult>,
        val showSummaryDialog: Boolean,
        val isNewHighScore: Boolean,
    ) : BoardState()
}
