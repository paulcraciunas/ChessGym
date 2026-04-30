package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.achievements.UpdateAchievementProgress
import com.paulcraciunas.domain.api.puzzles.OnFailedPuzzleComplete
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserRepository
import java.time.LocalDate
import javax.inject.Inject

class OnFailedPuzzleCompleteImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val updateAchievementProgress: UpdateAchievementProgress,
) : OnFailedPuzzleComplete {

    override suspend operator fun invoke(puzzleId: Int, timeSpentMillis: Long) {
        val currentUser = userRepository.get()

        val updatedFailedPuzzles = currentUser.failedPuzzles.filter { it != puzzleId }
        val newPuzzlesSolved = currentUser.statistics.puzzlesSolved + 1

        val updatedUser = currentUser.copy(
            failedPuzzles = updatedFailedPuzzles,
            statistics = currentUser.statistics.copy(
                puzzlesSolved = newPuzzlesSolved,
                failedPuzzlesRedeemed = currentUser.statistics.failedPuzzlesRedeemed + 1,
            ),
        )

        val withAchievements = updateAchievementProgress(updatedUser)

        val historyItem = User.HistoryItem(
            timestamp = LocalDate.now(),
            data = User.HistoryItem.HistoryItemData.FailedPuzzleData(
                puzzlesSolved = 1,
                timeSpent = timeSpentMillis,
            )
        )

        userRepository.update(withAchievements)
        userRepository.logHistory(listOf(historyItem))
    }
}
