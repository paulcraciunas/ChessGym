package com.paulcraciunas.domain.api

/**
 * Use case for handling successful puzzle completion in streak mode.
 *
 * This use case is responsible for:
 * - Incrementing the current streak count
 * - Updating the last puzzle ID so the user can continue later
 */
interface OnStreakPuzzleComplete {
    /**
     * Processes a successfully completed puzzle in streak mode.
     *
     * @param puzzleId The ID of the completed puzzle
     */
    suspend operator fun invoke(puzzleId: Int)
}
