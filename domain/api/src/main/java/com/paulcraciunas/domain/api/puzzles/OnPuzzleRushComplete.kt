package com.paulcraciunas.domain.api.puzzles

/**
 * Use case for handling puzzle rush completion and updating user statistics and history.
 *
 * This use case is responsible for:
 * - Updating puzzle rush high score if applicable
 * - Updating total puzzles played and solved statistics
 * - Adding failed puzzle IDs to the retry list
 * - Managing daily history tracking
 */
interface OnPuzzleRushComplete {

    /**
     * Processes a completed puzzle rush and updates all relevant user data.
     *
     * @param result The result of the completed puzzle rush
     */
    suspend operator fun invoke(result: PuzzleRushResult)
}

/**
 * Result data from a completed puzzle rush session.
 */
data class PuzzleRushResult(
    val puzzlesSolved: Int,
    val puzzlesFailed: Int,
    val failedPuzzleIds: List<Int>,
    val timeSpentMillis: Long,
)
