package com.paulcraciunas.domain.impl

import com.paulcraciunas.domain.api.OnStreakPuzzleComplete
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

    override suspend fun invoke() {
        val currentUser = userRepository.get()

        val newStreakCount = currentUser.puzzleStreak.currentCount + 1

        val updatedUser = currentUser.copy(
            puzzleStreak = currentUser.puzzleStreak.copy(
                currentCount = newStreakCount,
                lastPuzzleId = null, // Clear so next fetch gets a new puzzle
            )
        )

        userRepository.update(updatedUser)
    }
}
