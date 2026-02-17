package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.puzzles.OnStreakPuzzleComplete
import com.paulcraciunas.user.api.UserRepository
import javax.inject.Inject

/**
 * Implementation of [OnStreakPuzzleComplete] that updates user data after
 * successfully completing a puzzle in streak mode.
 *
 * Updates:
 * - Increments current streak count
 * - Clears lastPuzzleId so the next puzzle fetch will get a new one
 *
 * Note: The lastPuzzleId is managed by GetStreakPuzzle when loading a new puzzle,
 * so we only clear it here to signal that the current puzzle was completed.
 */
class OnStreakPuzzleCompleteImpl @Inject constructor(
    private val userRepository: UserRepository,
) : OnStreakPuzzleComplete {

    override suspend operator fun invoke(timeSpentMillis: Long) {
        val currentUser = userRepository.get()

        val newStreakCount = currentUser.puzzleStreak.currentCount + 1
        // Update total time spent
        val newTotalTimeSpent = currentUser.statistics.totalTimeSpent + timeSpentMillis

        val updatedUser = currentUser.copy(
            statistics = currentUser.statistics.copy(
                totalTimeSpent = newTotalTimeSpent
            ),
            puzzleStreak = currentUser.puzzleStreak.copy(
                currentCount = newStreakCount,
                lastPuzzleId = null, // Clear so next fetch gets a new puzzle
            )
        )

        userRepository.update(updatedUser)
    }
}
