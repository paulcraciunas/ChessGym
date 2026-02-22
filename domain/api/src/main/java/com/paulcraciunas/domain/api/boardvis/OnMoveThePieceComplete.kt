package com.paulcraciunas.domain.api.boardvis

/**
 * Use case for handling completion of a "Move the Piece" game session.
 *
 * This use case is responsible for:
 * - Updating the user's high score if not in training mode and score is higher
 * - Logging the time spent in the game session
 */
interface OnMoveThePieceComplete {
    suspend operator fun invoke(result: MoveThePieceResult)
}

/**
 * Represents the result of a completed "Move the Piece" game session.
 *
 * @property score The number of levels completed
 * @property timeSpentMillis Total time spent in the game session
 * @property isTrainingMode Whether the game was played in training mode
 */
data class MoveThePieceResult(
    val score: Int,
    val timeSpentMillis: Long,
    val isTrainingMode: Boolean
)
