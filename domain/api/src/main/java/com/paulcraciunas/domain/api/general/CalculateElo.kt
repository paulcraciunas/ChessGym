package com.paulcraciunas.domain.api.general

/**
 * Use case for calculating ELO rating changes based on the standard ELO algorithm.
 * 
 * The ELO rating system calculates the relative skill levels of players and determines
 * how ratings should change based on game outcomes and the difference in player ratings.
 */
interface CalculateElo {
    
    /**
     * Calculates potential ELO rating changes for a puzzle match.
     * 
     * @param userRating The current rating of the user
     * @param puzzleRating The rating of the puzzle to be solved
     * @return EloResult containing potential gain (on success) and potential loss (on failure)
     */
    operator fun invoke(userRating: Int, puzzleRating: Int): EloResult
}
