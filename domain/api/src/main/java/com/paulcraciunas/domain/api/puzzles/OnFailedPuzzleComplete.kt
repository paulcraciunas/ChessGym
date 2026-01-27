package com.paulcraciunas.domain.api.puzzles

/**
 * Use case for handling a successfully solved failed puzzle.
 *
 * This use case is responsible for:
 * - Removing the puzzle from the user's failed puzzles list
 * - Updating puzzles solved statistics
 */
interface OnFailedPuzzleComplete {

    /**
     * Removes the completed puzzle from the failed puzzles list.
     *
     * @param puzzleId The ID of the successfully solved puzzle
     */
    suspend operator fun invoke(puzzleId: Int)
}
