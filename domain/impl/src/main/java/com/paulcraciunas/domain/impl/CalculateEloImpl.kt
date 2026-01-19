package com.paulcraciunas.domain.impl

import com.paulcraciunas.domain.api.EloResult
import com.paulcraciunas.domain.api.CalculateElo
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.round

/**
 * Implementation of ELO rating calculation using the standard algorithm.
 *
 * Uses the classic ELO formula:
 * - Expected Score = 1 / (1 + 10^((opponent_rating - player_rating) / 400))
 * - New Rating = Old Rating + K * (Actual Score - Expected Score)
 *
 * K-factor varies based on rating level:
 * - K = 32 for ratings below 2100
 * - K = 24 for ratings between 2100-2400
 * - K = 16 for ratings above 2400
 */
class CalculateEloImpl @Inject constructor() : CalculateElo {

    override fun invoke(userRating: Int, puzzleRating: Int): EloResult {
        val kFactor = getKFactor(userRating)
        val expectedScore = calculateExpectedScore(userRating, puzzleRating)

        // Calculate rating change for win (actual score = 1.0)
        val ratingChangeOnWin = kFactor * (1.0 - expectedScore)
        val potentialGain = round(ratingChangeOnWin).toInt()

        // Calculate rating change for loss (actual score = 0.0)
        val ratingChangeOnLoss = kFactor * (0.0 - expectedScore)
        val potentialLoss = round(-ratingChangeOnLoss).toInt() // Make it positive for display

        return EloResult(
            potentialGain = potentialGain,
            potentialLoss = potentialLoss
        )
    }

    /**
     * Calculates the expected score using the standard ELO formula.
     * Expected Score = 1 / (1 + 10^((opponent_rating - player_rating) / 400))
     */
    private fun calculateExpectedScore(playerRating: Int, opponentRating: Int): Double {
        val ratingDifference = (opponentRating - playerRating).toDouble()
        val exponent = ratingDifference / SCALING_FACTOR
        return 1.0 / (1.0 + 10.0.pow(exponent))
    }

    /**
     * Determines the K-factor based on the player's current rating.
     * Lower rated players have higher K-factor for faster rating adjustments.
     */
    private fun getKFactor(rating: Int): Int = when {
        rating < RATING_THRESHOLD_MID -> K_FACTOR_LOW
        rating < RATING_THRESHOLD_HIGH -> K_FACTOR_MID
        else -> K_FACTOR_HIGH
    }

    companion object {
        private const val SCALING_FACTOR = 400.0
        private const val K_FACTOR_LOW = 32
        private const val K_FACTOR_MID = 24
        private const val K_FACTOR_HIGH = 16

        private const val RATING_THRESHOLD_MID = 2100
        private const val RATING_THRESHOLD_HIGH = 2400
    }
}
