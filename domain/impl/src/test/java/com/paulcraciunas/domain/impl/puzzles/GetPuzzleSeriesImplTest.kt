package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.general.FixedRandomFactory
import com.paulcraciunas.domain.api.puzzles.GetPuzzleSeries
import com.paulcraciunas.puzzles.api.FakePuzzleRepository
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class GetPuzzleSeriesImplTest {
    private val repository = FakePuzzleRepository.default(ratingStart = RATING_START, increment = INCREMENT)
    private val fakeRandom = FixedRandomFactory(returnValue = INCREMENT)

    private val underTest = GetPuzzleSeriesImpl(
        getPuzzleByRating = GetPuzzleByRatingImpl(repository, FakeAppSettingsRepository.default()),
        randomFactory = fakeRandom,
    )

    @Test
    fun `GIVEN empty repository WHEN invoke THEN no puzzles are returned`() = runTest {
        // Given
        repository.clear()

        // When
        val puzzles = underTest(from = RATING_START)

        // Then
        assertEquals(0, puzzles.size)
    }

    @Test
    fun `GIVEN repository with puzzles WHEN invoke with count THEN returns requested count`() = runTest {
        // Given
        val count = 5

        // When
        val puzzles = underTest(count = count, from = RATING_START)

        // Then
        assertEquals(count, puzzles.size)
    }

    @Test
    fun `GIVEN repository WHEN invoke exceeds available puzzles THEN returns all available`() = runTest {
        // When
        val puzzles = underTest(from = RATING_START)

        // Then
        assertEquals(FakePuzzleRepository.SIZE, puzzles.size)
    }

    @Test
    fun `GIVEN custom increment WHEN invoke THEN uses increment parameter for rating progression`() = runTest {
        // Given
        val customIncrement = 100
        fakeRandom.returnValue = customIncrement

        // When
        val puzzles = underTest(count = 3, increment = customIncrement, from = RATING_START)

        // Then
        assertEquals(3, puzzles.size)
        assertEquals(RATING_START, puzzles[0].rating)
        assertEquals(RATING_START + customIncrement, puzzles[1].rating)
        assertEquals(RATING_START + 2 * customIncrement, puzzles[2].rating)
    }

    @Test
    fun `GIVEN default increment WHEN invoke THEN finds nearby puzzles via expanding search`() = runTest {
        // Given - FakePuzzleRepository has puzzles every 50 points, default increment is 60
        fakeRandom.returnValue = GetPuzzleSeries.INCREMENT

        // When
        val puzzles = underTest(count = 2, from = RATING_START)

        // Then - GetPuzzleByRating expands ±1 to find the nearest puzzle
        assertEquals(2, puzzles.size)
        assertEquals(RATING_START, puzzles[0].rating)
    }

    @Test
    fun `GIVEN puzzles WHEN invoke THEN returns puzzles with increasing ratings`() = runTest {
        // Given
        fakeRandom.returnValue = INCREMENT

        // When
        val puzzles = underTest(count = 5, increment = INCREMENT, from = RATING_START)

        // Then
        val ratings = puzzles.map { it.rating }
        assertEquals(listOf(
            RATING_START,
            RATING_START + INCREMENT,
            RATING_START + INCREMENT * 2,
            RATING_START + INCREMENT * 3,
            RATING_START + INCREMENT * 4
        ), ratings)
    }

    @Test
    fun `GIVEN gap in ratings WHEN invoke THEN expanding search bridges the gap`() = runTest {
        // Given - repository has puzzles at 1200, 1250, 1300, ... but we request rating 1210
        val gapRating = RATING_START + 10

        // When
        val puzzles = underTest(count = 1, from = gapRating)

        // Then - expanding search should find the nearest puzzle
        assertEquals(1, puzzles.size)
        assertTrue(puzzles[0].rating in (gapRating - INCREMENT)..(gapRating + INCREMENT))
    }

    @Test
    fun `GIVEN ratings beyond max WHEN invoke THEN stops gracefully`() = runTest {
        // Given - start at a high rating where no puzzles exist
        val highRating = 5000

        // When
        val puzzles = underTest(count = 5, from = highRating)

        // Then - should return empty, not crash
        assertEquals(0, puzzles.size)
    }

    @Test
    fun `GIVEN series spans into empty range WHEN invoke THEN returns partial results`() = runTest {
        // Given - repository has puzzles from 1200 to 2150 (20 puzzles, 50 apart)
        // request more than available, starting from the end
        val nearEnd = RATING_START + (FakePuzzleRepository.SIZE - 2) * INCREMENT

        // When
        val puzzles = underTest(count = 10, from = nearEnd)

        // Then - should return what's available, not crash
        assertTrue(puzzles.isNotEmpty())
        assertTrue(puzzles.size < 10)
    }

    private companion object {
        const val RATING_START = 1200
        const val INCREMENT = 50
    }
}
