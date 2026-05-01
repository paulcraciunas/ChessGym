package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.domain.api.achievements.FakeAchievementNotificationManager
import com.paulcraciunas.domain.api.puzzles.PuzzleCompletionResult
import com.paulcraciunas.domain.impl.achievements.UpdateAchievementProgressImpl
import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserDefaults
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class OnPuzzleCompleteImplTest {
    private val fakeUserRepository = FakeUserRepository()
    private val updateAchievementProgress = UpdateAchievementProgressImpl(
        FakeAchievementNotificationManager(),
    )
    private val underTest: OnPuzzleCompleteImpl = OnPuzzleCompleteImpl(
        fakeUserRepository,
        updateAchievementProgress,
    )

    @Test
    fun `GIVEN successful completion WHEN invoke THEN updates rating stats and history`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        val completionResult = PuzzleCompletionResult(
            puzzleId = 42,
            puzzleRating = 1400,
            wasSuccessful = true,
            ratingChange = 200,
            timeSpentMillis = 300L
        )
        fakeUserRepository.update(currentUser)

        // When
        underTest(completionResult)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(UserDefaults.STATISTICS_PLAYED + 1, statistics.puzzlesPlayed)
            assertEquals(UserDefaults.STATISTICS_SOLVED + 1, statistics.puzzlesSolved)
            assertEquals(UserDefaults.STATISTICS_TIME_PLAYED + completionResult.timeSpentMillis, statistics.totalTimeSpent)
            assertEquals(UserDefaults.RATING + completionResult.ratingChange, ratings.current)
            assertEquals(ratings.current, highScores.ratedPuzzle)

            assertTrue(history.isNotEmpty())
            assertEquals(1, history.size)
            assertEquals(LocalDate.now(), history.first().timestamp)
            val historyData = history.first().data as User.HistoryItem.HistoryItemData.RatedPuzzleData
            assertEquals(1, historyData.puzzlesPlayed)
            assertEquals(1, historyData.puzzlesSolved)
            assertEquals(completionResult.ratingChange, historyData.ratingChange)
            assertEquals(completionResult.timeSpentMillis, historyData.timeSpent)

            // Successful puzzle should not be added to failedPuzzles
            assertTrue(failedPuzzles.isEmpty())
        }
    }

    @Test
    fun `GIVEN failed completion WHEN invoke THEN updates played count and logs failure`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        val completionResult = PuzzleCompletionResult(
            puzzleId = 123,
            puzzleRating = 1450,
            wasSuccessful = false,
            ratingChange = 15,
            timeSpentMillis = 800L
        )
        fakeUserRepository.update(currentUser)

        // When
        underTest(completionResult)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(UserDefaults.STATISTICS_PLAYED + 1, statistics.puzzlesPlayed)
            assertEquals(UserDefaults.STATISTICS_SOLVED, statistics.puzzlesSolved)
            assertEquals(UserDefaults.STATISTICS_TIME_PLAYED + completionResult.timeSpentMillis, statistics.totalTimeSpent)
            assertEquals(UserDefaults.RATING - completionResult.ratingChange, ratings.current)
            assertEquals(UserDefaults.HIGH_SCORE_RATED, highScores.ratedPuzzle)

            assertTrue(history.isNotEmpty())
            val historyData = history.first().data as User.HistoryItem.HistoryItemData.RatedPuzzleData
            assertEquals(1, historyData.puzzlesPlayed)
            assertEquals(0, historyData.puzzlesSolved)
            assertEquals(-completionResult.ratingChange, historyData.ratingChange)
            assertEquals(completionResult.timeSpentMillis, historyData.timeSpent)

            // Failed puzzle with ID should be added to failedPuzzles
            assertTrue(failedPuzzles.contains(123))
        }
    }

    @Test
    fun `GIVEN successful completion WHEN invoke THEN increments ratedPuzzlesSolved and win streak`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val completionResult = PuzzleCompletionResult(
            puzzleId = 42,
            puzzleRating = 1400,
            wasSuccessful = true,
            ratingChange = 200,
            timeSpentMillis = 300L
        )

        // When
        underTest(completionResult)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(1, statistics.ratedPuzzlesSolved)
            assertEquals(1L, achievements.progress[Achievement.RATED_PUZZLES_SOLVED.name])
            assertEquals(1, achievements.currentRatedWinStreak)
            assertEquals(1, achievements.bestRatedWinStreak)
            assertEquals(1L, achievements.progress[Achievement.RATED_WIN_STREAK.name])
        }
    }

    @Test
    fun `GIVEN failed completion WHEN invoke THEN does not increment ratedPuzzlesSolved and resets win streak`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser().copy(
            achievements = UserDefaults.signedInUser().achievements.copy(
                currentRatedWinStreak = 3,
                bestRatedWinStreak = 5,
            )
        )
        fakeUserRepository.update(currentUser)
        val completionResult = PuzzleCompletionResult(
            puzzleId = 123,
            puzzleRating = 1450,
            wasSuccessful = false,
            ratingChange = 15,
            timeSpentMillis = 800L
        )

        // When
        underTest(completionResult)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(0, statistics.ratedPuzzlesSolved)
            assertEquals(0L, achievements.progress[Achievement.RATED_PUZZLES_SOLVED.name])
            assertEquals(0, achievements.currentRatedWinStreak)
            assertEquals(5, achievements.bestRatedWinStreak)
            assertEquals(5L, achievements.progress[Achievement.RATED_WIN_STREAK.name])
        }
    }

    @Test
    fun `GIVEN failed completion without puzzle id WHEN invoke THEN does not add to failedPuzzles`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        val completionResult = PuzzleCompletionResult(
            puzzleId = null,
            puzzleRating = 1450,
            wasSuccessful = false,
            ratingChange = 15,
            timeSpentMillis = 800L
        )
        fakeUserRepository.update(currentUser)

        // When
        underTest(completionResult)

        // Then
        fakeUserRepository.get().apply {
            // Failed puzzle without ID should not add anything to failedPuzzles
            assertTrue(failedPuzzles.isEmpty())
        }
    }
}
