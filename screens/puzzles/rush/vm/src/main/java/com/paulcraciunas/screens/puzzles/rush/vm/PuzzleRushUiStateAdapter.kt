package com.paulcraciunas.screens.puzzles.rush.vm

import com.paulcraciunas.screens.data.engine.PlaySessionState
import java.util.Locale
import javax.inject.Inject

class PuzzleRushUiStateAdapter @Inject constructor() {

    fun toUiState(state: PlaySessionState, highScore: Int): PuzzleRushUiState {
        val time = (state.remainingTimeMs ?: 0L).toRemainingTime()
        return when (state.status) {
            PlaySessionState.Status.Failed -> PuzzleRushUiState.Failed
            PlaySessionState.Status.Loading -> PuzzleRushUiState.Loading
            PlaySessionState.Status.Ready -> PuzzleRushUiState.Ready(
                data = state.boardState,
                time = time,
                results = state.results,
            )
            PlaySessionState.Status.Playing,
            PlaySessionState.Status.Paused -> PuzzleRushUiState.Playing(
                data = state.boardState,
                time = time,
                results = state.results,
                showAbandonDialog = state.abandonRequested,
            )
            PlaySessionState.Status.Ended -> PuzzleRushUiState.Finished(
                data = state.boardState,
                time = time,
                results = state.results,
                showSummaryDialog = state.showSummary,
                isNewHighScore = state.results.count { it.success } > highScore,
            )
        }
    }

    companion object {
        internal const val DANGER_THRESHOLD = 20_000L
    }
}

internal fun Long.toRemainingTime(): PuzzleRushUiState.RemainingTime =
    PuzzleRushUiState.RemainingTime(
        value = formatTime(this),
        danger = this <= PuzzleRushUiStateAdapter.DANGER_THRESHOLD,
    )

private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    return if (millis <= PuzzleRushUiStateAdapter.DANGER_THRESHOLD) {
        val tenths = (millis % 1000) / 100
        String.format(Locale.getDefault(), "%02d.%01d", seconds, tenths)
    } else {
        val minutes = (millis / 1000) / 60
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}
