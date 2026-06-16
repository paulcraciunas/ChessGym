package com.paulcraciunas.chessgym.screens.home

import com.paulcraciunas.chessgym.base.BaseUiTest
import com.paulcraciunas.chessgym.dsl.setup.Puzzles
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@HiltAndroidTest
internal class HomeScreenTest : BaseUiTest() {

    @Before
    override fun setUp() {
        super.setUp()
        Given.settings.puzzlesDownloaded()
    }

    @Test
    fun GIVEN_default_user_WHEN_app_launched_THEN_shows_home_with_defaults() {
        Given.user.isDefault()

        When.app.launch()

        Then.homeScreen
            .isDisplayed()
            .hasProfileCard()
            .hasProfileInfo(name = "ChessEnthusiast", initial = "C", rating = 1200)
    }

    @Test
    fun GIVEN_empty_user_WHEN_app_launched_THEN_shows_home_with_defaults() {
        Given.user.isEmpty()

        When.app.launch()

        Then.homeScreen
            .isDisplayed()
            .hasProfileCard()
            .hasProfileInfo(name = "ChessEnthusiast", initial = "C", rating = 1000)
    }

    @Test
    fun GIVEN_custom_user_WHEN_app_launched_THEN_shows_correct_profile() {
        Given.user.isLoaded(
            displayName = "MagnusCarlsen",
            rating = 2882,
            joinDate = LocalDate.of(2024, 1, 15),
        )

        When.app.launch()

        Then.homeScreen
            .isDisplayed()
            .hasProfileInfo(name = "MagnusCarlsen", initial = "M", rating = 2882)
    }

    @Test
    fun GIVEN_default_user_WHEN_app_launched_THEN_shows_empty_timeline() {
        When.app.launch()

        Then.homeScreen
            .isDisplayed()
            .hasEmptyTimeline()
    }

    @Test
    fun GIVEN_user_with_history_WHEN_app_launched_THEN_shows_timeline() {
        Given.user.withHistory(
            Puzzles.historyItem(tries = 5, score = 18),
            Puzzles.ratedItem(played = 12, solved = 10)
        )

        When.app.launch()

        Then.homeScreen
            .isDisplayed()
            .hasTimeline()
    }

    @Test
    fun GIVEN_user_with_stats_WHEN_app_launched_THEN_shows_stats_card() {
        Given.user.withStatistics(
            puzzlesPlayed = 142,
            puzzlesSolved = 108,
        )

        When.app.launch()

        Then.homeScreen
            .isDisplayed()
            .hasStatsCard()
    }

    @Test
    fun GIVEN_user_with_high_scores_WHEN_app_launched_THEN_shows_high_scores_card() {
        Given.user.withHighScores(
            ratedPuzzle = 1623,
            puzzleRush = 23,
            puzzleStreak = 15,
        )

        When.app.launch()

        Then.homeScreen
            .isDisplayed()
            .hasHighScoresCard()
    }

    @Test
    fun WHEN_expanding_stats_THEN_shows_detailed_stats() {
        When.app.launch()

        Then.homeScreen.hasStatsCollapsed()

        When.homeScreen.expandStats()
        Then.homeScreen.hasStatsExpanded()

        When.homeScreen.collapseStats()
        Then.homeScreen.hasStatsCollapsed()
    }

    @Test
    fun WHEN_expanding_high_scores_THEN_shows_detailed_high_scores() {
        When.app.launch()

        Then.homeScreen.hasHighScoresCollapsed()

        When.homeScreen.expandHighScores()
        Then.homeScreen.hasHighScoresExpanded()

        When.homeScreen.collapseHighScores()
        Then.homeScreen.hasHighScoresCollapsed()
    }

    @Test
    fun WHEN_user_rating_updates_THEN_ui_reflects_change() {
        Given.user.isLoaded(rating = 1200)

        When.app.launch()

        Then.homeScreen.hasProfileRating(1200)

        Given.user.withRating(1500)
        composeRule.waitForIdle()

        Then.homeScreen.hasProfileRating(1500)
    }

    @Test
    fun WHEN_loading_dark_mode_THEN_ui_shows_correctly() {
        Given.settings.darkMode()

        When.app.launch()
        Then.theme.isDarkMode()
    }

    @Test
    fun WHEN_loading_light_mode_THEN_ui_shows_correctly() {
        Given.settings.lightMode()

        When.app.launch()
        Then.theme.isLightMode()
    }

    @Test
    fun WHEN_navigating_to_puzzles_THEN_shows_puzzle_dashboard() {
        When.app.launch()

        When.navigation.navigateToPuzzles()

        Then.puzzleDashboard.isDisplayed()
    }
}
