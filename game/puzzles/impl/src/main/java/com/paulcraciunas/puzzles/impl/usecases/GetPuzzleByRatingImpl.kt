package com.paulcraciunas.puzzles.impl.usecases

import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.puzzles.api.PuzzleRepository
import com.paulcraciunas.puzzles.api.usecases.GetPuzzleByRating
import com.paulcraciunas.puzzles.impl.impl.RandomFactory
import javax.inject.Inject

class GetPuzzleByRatingImpl @Inject constructor(
    private val repository: PuzzleRepository,
    private val randomFactory: RandomFactory,
) : GetPuzzleByRating {

    override suspend fun invoke(targetRating: Int): Puzzle {
        repository.getByRating(targetRating)?.let {
            return@invoke it
        }

        var topRating = targetRating
        var bottomRating = targetRating
        while (bottomRating > 0 && topRating < 4000) { // TODO Paul: integrate AppSettingsRepository.AppSettings.maxPuzzleRating
            topRating += randomFactory.nextInt(1, DEVIATION)
            bottomRating -= randomFactory.nextInt(1, DEVIATION)
            repository.getByRatingRange(topRating, bottomRating)?.let {
                return@invoke it
            }
        }
        throw IllegalArgumentException("Invalid rating $targetRating. No puzzles in database match.")
    }

    private companion object {
        private const val DEVIATION = 10
    }
}
