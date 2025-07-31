package com.paulcraciunas.domain.impl

import com.paulcraciunas.domain.api.GetPuzzleByRating
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.puzzles.api.PuzzleRepository
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetPuzzleByRatingImpl @Inject constructor(
    private val puzzleRepository: PuzzleRepository,
    private val appSettingsRepository: AppSettingsRepository,
) : GetPuzzleByRating {

    override suspend fun invoke(targetRating: Int): Puzzle {
        // First try exact match
        puzzleRepository.getByRating(targetRating)?.let {
            return it
        }

        // Get max rating from settings
        val appSettings = appSettingsRepository.appSettings.first()
        val maxRating = appSettings.maxPuzzleRating

        // Use systematic ±1 expansion logic
        var topRating = targetRating
        var bottomRating = targetRating

        while (bottomRating > MIN_RATING && topRating < maxRating) {
            // Expand by 1 in each direction
            topRating += 1
            bottomRating -= 1

            // Check the expanded range
            puzzleRepository.getByRatingRange(bottomRating, topRating)?.let {
                return it
            }
        }

        throw IllegalArgumentException("Invalid rating $targetRating. No puzzles in database match within available rating range.")
    }

    private companion object {
        private const val MIN_RATING = 1
    }
}
