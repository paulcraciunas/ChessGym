package com.paulcraciunas.settings.user

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserStatsRepositoryTest {
    private lateinit var context: Context
    private lateinit var underTest: UserStatsRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        underTest = DataStoreUserStatsRepository(context)
    }

    @Test
    fun given_noInitialData_WHEN_gettingUserStats_THEN_returnsDefaultValues() = runBlocking {
        // WHEN
        val stats = underTest.userStats.first()

        // THEN
        assertEquals(0, stats.puzzlesPlayed)
        assertEquals(0, stats.puzzlesSolved)
        assertEquals(1200, stats.currentRating)
        assertEquals(1200, stats.bestRating)
        assertEquals(0, stats.bestPuzzleRushScore)
        assertEquals(0, stats.bestBlindModeScore)
        assertEquals(0, stats.bestVisualizationScore)
    }

    @Test
    fun given_noInitialData_WHEN_updatingPuzzlesPlayed_THEN_updatesValueCorrectly() = runBlocking {
        // WHEN
        underTest.updatePuzzlesPlayed(5)

        // THEN
        val stats = underTest.userStats.first()
        assertEquals(5, stats.puzzlesPlayed)
    }

    @Test
    fun given_noInitialData_WHEN_updatingPuzzlesSolved_THEN_updatesValueCorrectly() = runBlocking {
        // WHEN
        underTest.updatePuzzlesSolved(3)

        // THEN
        val stats = underTest.userStats.first()
        assertEquals(3, stats.puzzlesSolved)
    }

    @Test
    fun given_noInitialData_WHEN_updatingCurrentRatingWithHigherValue_THEN_updatesBothCurrentAndBestRating() = runBlocking {
        // WHEN
        underTest.updateCurrentRating(1500)

        // THEN
        val stats = underTest.userStats.first()
        assertEquals(1500, stats.currentRating)
        assertEquals(1500, stats.bestRating)
    }

    @Test
    fun given_bestRatingOf1500_WHEN_updatingCurrentRatingWithLowerValue_THEN_onlyUpdatesCurrentRating() = runBlocking {
        // GIVEN
        underTest.updateCurrentRating(1500)
        val initialStats = underTest.userStats.first()
        assertEquals(1500, initialStats.bestRating)

        // WHEN
        underTest.updateCurrentRating(1300)

        // THEN
        val stats = underTest.userStats.first()
        assertEquals(1300, stats.currentRating)
        assertEquals(1500, stats.bestRating)
    }

    @Test
    fun given_bestPuzzleRushScoreOf100_WHEN_updatingWithLowerScore_THEN_keepsHigherScore() = runBlocking {
        // GIVEN
        underTest.updateBestPuzzleRushScore(100)
        val initialStats = underTest.userStats.first()
        assertEquals(100, initialStats.bestPuzzleRushScore)

        // WHEN
        underTest.updateBestPuzzleRushScore(50)

        // THEN
        val stats = underTest.userStats.first()
        assertEquals(100, stats.bestPuzzleRushScore)
    }

    @Test
    fun given_bestBlindModeScoreOf200_WHEN_updatingWithLowerScore_THEN_keepsHigherScore() = runBlocking {
        // GIVEN
        underTest.updateBestBlindModeScore(200)
        val initialStats = underTest.userStats.first()
        assertEquals(200, initialStats.bestBlindModeScore)

        // WHEN
        underTest.updateBestBlindModeScore(150)

        // THEN
        val stats = underTest.userStats.first()
        assertEquals(200, stats.bestBlindModeScore)
    }

    @Test
    fun given_bestVisualizationScoreOf300_WHEN_updatingWithLowerScore_THEN_keepsHigherScore() = runBlocking {
        // GIVEN
        underTest.updateBestVisualizationScore(300)
        val initialStats = underTest.userStats.first()
        assertEquals(300, initialStats.bestVisualizationScore)

        // WHEN
        underTest.updateBestVisualizationScore(250)

        // THEN
        val stats = underTest.userStats.first()
        assertEquals(300, stats.bestVisualizationScore)
    }

    @Test
    fun given_noInitialData_WHEN_updatingMultipleStats_THEN_allStatsAreUpdatedCorrectly() = runBlocking {
        // WHEN
        underTest.updatePuzzlesPlayed(10)
        underTest.updatePuzzlesSolved(7)
        underTest.updateCurrentRating(1600)
        underTest.updateBestPuzzleRushScore(150)
        underTest.updateBestBlindModeScore(250)
        underTest.updateBestVisualizationScore(350)

        // THEN
        val stats = underTest.userStats.first()
        assertEquals(10, stats.puzzlesPlayed)
        assertEquals(7, stats.puzzlesSolved)
        assertEquals(1600, stats.currentRating)
        assertEquals(1600, stats.bestRating)
        assertEquals(150, stats.bestPuzzleRushScore)
        assertEquals(250, stats.bestBlindModeScore)
        assertEquals(350, stats.bestVisualizationScore)
    }
} 