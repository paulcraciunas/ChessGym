package com.paulcraciunas.screens.boardvis.pieces.vm

import com.paulcraciunas.screens.data.RemainingTime
import com.paulcraciunas.screens.data.engine.PlaySessionState
import java.util.Locale

internal class KnightPathUiStateAdapter {
    fun toUiState(state: PlaySessionState, highScore: Int): KnightPathUiState {
        val time = (state.remainingTimeMs ?: 0L).toRemainingTime()
        val successCount = state.results.count { it.success }
        return when (state.status) {
            PlaySessionState.Status.Loading -> KnightPathUiState.Setup
            PlaySessionState.Status.Ready,
            PlaySessionState.Status.Playing,
            PlaySessionState.Status.Paused -> KnightPathUiState.Playing(
                boardState = state.boardState,
                timeRemaining = time,
                showAbandonDialog = state.abandonRequested,
                score = successCount,
            )
            PlaySessionState.Status.Failed,
            PlaySessionState.Status.Ended -> KnightPathUiState.GameOver(
                boardState = state.boardState,
                timeRemaining = time,
                score = successCount,
                isNewHighScore = successCount > highScore,
                previousHighScore = highScore,
                wasWrongMove = state.remainingTimeMs != 0L,
            )
        }
    }
}

internal fun Long.toRemainingTime(): RemainingTime = RemainingTime(
    value = formatTime(this),
    danger = this < DANGER_THRESHOLD,
)

private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val tenths = (millis % 1000) / 100
    return String.format(Locale.getDefault(), "%02d.%01d", seconds, tenths)
}
