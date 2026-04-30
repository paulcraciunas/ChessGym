package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.achievements.UpdateAchievementProgress
import com.paulcraciunas.domain.api.puzzles.OnPuzzleRushComplete
import com.paulcraciunas.domain.api.puzzles.PuzzleRushResult
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserRepository
import java.time.LocalDate
import javax.inject.Inject

class OnPuzzleRushCompleteImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val updateAchievementProgress: UpdateAchievementProgress,
) : OnPuzzleRushComplete {

    override suspend operator fun invoke(result: PuzzleRushResult) {
        val currentUser = userRepository.get()

        val totalPuzzles = result.puzzlesSolved + result.puzzlesFailed
        val newPuzzlesPlayed = currentUser.statistics.puzzlesPlayed + totalPuzzles
        val newPuzzlesSolved = currentUser.statistics.puzzlesSolved + result.puzzlesSolved
        val newPuzzleRushHighScore = maxOf(currentUser.highScores.puzzleRush, result.puzzlesSolved)
        val newTotalTimeSpent = currentUser.statistics.totalTimeSpent + result.timeSpentMillis
        val newFailedPuzzles = (currentUser.failedPuzzles + result.failedPuzzleIds).distinct()

        val updatedUser = currentUser.copy(
            highScores = currentUser.highScores.copy(puzzleRush = newPuzzleRushHighScore),
            statistics = currentUser.statistics.copy(
                puzzlesPlayed = newPuzzlesPlayed,
                puzzlesSolved = newPuzzlesSolved,
                totalTimeSpent = newTotalTimeSpent,
                puzzleRushSessions = currentUser.statistics.puzzleRushSessions + 1,
                rushPuzzlesSolved = currentUser.statistics.rushPuzzlesSolved + result.puzzlesSolved,
            ),
            failedPuzzles = newFailedPuzzles,
        )

        val withAchievements = updateAchievementProgress(updatedUser)

        val historyItem = User.HistoryItem(
            timestamp = LocalDate.now(),
            data = User.HistoryItem.HistoryItemData.PuzzleRushData(
                tries = 1,
                bestScore = result.puzzlesSolved,
                timeSpent = result.timeSpentMillis
            )
        )

        userRepository.update(withAchievements)
        userRepository.logHistory(listOf(historyItem))
    }
}
