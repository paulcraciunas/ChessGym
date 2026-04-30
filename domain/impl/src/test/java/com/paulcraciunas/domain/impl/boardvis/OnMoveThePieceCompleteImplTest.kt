package com.paulcraciunas.domain.impl.boardvis

import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.domain.api.achievements.FakeAchievementNotificationManager
import com.paulcraciunas.domain.api.boardvis.MoveThePieceResult
import com.paulcraciunas.domain.impl.achievements.UpdateAchievementProgressImpl
import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserDefaults
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class OnMoveThePieceCompleteImplTest {
    private val fakeUserRepository = FakeUserRepository()
    private val updateAchievementProgress = UpdateAchievementProgressImpl(
        FakeAchievementNotificationManager(),
    )
    private val underTest = OnMoveThePieceCompleteImpl(
        fakeUserRepository,
        updateAchievementProgress,
    )

    @Test
    fun `GIVEN non-training mode with score above high score WHEN invoke THEN updates high score`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val newHighScore = UserDefaults.HIGH_SCORE_MOVE_PIECE + 10
        val result = MoveThePieceResult(
            score = newHighScore,
            timeSpentMillis = 60_000L,
            isTrainingMode = false
        )

        // When
        underTest(result)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(newHighScore, highScores.moveThePiece)
        }
    }

    @Test
    fun `GIVEN training mode with score above high score WHEN invoke THEN does not update high score`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val newHighScore = UserDefaults.HIGH_SCORE_MOVE_PIECE + 10
        val result = MoveThePieceResult(
            score = newHighScore,
            timeSpentMillis = 60_000L,
            isTrainingMode = true
        )

        // When
        underTest(result)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(UserDefaults.HIGH_SCORE_MOVE_PIECE, highScores.moveThePiece)
        }
    }

    @Test
    fun `GIVEN non-training mode with score below high score WHEN invoke THEN does not update high score`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val belowHighScore = UserDefaults.HIGH_SCORE_MOVE_PIECE - 5
        val result = MoveThePieceResult(
            score = belowHighScore,
            timeSpentMillis = 60_000L,
            isTrainingMode = false
        )

        // When
        underTest(result)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(UserDefaults.HIGH_SCORE_MOVE_PIECE, highScores.moveThePiece)
        }
    }

    @Test
    fun `GIVEN result WHEN invoke THEN logs history entry`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val result = MoveThePieceResult(
            score = 15,
            timeSpentMillis = 60_000L,
            isTrainingMode = false
        )

        // When
        underTest(result)

        // Then
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
    fun `GIVEN training mode result WHEN invoke THEN still logs history entry`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val result = MoveThePieceResult(
            score = 15,
            timeSpentMillis = 45_000L,
            isTrainingMode = true
        )

        // When
        underTest(result)

        // Then
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
    fun `GIVEN multiple completions same day WHEN invoke THEN merges history entries`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)

        val firstResult = MoveThePieceResult(
            score = 10,
            timeSpentMillis = 30_000L,
            isTrainingMode = false
        )
        val secondResult = MoveThePieceResult(
            score = 20,
            timeSpentMillis = 30_000L,
            isTrainingMode = false
        )

        // When
        underTest(firstResult)
        underTest(secondResult)

        // Then
        fakeUserRepository.get().apply {
            val todayHistory = history.filter { it.timestamp == LocalDate.now() }
            assertEquals(1, todayHistory.size) // Should be merged

            val historyData = todayHistory.first().data as User.HistoryItem.HistoryItemData.BoardVisualizationData
            assertEquals(2, historyData.sessionsCompleted)
            assertEquals(60_000L, historyData.timeSpent)
        }
    }

    @Test
    fun `GIVEN user with zero high score WHEN invoke non-training mode THEN updates high score`() = runTest {
        // Given
        val currentUser = User()
        fakeUserRepository.update(currentUser)
        val result = MoveThePieceResult(
            score = 5,
            timeSpentMillis = 60_000L,
            isTrainingMode = false
        )

        // When
        underTest(result)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(5, highScores.moveThePiece)
        }
    }

    @Test
    fun `GIVEN non-training result WHEN invoke THEN updates totalTimeSpent`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val result = MoveThePieceResult(
            score = 10,
            timeSpentMillis = 45_000L,
            isTrainingMode = false
        )

        // When
        underTest(result)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(
                UserDefaults.STATISTICS_TIME_PLAYED + result.timeSpentMillis,
                statistics.totalTimeSpent
            )
        }
    }

    @Test
    fun `GIVEN training mode result WHEN invoke THEN still updates totalTimeSpent`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val result = MoveThePieceResult(
            score = 10,
            timeSpentMillis = 30_000L,
            isTrainingMode = true
        )

        // When
        underTest(result)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(
                UserDefaults.STATISTICS_TIME_PLAYED + result.timeSpentMillis,
                statistics.totalTimeSpent
            )
        }
    }

    @Test
    fun `GIVEN multiple completions WHEN invoke twice THEN totalTimeSpent accumulates`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val firstResult = MoveThePieceResult(score = 5, timeSpentMillis = 20_000L, isTrainingMode = false)
        val secondResult = MoveThePieceResult(score = 10, timeSpentMillis = 30_000L, isTrainingMode = false)

        // When
        underTest(firstResult)
        underTest(secondResult)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(
                UserDefaults.STATISTICS_TIME_PLAYED + 20_000L + 30_000L,
                statistics.totalTimeSpent
            )
        }
    }

    @Test
    fun `GIVEN result WHEN invoke THEN updates achievement-related statistics and progress`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val result = MoveThePieceResult(
            score = 15,
            timeSpentMillis = 60_000L,
            isTrainingMode = false
        )

        // When
        underTest(result)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(1, statistics.moveThePieceSessions)
            assertEquals(1L, achievements.progress[Achievement.MOVE_PIECE_SESSIONS.name])
            assertEquals(1L, achievements.progress[Achievement.BOARD_VISION.name])
        }
    }

    @Test
    fun `GIVEN result WHEN invoke THEN does not affect other high scores`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val result = MoveThePieceResult(
            score = 100,
            timeSpentMillis = 60_000L,
            isTrainingMode = false
        )

        // When
        underTest(result)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(UserDefaults.HIGH_SCORE_RATED, highScores.ratedPuzzle)
            assertEquals(UserDefaults.HIGH_SCORE_RUSH, highScores.puzzleRush)
            assertEquals(UserDefaults.HIGH_SCORE_STREAK, highScores.puzzleStreak)
            assertEquals(UserDefaults.HIGH_SCORE_FIND_SQUARE, highScores.findTheSquare)
            assertEquals(UserDefaults.RATING_BLIND_MODE, highScores.blindMode)
        }
    }
}
