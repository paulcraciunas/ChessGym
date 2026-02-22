package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.general.FixedRandomFactory
import com.paulcraciunas.domain.api.puzzles.GetRatedPuzzle
import com.paulcraciunas.domain.impl.general.CalculateEloImpl
import com.paulcraciunas.puzzles.api.FakePuzzleRepository
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository
import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserDefaults
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class GetRatedPuzzleImplTest {
    private val fakeUserRepository = FakeUserRepository()
    private val fixedRandomFactory = FixedRandomFactory()
    private val fakePuzzleRepository = FakePuzzleRepository()
    private val fakeAppSettingsRepository = FakeAppSettingsRepository.default()
    private val calculateElo = CalculateEloImpl()

    private val underTest = GetRatedPuzzleImpl(
        userRepository = fakeUserRepository,
        randomFactory = fixedRandomFactory,
        getPuzzleByRating = GetPuzzleByRatingImpl(fakePuzzleRepository, fakeAppSettingsRepository),
        calculateElo = calculateElo,
    )

    @Test
    fun `GIVEN user with default rating WHEN invoke THEN returns puzzle near user rating`() =
        runTest {
            // Given
            fakeUserRepository.update(UserDefaults.signedInUser())
            fixedRandomFactory.returnValue = UserDefaults.RATING
            fakePuzzleRepository.withPuzzle(DEFAULT_FEN, DEFAULT_MOVES, rating = UserDefaults.RATING)

            // When
            val result = underTest()

            // Then
            assertNotNull(result.puzzle)
            assertEquals(UserDefaults.RATING, result.puzzle.rating)
        }

    @Test
    fun `GIVEN equal user and puzzle rating WHEN invoke THEN returns symmetric elo change`() =
        runTest {
            // Given
            val rating = UserDefaults.RATING
            fakeUserRepository.update(UserDefaults.signedInUser())
            fixedRandomFactory.returnValue = rating
            fakePuzzleRepository.withPuzzle(DEFAULT_FEN, DEFAULT_MOVES, rating = rating)

            // When
            val result = underTest()

            // Then - K=32 for rating < 2100, equal ratings → expectedScore = 0.5
            assertEquals(16, result.ratingChange.potentialGain)
            assertEquals(16, result.ratingChange.potentialLoss)
        }

    @Test
    fun `GIVEN random targets lower bound WHEN invoke THEN fetches puzzle at lower rating`() =
        runTest {
            // Given
            val userRating = UserDefaults.RATING
            val targetRating = userRating - GetRatedPuzzle.DEVIATION
            fakeUserRepository.update(UserDefaults.signedInUser())
            fixedRandomFactory.returnValue = targetRating
            fakePuzzleRepository.withPuzzle(DEFAULT_FEN, DEFAULT_MOVES, rating = targetRating)

            // When
            val result = underTest()

            // Then
            assertEquals(targetRating, result.puzzle.rating)
        }

    @Test
    fun `GIVEN random targets upper bound WHEN invoke THEN fetches puzzle at higher rating`() =
        runTest {
            // Given
            val userRating = UserDefaults.RATING
            val targetRating = userRating + GetRatedPuzzle.DEVIATION - 1
            fakeUserRepository.update(UserDefaults.signedInUser())
            fixedRandomFactory.returnValue = targetRating
            fakePuzzleRepository.withPuzzle(DEFAULT_FEN, DEFAULT_MOVES, rating = targetRating)

            // When
            val result = underTest()

            // Then
            assertEquals(targetRating, result.puzzle.rating)
        }

    @Test
    fun `GIVEN puzzle rated higher than user WHEN invoke THEN gain exceeds loss`() = runTest {
        // Given
        val userRating = 1200
        val puzzleRating = 1400
        fakeUserRepository.update(User(ratings = User.Ratings(current = userRating)))
        fixedRandomFactory.returnValue = puzzleRating
        fakePuzzleRepository.withPuzzle(DEFAULT_FEN, DEFAULT_MOVES, rating = puzzleRating)

        // When
        val result = underTest()

        // Then - lower rated user gains more than they lose against stronger puzzle
        assertTrue(result.ratingChange.potentialGain > result.ratingChange.potentialLoss)
    }

    @Test
    fun `GIVEN puzzle rated lower than user WHEN invoke THEN loss exceeds gain`() = runTest {
        // Given
        val userRating = 1400
        val puzzleRating = 1200
        fakeUserRepository.update(User(ratings = User.Ratings(current = userRating)))
        fixedRandomFactory.returnValue = puzzleRating
        fakePuzzleRepository.withPuzzle(DEFAULT_FEN, DEFAULT_MOVES, rating = puzzleRating)

        // When
        val result = underTest()

        // Then - higher rated user loses more than they gain against weaker puzzle
        assertTrue(result.ratingChange.potentialLoss > result.ratingChange.potentialGain)
    }

    @Test
    fun `GIVEN user with custom rating WHEN invoke THEN elo uses actual user rating`() = runTest {
        // Given
        val customRating = 1800
        fakeUserRepository.update(User(ratings = User.Ratings(current = customRating)))
        fixedRandomFactory.returnValue = customRating
        fakePuzzleRepository.withPuzzle(DEFAULT_FEN, DEFAULT_MOVES, rating = customRating)

        // When
        val result = underTest()

        // Then - equal ratings with K=32 → symmetric gain/loss = 16
        assertEquals(16, result.ratingChange.potentialGain)
        assertEquals(16, result.ratingChange.potentialLoss)
    }

    @Test
    fun `GIVEN high rated user WHEN invoke THEN elo uses lower K-factor`() = runTest {
        // Given - rating >= 2100 uses K=24
        val highRating = 2200
        fakeUserRepository.update(User(ratings = User.Ratings(current = highRating)))
        fixedRandomFactory.returnValue = highRating
        fakePuzzleRepository.withPuzzle(DEFAULT_FEN, DEFAULT_MOVES, rating = highRating)

        // When
        val result = underTest()

        // Then - K=24, equal ratings → gain/loss = 12
        assertEquals(12, result.ratingChange.potentialGain)
        assertEquals(12, result.ratingChange.potentialLoss)
    }

    @Test
    fun `GIVEN no puzzle available WHEN invoke THEN exception propagates`() = runTest {
        // Given - empty repository, no puzzles to find
        fakeUserRepository.update(UserDefaults.signedInUser())
        fixedRandomFactory.returnValue = UserDefaults.RATING

        // When / Then
        assertThrows<IllegalArgumentException> {
            underTest()
        }
    }

    @Test
    fun `GIVEN successive invocations WHEN invoke twice THEN each uses fresh user rating`() =
        runTest {
            // Given - first call at rating 1200
            val initialRating = 1200
            fakeUserRepository.update(User(ratings = User.Ratings(current = initialRating)))
            fixedRandomFactory.returnValue = initialRating
            fakePuzzleRepository.withPuzzle(DEFAULT_FEN, DEFAULT_MOVES, rating = initialRating)

            // When
            val firstResult = underTest()

            // Then - equal ratings → symmetric
            assertEquals(16, firstResult.ratingChange.potentialGain)
            assertEquals(16, firstResult.ratingChange.potentialLoss)

            // Given - update rating before second call
            val updatedRating = 1400
            fakeUserRepository.update(User(ratings = User.Ratings(current = updatedRating)))
            fixedRandomFactory.returnValue = initialRating
            // puzzle is still at 1200, but user is now 1400

            // When
            val secondResult = underTest()

            // Then - user higher rated now → gain less, lose more
            assertTrue(secondResult.ratingChange.potentialGain < 16)
            assertTrue(secondResult.ratingChange.potentialLoss > 16)
        }

    @Test
    fun `GIVEN puzzle nearby but not exact WHEN invoke THEN returns closest available puzzle`() =
        runTest {
            // Given - random targets 1500, but puzzle only available at 1503
            val userRating = 1500
            fakeUserRepository.update(User(ratings = User.Ratings(current = userRating)))
            fixedRandomFactory.returnValue = userRating
            fakePuzzleRepository.withPuzzle(DEFAULT_FEN, DEFAULT_MOVES, rating = 1503)

            // When
            val result = underTest()

            // Then - GetPuzzleByRatingImpl expands the search and finds the puzzle
            assertEquals(1503, result.puzzle.rating)
        }

    @Test
    fun `GIVEN result WHEN invoke THEN data contains both puzzle and rating change`() = runTest {
        // Given
        fakeUserRepository.update(UserDefaults.signedInUser())
        fixedRandomFactory.returnValue = UserDefaults.RATING
        fakePuzzleRepository.withPuzzle(DEFAULT_FEN, DEFAULT_MOVES, rating = UserDefaults.RATING)

        // When
        val result = underTest()

        // Then
        assertNotNull(result.puzzle)
        assertNotNull(result.ratingChange)
        assertTrue(result.ratingChange.potentialGain > 0)
        assertTrue(result.ratingChange.potentialLoss > 0)
    }

    companion object {
        private const val DEFAULT_FEN =
            "2k3r1/pppn1p2/8/3p2N1/1P1P1q2/2P1R3/P4PPN/R5K1 b - - 0 21"
        private const val DEFAULT_MOVES = "g8g5 e3e8"
    }
}
