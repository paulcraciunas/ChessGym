package com.paulcraciunas.domain.impl

import com.paulcraciunas.domain.api.OnFailedPuzzleComplete
import com.paulcraciunas.user.api.UserRepository
import javax.inject.Inject

/**
 * Implementation of [OnFailedPuzzleComplete] that removes the puzzle from the user's failed list.
 *
 * Updates:
 * - Removes the puzzle ID from the failed puzzles list
 * - Increments the puzzles solved count
 */
class OnFailedPuzzleCompleteImpl @Inject constructor(
    private val userRepository: UserRepository,
) : OnFailedPuzzleComplete {

    override suspend operator fun invoke(puzzleId: Int) {
        val currentUser = userRepository.get()

        // Remove the puzzle ID from failed list
        val updatedFailedPuzzles = currentUser.failedPuzzles.filter { it != puzzleId }

        // Increment puzzles solved count
        val newPuzzlesSolved = currentUser.statistics.puzzlesSolved + 1

        // Create updated user
        val updatedUser = currentUser.copy(
            failedPuzzles = updatedFailedPuzzles,
            statistics = currentUser.statistics.copy(
                puzzlesSolved = newPuzzlesSolved
            )
        )

        userRepository.update(updatedUser)
    }
}
