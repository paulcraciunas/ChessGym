package com.paulcraciunas.domain.impl.boardvis

import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.domain.api.achievements.FakeAchievementNotificationManager
import com.paulcraciunas.domain.api.boardvis.KnightPathResult
import com.paulcraciunas.domain.impl.achievements.UpdateAchievementProgressImpl
import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserDefaults
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class OnKnightPathCompleteImplTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fakeUserRepository = FakeUserRepository()
    private val updateAchievementProgress = UpdateAchievementProgressImpl(
        FakeAchievementNotificationManager(),
    )
    private val underTest = OnKnightPathCompleteImpl(
        fakeUserRepository,
        updateAchievementProgress,
        testDispatcher,
    )

    @Test
    fun `GIVEN score above high score WHEN invoke THEN updates high score`() = runTest(testDispatcher) {
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val newHighScore = UserDefaults.HIGH_SCORE_KNIGHT_PATH + 10
        val result = KnightPathResult(score = newHighScore, timeSpentMillis = 60_000L)

        underTest(result)

        fakeUserRepository.get().apply {
            assertEquals(newHighScore, highScores.knightPath)
        }
    }

    @Test
    fun `GIVEN score below high score WHEN invoke THEN does not update high score`() = runTest(testDispatcher) {
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val belowHighScore = UserDefaults.HIGH_SCORE_KNIGHT_PATH - 5
        val result = KnightPathResult(score = belowHighScore, timeSpentMillis = 60_000L)

        underTest(result)

        fakeUserRepository.get().apply {
            assertEquals(UserDefaults.HIGH_SCORE_KNIGHT_PATH, highScores.knightPath)
        }
    }

    @Test
    fun `GIVEN result WHEN invoke THEN logs history entry`() = runTest(testDispatcher) {
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val result = KnightPathResult(score = 15, timeSpentMillis = 60_000L)

        underTest(result)

        fakeUserRepository.get().apply {
            assertTrue(history.isNotEmpty())
            val todayHistory = history.find { it.timestamp == LocalDate.now() }
            assertTrue(todayHistory != null)

            val historyData = todayHistory!!.data as User.HistoryItem.HistoryItemData.BoardVisualizationData
            assertEquals(1, historyData.sessionsCompleted)
            assertEquals(result.timeSpentMillis, historyData.timeSpent)
        }
    }

    @Test
    fun `GIVEN multiple completions same day WHEN invoke THEN merges history entries`() = runTest(testDispatcher) {
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)

        val firstResult = KnightPathResult(score = 10, timeSpentMillis = 30_000L)
        val secondResult = KnightPathResult(score = 20, timeSpentMillis = 30_000L)

        underTest(firstResult)
        underTest(secondResult)

        fakeUserRepository.get().apply {
            val todayHistory = history.filter { it.timestamp == LocalDate.now() }
            assertEquals(1, todayHistory.size)

            val historyData = todayHistory.first().data as User.HistoryItem.HistoryItemData.BoardVisualizationData
            assertEquals(2, historyData.sessionsCompleted)
            assertEquals(60_000L, historyData.timeSpent)
        }
    }

    @Test
    fun `GIVEN user with zero high score WHEN invoke THEN updates high score`() = runTest(testDispatcher) {
        val currentUser = User()
        fakeUserRepository.update(currentUser)
        val result = KnightPathResult(score = 5, timeSpentMillis = 60_000L)

        underTest(result)

        fakeUserRepository.get().apply {
            assertEquals(5, highScores.knightPath)
        }
    }

    @Test
    fun `GIVEN result WHEN invoke THEN updates totalTimeSpent`() = runTest(testDispatcher) {
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val result = KnightPathResult(score = 10, timeSpentMillis = 45_000L)

        underTest(result)

        fakeUserRepository.get().apply {
            assertEquals(
                UserDefaults.STATISTICS_TIME_PLAYED + result.timeSpentMillis,
                statistics.totalTimeSpent
            )
        }
    }

    @Test
    fun `GIVEN multiple completions WHEN invoke twice THEN totalTimeSpent accumulates`() = runTest(testDispatcher) {
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val firstResult = KnightPathResult(score = 5, timeSpentMillis = 20_000L)
        val secondResult = KnightPathResult(score = 10, timeSpentMillis = 30_000L)

        underTest(firstResult)
        underTest(secondResult)

        fakeUserRepository.get().apply {
            assertEquals(
                UserDefaults.STATISTICS_TIME_PLAYED + 20_000L + 30_000L,
                statistics.totalTimeSpent
            )
        }
    }

    @Test
    fun `GIVEN result WHEN invoke THEN updates achievement-related statistics and progress`() = runTest(testDispatcher) {
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val result = KnightPathResult(score = 15, timeSpentMillis = 60_000L)

        underTest(result)

        fakeUserRepository.get().apply {
            assertEquals(1, statistics.knightPathSessions)
            assertEquals(1L, achievements.progress[Achievement.KNIGHT_PATH_SESSIONS.name])
            assertEquals(1L, achievements.progress[Achievement.BOARD_VISION.name])
        }
    }

    @Test
    fun `GIVEN result WHEN invoke THEN does not affect other high scores`() = runTest(testDispatcher) {
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val result = KnightPathResult(score = 100, timeSpentMillis = 60_000L)

        underTest(result)

        fakeUserRepository.get().apply {
            assertEquals(UserDefaults.HIGH_SCORE_RATED, highScores.ratedPuzzle)
            assertEquals(UserDefaults.HIGH_SCORE_RUSH, highScores.puzzleRush)
            assertEquals(UserDefaults.HIGH_SCORE_STREAK, highScores.puzzleStreak)
            assertEquals(UserDefaults.HIGH_SCORE_FIND_SQUARE, highScores.findTheSquare)
            assertEquals(UserDefaults.RATING_BLIND_MODE, highScores.blindMode)
        }
    }
}
