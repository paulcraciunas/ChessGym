package com.paulcraciunas.domain.api.blindmode

import com.paulcraciunas.game.logic.api.Result

/**
 * Use case for handling completion of a blind mode game session.
 * Updates the user's rating and statistics based on the game result.
 */
interface OnBlindModeGameComplete {
    suspend operator fun invoke(result: BlindModeGameResult)
}

data class BlindModeGameResult(
    val result: Result,
    val isPlayerWin: Boolean,
    val movesPlayed: Int,
    val timeSpentMillis: Long,
    val isTrainingMode: Boolean,
    val opponentElo: Int,
)
