package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.puzzles.api.FakePuzzleRepository
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class GetPuzzleByRatingImplTest {
    private val fakePuzzleRepository = FakePuzzleRepository()
    private val fakeAppSettingsRepository = FakeAppSettingsRepository.default()

    private val underTest = GetPuzzleByRatingImpl(
        puzzleRepository = fakePuzzleRepository,
        appSettingsRepository = fakeAppSettingsRepository
    )

    @Test
    fun `GIVEN exactRatingMatch WHEN invoke THEN returnsExactMatch`() = runTest {
        // GIVEN
        val targetRating = 1500
        fakePuzzleRepository.withPuzzle(
            fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            moves = "e4",
            rating = targetRating
        )

        // WHEN
        val result = underTest(targetRating)

        // THEN
        assertEquals(targetRating, result.rating)
    }

    @Test
    fun `GIVEN noExactMatchButPuzzleInRange WHEN invoke THEN returnsPuzzleInRange`() = runTest {
        // GIVEN
        val targetRating = 1500
        val puzzleInRange = 1501
        fakePuzzleRepository.withPuzzle(
            fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            moves = "e4",
            rating = puzzleInRange
        )

        // WHEN
        val result = underTest(targetRating)

        // THEN
        assertEquals(puzzleInRange, result.rating)
    }

    @Test
    fun `GIVEN noExactMatchButPuzzleBelowTarget WHEN invoke THEN returnsPuzzleBelowTarget`() = runTest {
        // GIVEN
        val targetRating = 1500
        val puzzleBelow = 1499
        fakePuzzleRepository.withPuzzle(
            fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            moves = "e4",
            rating = puzzleBelow
        )

        // WHEN
        val result = underTest(targetRating)

        // THEN
        assertEquals(puzzleBelow, result.rating)
    }

    @Test
    fun `GIVEN noExactMatchButPuzzleAboveTarget WHEN invoke THEN returnsPuzzleAboveTarget`() = runTest {
        // GIVEN
        val targetRating = 1500
        val puzzleAbove = 1501
        fakePuzzleRepository.withPuzzle(
            fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            moves = "e4",
            rating = puzzleAbove
        )

        // WHEN
        val result = underTest(targetRating)

        // THEN
        assertEquals(puzzleAbove, result.rating)
    }

    @Test
    fun `GIVEN noExactMatchButPuzzleInExpandedRange WHEN invoke THEN returnsPuzzleInExpandedRange`() = runTest {
        // GIVEN
        val targetRating = 1500
        val puzzleInExpandedRange = 1502
        fakePuzzleRepository.withPuzzle(
            fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            moves = "e4",
            rating = puzzleInExpandedRange
        )

        // WHEN
        val result = underTest(targetRating)

        // THEN
        assertEquals(puzzleInExpandedRange, result.rating)
    }

    @Test
    fun `GIVEN noExactMatchButPuzzleInFurtherExpandedRange WHEN invoke THEN returnsPuzzleInFurtherExpandedRange`() = runTest {
        // GIVEN
        val targetRating = 1500
        val puzzleInFurtherRange = 1503
        fakePuzzleRepository.withPuzzle(
            fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            moves = "e4",
            rating = puzzleInFurtherRange
        )

        // WHEN
        val result = underTest(targetRating)

        // THEN
        assertEquals(puzzleInFurtherRange, result.rating)
    }

    @Test
    fun `GIVEN noExactMatchButPuzzleBelowInExpandedRange WHEN invoke THEN returnsPuzzleBelowInExpandedRange`() = runTest {
        // GIVEN
        val targetRating = 1500
        val puzzleBelowInRange = 1498
        fakePuzzleRepository.withPuzzle(
            fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            moves = "e4",
            rating = puzzleBelowInRange
        )

        // WHEN
        val result = underTest(targetRating)

        // THEN
        assertEquals(puzzleBelowInRange, result.rating)
    }

    @Test
    fun `GIVEN noExactMatchButPuzzleBelowInFurtherExpandedRange WHEN invoke THEN returnsPuzzleBelowInFurtherExpandedRange`() = runTest {
        // GIVEN
        val targetRating = 1500
        val puzzleBelowInFurtherRange = 1497
        fakePuzzleRepository.withPuzzle(
            fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            moves = "e4",
            rating = puzzleBelowInFurtherRange
        )

        // WHEN
        val result = underTest(targetRating)

        // THEN
        assertEquals(puzzleBelowInFurtherRange, result.rating)
    }

    @Test
    fun `GIVEN noPuzzleFoundInRange WHEN invoke THEN throwsIllegalArgumentException`() = runTest {
        // GIVEN
        val targetRating = 1500

        // WHEN & THEN
        assertThrows<IllegalArgumentException> {
            underTest(targetRating)
        }
    }

    @Test
    fun `GIVEN targetRatingAtMinBoundary WHEN invoke THEN respectsMinBoundary`() = runTest {
        // GIVEN
        val targetRating = 400
        val puzzleAtMin = 400
        fakePuzzleRepository.withPuzzle(
            fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            moves = "e4",
            rating = puzzleAtMin
        )

        // WHEN
        val result = underTest(targetRating)

        // THEN
        assertEquals(puzzleAtMin, result.rating)
    }

    @Test
    fun `GIVEN targetRatingAtMaxBoundary WHEN invoke THEN respectsMaxBoundary`() = runTest {
        // GIVEN
        val targetRating = 2500
        val puzzleAtMax = 2500
        fakePuzzleRepository.withPuzzle(
            fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            moves = "e4",
            rating = puzzleAtMax
        )

        // WHEN
        val result = underTest(targetRating)

        // THEN
        assertEquals(puzzleAtMax, result.rating)
    }

    @Test
    fun `GIVEN targetRatingBelowMinBoundary WHEN invoke THEN throwsIllegalArgumentException`() = runTest {
        // GIVEN
        val targetRating = 300 // Below min rating of 400

        // WHEN & THEN
        assertThrows<IllegalArgumentException> {
            underTest(targetRating)
        }
    }

    @Test
    fun `GIVEN targetRatingAboveMaxBoundary WHEN invoke THEN throwsIllegalArgumentException`() = runTest {
        // GIVEN
        val targetRating = 2600 // Above max rating of 2500

        // WHEN & THEN
        assertThrows<IllegalArgumentException> {
            underTest(targetRating)
        }
    }

    @Test
    fun `GIVEN multiplePuzzlesInRange WHEN invoke THEN returnsFirstFoundPuzzle`() = runTest {
        // GIVEN
        val targetRating = 1500
        val firstPuzzle = 1501
        val secondPuzzle = 1499

        fakePuzzleRepository.withPuzzle(
            fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            moves = "e4",
            rating = firstPuzzle
        )
        fakePuzzleRepository.withPuzzle(
            fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            moves = "d4",
            rating = secondPuzzle
        )


        // WHEN
        val result = underTest(targetRating)

        // THEN
        // Should return the first puzzle found in the range (1501)
        assertEquals(firstPuzzle, result.rating)
    }

    @Test
    fun `GIVEN systematicExpansion WHEN invoke THEN expandsCorrectly`() = runTest {
        // GIVEN
        val targetRating = 1500
        val puzzleFoundAtExpansion = 1503

        // No puzzle at 1500, 1501, 1502, but found at 1503
        fakePuzzleRepository.withPuzzle(
            fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            moves = "e4",
            rating = puzzleFoundAtExpansion
        )


        // WHEN
        val result = underTest(targetRating)

        // THEN
        // Should find puzzle at rating 1503 after expanding ±3
        assertEquals(puzzleFoundAtExpansion, result.rating)
    }

    @Test
    fun `GIVEN expansionReachesBoundaries WHEN invoke THEN stopsAtBoundaries`() = runTest {
        // GIVEN
        val targetRating = 1500
        fakeAppSettingsRepository.updateMaxPuzzleRating(1502) // Close to target
        fakeAppSettingsRepository.updateMinPuzzleRating(1498) // Close to target

        // WHEN & THEN
        assertThrows<IllegalArgumentException> {
            underTest(targetRating)
        }
    }

    @Test
    fun `GIVEN differentAppSettings WHEN invoke THEN usesCorrectSettings`() = runTest {
        // GIVEN
        val targetRating = 1500
        val existingPuzzleRating = 1501
        fakePuzzleRepository.withPuzzle(
            fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            moves = "e4",
            rating = existingPuzzleRating
        )

        // WHEN
        val result = underTest(targetRating)

        // THEN
        assertEquals(existingPuzzleRating, result.rating)
    }
}
