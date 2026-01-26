package com.paulcraciunas.domain.api

/**
 * Use case for handling completion of a "Find the Square" game session.
 *
 * This use case is responsible for:
 * - Updating the user's high score if the current score is higher
 * - Logging the game session to the user's history
 */
interface OnFindSquareComplete {
    suspend operator fun invoke(result: FindSquareResult)
}

/**
 * Represents the result of a completed "Find the Square" game session.
 *
 * @property score The number of correctly identified squares
 * @property timeSpentMillis Total time spent in the game session
 */
data class FindSquareResult(
    val score: Int,
    val timeSpentMillis: Long
)
