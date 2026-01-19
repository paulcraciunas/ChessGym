package com.paulcraciunas.domain.api

/**
 * Use case for handling puzzle completion and updating user statistics and history.
 * 
 * This use case is responsible for:
 * - Updating user's current rating
 * - Updating puzzle statistics (played, solved)
 * - Managing daily history tracking
 * - Updating best rating if applicable
 */
interface OnPuzzleComplete {
    
    /**
     * Processes a completed puzzle and updates all relevant user data.
     * 
     * @param completionResult The result of the completed puzzle
     */
    suspend operator fun invoke(completionResult: PuzzleCompletionResult)
}
