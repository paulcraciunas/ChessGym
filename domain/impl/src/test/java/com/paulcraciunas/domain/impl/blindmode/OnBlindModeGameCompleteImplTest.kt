package com.paulcraciunas.domain.impl.blindmode

import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.domain.api.achievements.FakeAchievementNotificationManager
import com.paulcraciunas.domain.api.blindmode.BlindModeGameResult
import com.paulcraciunas.domain.impl.achievements.UpdateAchievementProgressImpl
import com.paulcraciunas.domain.impl.general.CalculateEloImpl
import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserDefaults
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class OnBlindModeGameCompleteImplTest {
    private val userRepository = FakeUserRepository()
    private val calculateElo = CalculateEloImpl()
    private val updateAchievementProgress = UpdateAchievementProgressImpl(
        FakeAchievementNotificationManager(),
    )

    private val underTest = OnBlindModeGameCompleteImpl(
        userRepository = userRepository,
        calculateElo = calculateElo,
        updateAchievementProgress = updateAchievementProgress,
    )

    @BeforeEach
    fun setUp() = runTest {
        userRepository.update(UserDefaults.signedInUser())
    }

    @Test
    fun `GIVEN training mode win WHEN invoke THEN updates time but not rating`() = runTest {
        val result = trainingResult(isPlayerWin = true)

        underTest.invoke(result)

        val updatedUser = userRepository.get()
        assertEquals(UserDefaults.RATING_BLIND_MODE, updatedUser.ratings.blindMode)
        assertEquals(
            UserDefaults.STATISTICS_TIME_PLAYED + result.timeSpentMillis,
            updatedUser.statistics.totalTimeSpent,
        )
    }

    @Test
    fun `GIVEN training mode loss WHEN invoke THEN updates time but not rating`() = runTest {
        val result = trainingResult(isPlayerWin = false)

        underTest.invoke(result)

        val updatedUser = userRepository.get()
        assertEquals(UserDefaults.RATING_BLIND_MODE, updatedUser.ratings.blindMode)
    }

    @Test
    fun `GIVEN rated win WHEN invoke THEN increases rating`() = runTest {
        val result = ratedResult(isPlayerWin = true)
        val previousRating = userRepository.get().ratings.blindMode

        underTest.invoke(result)

        val updatedUser = userRepository.get()
        assertTrue(updatedUser.ratings.blindMode > previousRating)
    }

    @Test
    fun `GIVEN rated loss WHEN invoke THEN decreases rating`() = runTest {
        val result = ratedResult(isPlayerWin = false)
        val previousRating = userRepository.get().ratings.blindMode

        underTest.invoke(result)

        val updatedUser = userRepository.get()
        assertTrue(updatedUser.ratings.blindMode < previousRating)
    }

    @Test
    fun `GIVEN rated game WHEN invoke THEN logs BlindModeData history`() = runTest {
        val result = ratedResult(isPlayerWin = true)

        underTest.invoke(result)

        val updatedUser = userRepository.get()
        val historyData = updatedUser.history.last().data
        assertTrue(historyData is User.HistoryItem.HistoryItemData.BlindModeData)
    }

    @Test
    fun `GIVEN training game WHEN invoke THEN logs BlindModeTrainingData history`() = runTest {
        val result = trainingResult(isPlayerWin = false)

        underTest.invoke(result)

        val updatedUser = userRepository.get()
        val historyData = updatedUser.history.last().data
        assertTrue(historyData is User.HistoryItem.HistoryItemData.BlindModeTrainingData)
        val trainingData = historyData as User.HistoryItem.HistoryItemData.BlindModeTrainingData
        assertEquals(result.movesPlayed, trainingData.mostMovesCompleted)
        assertEquals(result.timeSpentMillis, trainingData.timeSpent)
    }

    @Test
    fun `GIVEN rated win WHEN invoke THEN history has positive rating change`() = runTest {
        val result = ratedResult(isPlayerWin = true)

        underTest.invoke(result)

        val updatedUser = userRepository.get()
        val blindModeData = updatedUser.history.last().data as User.HistoryItem.HistoryItemData.BlindModeData
        assertTrue(blindModeData.ratingChange > 0)
    }

    @Test
    fun `GIVEN rated loss WHEN invoke THEN history has negative rating change`() = runTest {
        val result = ratedResult(isPlayerWin = false)

        underTest.invoke(result)

        val updatedUser = userRepository.get()
        val blindModeData = updatedUser.history.last().data as User.HistoryItem.HistoryItemData.BlindModeData
        assertTrue(blindModeData.ratingChange < 0)
    }

    @Test
    fun `GIVEN rated win WHEN invoke THEN updates achievement-related statistics and progress`() = runTest {
        // Given
        val result = ratedResult(isPlayerWin = true)

        // When
        underTest.invoke(result)

        // Then
        val updatedUser = userRepository.get()
        assertEquals(1, updatedUser.statistics.blindModeWins)
        assertEquals(1L, updatedUser.achievements.progress[Achievement.BLIND_MODE_WINS.name])
    }

    @Test
    fun `GIVEN rated loss WHEN invoke THEN does not increment blindModeWins`() = runTest {
        // Given
        val result = ratedResult(isPlayerWin = false)

        // When
        underTest.invoke(result)

        // Then
        val updatedUser = userRepository.get()
        assertEquals(0, updatedUser.statistics.blindModeWins)
        assertEquals(0L, updatedUser.achievements.progress[Achievement.BLIND_MODE_WINS.name])
    }

    @Test
    fun `GIVEN training mode win WHEN invoke THEN does not increment blindModeWins`() = runTest {
        // Given
        val result = trainingResult(isPlayerWin = true)

        // When
        underTest.invoke(result)

        // Then
        val updatedUser = userRepository.get()
        assertEquals(0, updatedUser.statistics.blindModeWins)
        assertEquals(0L, updatedUser.achievements.progress[Achievement.BLIND_MODE_WINS.name])
    }

    private fun trainingResult(isPlayerWin: Boolean): BlindModeGameResult = BlindModeGameResult(
        isPlayerWin = isPlayerWin,
        movesPlayed = 20,
        timeSpentMillis = 5_000L,
        isTrainingMode = true,
        opponentElo = UserDefaults.RATING_BLIND_MODE,
    )

    private fun ratedResult(isPlayerWin: Boolean): BlindModeGameResult = BlindModeGameResult(
        isPlayerWin = isPlayerWin,
        movesPlayed = 30,
        timeSpentMillis = 10_000L,
        isTrainingMode = false,
        opponentElo = UserDefaults.RATING_BLIND_MODE,
    )
}
