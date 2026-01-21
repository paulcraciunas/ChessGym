package com.paulcraciunas.domain.impl

import com.paulcraciunas.domain.api.GetStreakPuzzle
import com.paulcraciunas.puzzles.api.PuzzleRepository
import com.paulcraciunas.user.api.UserRepository
import javax.inject.Inject

/**
 * Implementation of [GetStreakPuzzle] that fetches puzzles with increasing difficulty
 * based on the current streak count.
 *
 * The puzzle rating is calculated as: BASE_RATING + (currentCount * RATING_INCREMENT)
 */
class GetStreakPuzzleImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val puzzleRepository: PuzzleRepository,
) : GetStreakPuzzle {

    override suspend fun invoke(): GetStreakPuzzle.Data {
        val user = userRepository.get()
        val currentCount = user.puzzleStreak.currentCount

        // Calculate target rating based on current streak count
        val targetRating = GetStreakPuzzle.BASE_RATING + (currentCount * GetStreakPuzzle.RATING_INCREMENT)

        val puzzle = puzzleRepository.getByRating(targetRating)
            ?: throw IllegalStateException("No puzzle available for rating $targetRating")

        return GetStreakPuzzle.Data(
            puzzle = puzzle,
            currentStreakCount = currentCount,
        )
    }
}
