package com.paulcraciunas.screens.puzzles.streak.vm

import com.paulcraciunas.screens.data.engine.PlaySessionState

internal class PuzzleStreakUiStateAdapter(
    private val sessions: PuzzleStreakSessions,
) {
    fun toUiState(state: PlaySessionState, currentHighScore: Int): PuzzleStreakUiState = when (state.status) {
        PlaySessionState.Status.Failed -> PuzzleStreakUiState.Failed
        PlaySessionState.Status.Loading -> PuzzleStreakUiState.Loading
        PlaySessionState.Status.Ended -> PuzzleStreakUiState.StreakEnded(
            data = state.boardState,
            streakCount = sessions.streakCount,
            showSummary = state.showSummary,
            isNewHighScore = sessions.streakCount > currentHighScore,
        )
        PlaySessionState.Status.Paused -> PuzzleStreakUiState.Playing(
            data = state.boardState,
            streakCount = sessions.streakCount + 1, // we just completed a puzzle
            hintEnabled = state.hintAvailable,
            showAbandonDialog = state.abandonRequested,
            isAwaitingNextPuzzle = true,
        )
        else -> PuzzleStreakUiState.Playing(
            data = state.boardState,
            streakCount = sessions.streakCount,
            hintEnabled = state.hintAvailable,
            showAbandonDialog = state.abandonRequested,
            isAwaitingNextPuzzle = false,
        )
    }
}
