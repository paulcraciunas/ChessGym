package com.paulcraciunas.domain.impl.blindmode

import com.paulcraciunas.domain.api.achievements.UpdateAchievementProgress
import com.paulcraciunas.domain.api.blindmode.BlindModeGameResult
import com.paulcraciunas.domain.api.blindmode.OnBlindModeGameComplete
import com.paulcraciunas.domain.api.general.CalculateElo
import com.paulcraciunas.domain.api.general.EloResult
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserRepository
import java.time.LocalDate
import javax.inject.Inject

class OnBlindModeGameCompleteImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val calculateElo: CalculateElo,
    private val updateAchievementProgress: UpdateAchievementProgress,
) : OnBlindModeGameComplete {

    override suspend fun invoke(result: BlindModeGameResult) {
        val currentUser = userRepository.get()
        val newTimeSpent = currentUser.statistics.totalTimeSpent + result.timeSpentMillis

        val eloResult = calculateElo(
            userRating = currentUser.ratings.blindMode,
            puzzleRating = result.opponentElo,
        )

        val updatedUser = if (result.isTrainingMode) {
            buildTrainingUpdate(currentUser, newTimeSpent)
        } else {
            buildRatedUpdate(currentUser, result, newTimeSpent, eloResult)
        }

        val withAchievements = updateAchievementProgress(updatedUser)

        userRepository.update(withAchievements)
        userRepository.logHistory(listOf(buildHistoryItem(result, eloResult)))
    }

    private fun buildTrainingUpdate(
        currentUser: User,
        newTimeSpent: Long,
    ): User = currentUser.copy(
        statistics = currentUser.statistics.copy(totalTimeSpent = newTimeSpent),
    )

    private fun buildRatedUpdate(
        currentUser: User,
        result: BlindModeGameResult,
        newTimeSpent: Long,
        eloResult: EloResult,
    ): User {
        val ratingChange = eloResult.getNormalized(result.isPlayerWin)

        return currentUser.copy(
            ratings = currentUser.ratings.copy(
                blindMode = currentUser.ratings.blindMode + ratingChange
            ),
            statistics = currentUser.statistics.copy(
                totalTimeSpent = newTimeSpent,
                blindModeWins = currentUser.statistics.blindModeWins + if (result.isPlayerWin) 1 else 0,
            ),
        )
    }

    private fun buildHistoryItem(
        result: BlindModeGameResult,
        eloResult: EloResult,
    ): User.HistoryItem {
        val today = LocalDate.now()
        val data = if (result.isTrainingMode) {
            User.HistoryItem.HistoryItemData.BlindModeTrainingData(
                tries = 1,
                mostMovesCompleted = result.movesPlayed,
                timeSpent = result.timeSpentMillis,
            )
        } else {
            User.HistoryItem.HistoryItemData.BlindModeData(
                played = 1,
                ratingChange = eloResult.getNormalized(result.isPlayerWin),
                timeSpent = result.timeSpentMillis,
            )
        }
        return User.HistoryItem(timestamp = today, data = data)
    }
}
