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
 * - Stores the completed puzzle ID for continuation
 */
class OnStreakPuzzleCompleteImpl @Inject constructor(
    private val userRepository: UserRepository,
) : OnStreakPuzzleComplete {

    override suspend fun invoke(puzzleId: Int) {
        val currentUser = userRepository.get()

        val newStreakCount = currentUser.puzzleStreak.currentCount + 1

        val updatedUser = currentUser.copy(
            puzzleStreak = currentUser.puzzleStreak.copy(
                currentCount = newStreakCount,
                lastPuzzleId = puzzleId,
            )
        )

        userRepository.update(updatedUser)
    }
}
