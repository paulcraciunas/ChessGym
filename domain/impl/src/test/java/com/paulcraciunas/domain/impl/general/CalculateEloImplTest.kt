package com.paulcraciunas.domain.impl.general

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CalculateEloImplTest {
    private val underTest = CalculateEloImpl()

    @Test
    fun `GIVEN equalRatings WHEN calculateEloChange THEN returnsSymmetricChanges`() {
        // GIVEN
        val userRating = 1400
        val puzzleRating = 1400

        // WHEN
        val result = underTest(userRating, puzzleRating)

        // THEN
        // With equal ratings, expected score should be 0.5
        // K-factor for rating 1400 should be 32 (below 2100)
        // Gain: 32 * (1.0 - 0.5) = 16
        // Loss: 32 * (0.5 - 0.0) = 16
        assertEquals(16, result.potentialGain)
        assertEquals(16, result.potentialLoss)
    }

    @Test
    fun `GIVEN userRatedHigher WHEN calculateEloChange THEN returnsSmallGainLargeLoss`() {
        // GIVEN
        val userRating = 1600
        val puzzleRating = 1400

        // WHEN
        val result = underTest(userRating, puzzleRating)

        // THEN
        // Higher rated user should gain fewer points for winning
        // but lose more points for losing
        assert(result.potentialGain < 16) { "Higher rated user should gain fewer points" }
        assert(result.potentialLoss > 16) { "Higher rated user should lose more points" }
    }

    @Test
    fun `GIVEN userRatedLower WHEN calculateEloChange THEN returnsLargeGainSmallLoss`() {
        // GIVEN
        val userRating = 1200
        val puzzleRating = 1400

        // WHEN
        val result = underTest(userRating, puzzleRating)

        // THEN
        // Lower rated user should gain more points for winning
        // but lose fewer points for losing
        assert(result.potentialGain > 16) { "Lower rated user should gain more points" }
        assert(result.potentialLoss < 16) { "Lower rated user should lose fewer points" }
    }

    @Test
    fun `GIVEN lowRatingUser WHEN calculateEloChange THEN usesHighKFactor`() {
        // GIVEN
        val userRating = 1000 // Below 2100, should use K=32
        val puzzleRating = 1000

        // WHEN
        val result = underTest(userRating, puzzleRating)

        // THEN
        // With K=32 and equal ratings (expected score = 0.5)
        assertEquals(16, result.potentialGain)
        assertEquals(16, result.potentialLoss)
    }

    @Test
    fun `GIVEN midRatingUser WHEN calculateEloChange THEN usesMidKFactor`() {
        // GIVEN
        val userRating = 2200 // Between 2100-2400, should use K=24
        val puzzleRating = 2200

        // WHEN
        val result = underTest(userRating, puzzleRating)

        // THEN
        // With K=24 and equal ratings (expected score = 0.5)
        assertEquals(12, result.potentialGain)
        assertEquals(12, result.potentialLoss)
    }

    @Test
    fun `GIVEN highRatingUser WHEN calculateEloChange THEN usesLowKFactor`() {
        // GIVEN
        val userRating = 2500 // Above 2400, should use K=16
        val puzzleRating = 2500

        // WHEN
        val result = underTest(userRating, puzzleRating)

        // THEN
        // With K=16 and equal ratings (expected score = 0.5)
        assertEquals(8, result.potentialGain)
        assertEquals(8, result.potentialLoss)
    }

    @Test
    fun `GIVEN extremeRatingDifference WHEN calculateEloChange THEN returnsExpectedChanges`() {
        // GIVEN
        val userRating = 1200
        val puzzleRating = 2000 // 800 point difference

        // WHEN
        val result = underTest(userRating, puzzleRating)

        // THEN
        // With large rating difference, user should have very low expected score
        // So potential gain should be close to maximum (32) and loss should be minimal
        assert(result.potentialGain > 30) { "Should gain almost maximum points against much higher rated puzzle" }
        assert(result.potentialLoss < 2) { "Should lose very few points against much higher rated puzzle" }
    }

    @Test
    fun `GIVEN userMuchHigherRated WHEN calculateEloChange THEN returnsMinimalGainMaximalLoss`() {
        // GIVEN
        val userRating = 2000
        val puzzleRating = 1200 // 800 point difference in favor of user

        // WHEN
        val result = underTest(userRating, puzzleRating)

        // THEN
        // With large rating difference in user's favor, expected score should be very high
        // So potential gain should be minimal and loss should be close to maximum
        assert(result.potentialGain < 2) { "Should gain very few points against much lower rated puzzle" }
        assert(result.potentialLoss > 30) { "Should lose almost maximum points against much lower rated puzzle" }
    }

    @Test
    fun `GIVEN realWorldScenario WHEN calculateEloChange THEN returnsReasonableValues`() {
        // GIVEN - A typical scenario
        val userRating = 1450
        val puzzleRating = 1550 // Puzzle is 100 points higher

        // WHEN
        val result = underTest(userRating, puzzleRating)

        // THEN
        // Verify that the values are reasonable and within expected bounds
        assert(result.potentialGain > 0) { "Should have positive potential gain" }
        assert(result.potentialLoss > 0) { "Should have positive potential loss" }
        assert(result.potentialGain <= 32) { "Gain should not exceed maximum K-factor" }
        assert(result.potentialLoss <= 32) { "Loss should not exceed maximum K-factor" }

        // Since puzzle is higher rated, user should gain more than they lose
        assert(result.potentialGain > result.potentialLoss) { "Should gain more than lose against higher rated puzzle" }
    }

    @Test
    fun `GIVEN boundaryRatings WHEN calculateEloChange THEN handlesCorrectly`() {
        // Test boundary between K-factor changes

        // GIVEN - Just below threshold
        val result1 = underTest(2099, 2099)

        // GIVEN - Just above threshold
        val result2 = underTest(2101, 2101)

        // THEN
        // K=32 vs K=24 should show different results
        assert(result1.potentialGain > result2.potentialGain) { "Lower threshold should have higher K-factor" }
        assertEquals(16, result1.potentialGain) // K=32, so 32*0.5 = 16
        assertEquals(12, result2.potentialGain) // K=24, so 24*0.5 = 12
    }

    @Test
    fun `GIVEN variousRatings WHEN calculateEloChange THEN maintainsMathematicalProperties`() {
        // Test mathematical properties of ELO system

        val testCases = listOf(
            Pair(1000, 1200),
            Pair(1500, 1500),
            Pair(2000, 1800),
            Pair(2500, 2500)
        )

        for ((userRating, puzzleRating) in testCases) {
            // WHEN
            val result = underTest(userRating, puzzleRating)

            // THEN
            // Verify basic mathematical properties
            assert(result.potentialGain >= 0) { "Potential gain should be non-negative" }
            assert(result.potentialLoss >= 0) { "Potential loss should be non-negative" }

            // The sum of changes should roughly equal the K-factor for symmetric games
            if (userRating == puzzleRating) {
                val expectedKFactor = when {
                    userRating < 2100 -> 32
                    userRating < 2400 -> 24
                    else -> 16
                }
                val halfK = expectedKFactor / 2
                assertEquals(halfK, result.potentialGain)
                assertEquals(halfK, result.potentialLoss)
            }
        }
    }
}