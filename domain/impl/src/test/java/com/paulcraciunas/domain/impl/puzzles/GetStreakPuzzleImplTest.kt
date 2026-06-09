package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.puzzles.GetStreakPuzzle
import com.paulcraciunas.domain.api.puzzles.NoPuzzleException
import com.paulcraciunas.puzzles.api.FakePuzzleRepository
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository
import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserDefaults
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalCoroutinesApi::class)
internal class GetStreakPuzzleImplTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val fakeUserRepository = FakeUserRepository()
    private val fakePuzzleRepository = FakePuzzleRepository()
    private val underTest = GetStreakPuzzleImpl(
        userRepository = fakeUserRepository,
        puzzleRepository = fakePuzzleRepository,
        getPuzzleByRating = GetPuzzleByRatingImpl(fakePuzzleRepository, FakeAppSettingsRepository.default()),
        dispatcher = testDispatcher,
    )

    @Test
    fun `GIVEN no active streak WHEN invoke THEN returns puzzle at base rating`() = runTest {
        // Given
        val user = UserDefaults.signedInUser().copy(
            ratings = UserDefaults.signedInUser().ratings.copy(
                puzzleStreak = User.PuzzleStreak(currentCount = 0, lastPuzzleId = null)
            )
        )
        fakeUserRepository.update(user)
        fakePuzzleRepository.withPuzzle(
            fen = "2k3r1/pppn1p2/8/3p2N1/1P1P1q2/2P1R3/P4PPN/R5K1 b - - 0 21",
            moves = "g8g5 e3e8",
            rating = GetStreakPuzzle.BASE_RATING
        )

        // When
        val result = underTest()

        // Then
        assertEquals(0, result.currentStreakCount)
        assertEquals(GetStreakPuzzle.BASE_RATING, result.puzzle.rating)
    }

    @Test
    fun `GIVEN no active streak WHEN invoke THEN saves lastPuzzleId`() = runTest {
        // Given
        val user = UserDefaults.signedInUser().copy(
            ratings = UserDefaults.signedInUser().ratings.copy(
                puzzleStreak = User.PuzzleStreak(currentCount = 0, lastPuzzleId = null)
            )
        )
        fakeUserRepository.update(user)
        fakePuzzleRepository.withPuzzle(
            fen = "2k3r1/pppn1p2/8/3p2N1/1P1P1q2/2P1R3/P4PPN/R5K1 b - - 0 21",
            moves = "g8g5 e3e8",
            rating = GetStreakPuzzle.BASE_RATING
        )

        // When
        val result = underTest()

        // Then
        val updatedUser = fakeUserRepository.get()
        assertEquals(result.puzzle.id, updatedUser.ratings.puzzleStreak.lastPuzzleId)
    }

    @Test
    fun `GIVEN lastPuzzleId exists WHEN invoke THEN returns that puzzle`() = runTest {
        // Given
        val savedPuzzleId = 1
        fakePuzzleRepository.withPuzzle(
            fen = "2k3r1/pppn1p2/8/3p2N1/1P1P1q2/2P1R3/P4PPN/R5K1 b - - 0 21",
            moves = "g8g5 e3e8",
            rating = 500
        )
        val user = UserDefaults.signedInUser().copy(
            ratings = UserDefaults.signedInUser().ratings.copy(
                puzzleStreak = User.PuzzleStreak(currentCount = 5, lastPuzzleId = savedPuzzleId)
            )
        )
        fakeUserRepository.update(user)

        // When
        val result = underTest()

        // Then
        assertEquals(savedPuzzleId, result.puzzle.id)
        assertEquals(5, result.currentStreakCount)
    }

    @Test
    fun `GIVEN lastPuzzleId exists but puzzle not found WHEN invoke THEN fetches new puzzle`() = runTest {
        // Given
        val streakCount = 5
        val expectedRating = GetStreakPuzzle.BASE_RATING + (streakCount * GetStreakPuzzle.RATING_INCREMENT)
        val user = UserDefaults.signedInUser().copy(
            ratings = UserDefaults.signedInUser().ratings.copy(
                puzzleStreak = User.PuzzleStreak(currentCount = streakCount, lastPuzzleId = 999)
            )
        )
        fakeUserRepository.update(user)
        fakePuzzleRepository.withPuzzle(
            fen = "7k/1Q1q2pp/p4r2/5p2/3p1P2/3P4/6PP/4R2K b - - 0 31",
            moves = "d7b7 e1e8 f6f8 e8f8",
            rating = expectedRating
        )

        // When
        val result = underTest()

        // Then
        assertEquals(streakCount, result.currentStreakCount)
        assertEquals(expectedRating, result.puzzle.rating)
    }

    @Test
    fun `GIVEN active streak of 5 without lastPuzzleId WHEN invoke THEN returns puzzle at calculated rating`() = runTest {
        // Given
        val streakCount = 5
        val expectedRating = GetStreakPuzzle.BASE_RATING + (streakCount * GetStreakPuzzle.RATING_INCREMENT)
        val user = UserDefaults.signedInUser().copy(
            ratings = UserDefaults.signedInUser().ratings.copy(
                puzzleStreak = User.PuzzleStreak(currentCount = streakCount, lastPuzzleId = null)
            )
        )
        fakeUserRepository.update(user)
        fakePuzzleRepository.withPuzzle(
            fen = "7k/1Q1q2pp/p4r2/5p2/3p1P2/3P4/6PP/4R2K b - - 0 31",
            moves = "d7b7 e1e8 f6f8 e8f8",
            rating = expectedRating
        )

        // When
        val result = underTest()

        // Then
        assertEquals(streakCount, result.currentStreakCount)
        assertEquals(expectedRating, result.puzzle.rating)
    }

    @Test
    fun `GIVEN no puzzle available WHEN invoke THEN throws NoPuzzleException`() = runTest {
        // Given
        val user = UserDefaults.signedInUser().copy(
            ratings = UserDefaults.signedInUser().ratings.copy(
                puzzleStreak = User.PuzzleStreak(currentCount = 0, lastPuzzleId = null)
            )
        )
        fakeUserRepository.update(user)

        // Then
        assertThrows<NoPuzzleException> {
            underTest()
        }
    }

    @Test
    fun `GIVEN streak of 10 WHEN invoke THEN rating increases accordingly`() = runTest {
        // Given
        val streakCount = 10
        val expectedRating = GetStreakPuzzle.BASE_RATING + (streakCount * GetStreakPuzzle.RATING_INCREMENT)
        val user = UserDefaults.signedInUser().copy(
            ratings = UserDefaults.signedInUser().ratings.copy(
                puzzleStreak = User.PuzzleStreak(currentCount = streakCount, lastPuzzleId = null)
            )
        )
        fakeUserRepository.update(user)
        fakePuzzleRepository.withPuzzle(
            fen = "6k1/6r1/1R3K2/8/8/8/8/8 b - - 8 76",
            moves = "g8f8 b6b8",
            rating = expectedRating
        )

        // When
        val result = underTest()

        // Then
        assertEquals(streakCount, result.currentStreakCount)
        assertEquals(expectedRating, result.puzzle.rating)
    }
}
