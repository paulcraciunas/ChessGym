package com.paulcraciunas.domain.impl

import com.paulcraciunas.domain.api.GetStreakPuzzle
import com.paulcraciunas.puzzles.api.FakePuzzleRepository
import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserDefaults
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class GetStreakPuzzleImplTest {
    private val fakeUserRepository = FakeUserRepository()
    private val fakePuzzleRepository = FakePuzzleRepository()
    private val underTest = GetStreakPuzzleImpl(fakeUserRepository, fakePuzzleRepository)

    @BeforeEach
    fun setUp() {
        fakePuzzleRepository.clear()
    }

    @Test
    fun `GIVEN no active streak WHEN invoke THEN returns puzzle at base rating`() = runTest {
        // Given
        val user = UserDefaults.signedInUser().copy(
            puzzleStreak = User.PuzzleStreak(currentCount = 0, lastPuzzleId = null)
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
    fun `GIVEN active streak of 5 WHEN invoke THEN returns puzzle at calculated rating`() = runTest {
        // Given
        val streakCount = 5
        val expectedRating = GetStreakPuzzle.BASE_RATING + (streakCount * GetStreakPuzzle.RATING_INCREMENT)
        val user = UserDefaults.signedInUser().copy(
            puzzleStreak = User.PuzzleStreak(currentCount = streakCount, lastPuzzleId = 42)
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
    fun `GIVEN no puzzle available WHEN invoke THEN throws exception`() = runTest {
        // Given
        val user = UserDefaults.signedInUser().copy(
            puzzleStreak = User.PuzzleStreak(currentCount = 0, lastPuzzleId = null)
        )
        fakeUserRepository.update(user)
        // No puzzle added to repository

        // When/Then
        assertThrows(IllegalStateException::class.java) {
            runTest { underTest() }
        }
    }

    @Test
    fun `GIVEN streak of 10 WHEN invoke THEN rating increases accordingly`() = runTest {
        // Given
        val streakCount = 10
        val expectedRating = GetStreakPuzzle.BASE_RATING + (streakCount * GetStreakPuzzle.RATING_INCREMENT)
        val user = UserDefaults.signedInUser().copy(
            puzzleStreak = User.PuzzleStreak(currentCount = streakCount, lastPuzzleId = 99)
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
