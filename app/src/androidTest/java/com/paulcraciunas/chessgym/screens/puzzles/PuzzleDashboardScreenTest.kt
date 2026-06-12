package com.paulcraciunas.chessgym.screens.puzzles

import com.paulcraciunas.chessgym.base.BaseUiTest
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Test

@HiltAndroidTest
internal class PuzzleDashboardScreenTest : BaseUiTest() {

    @Before
    override fun setUp() {
        super.setUp()
        Given.settings.puzzlesDownloaded()
        Given.user.isDefault()
    }

    @Test
    fun GIVEN_default_user_WHEN_navigated_to_dashboard_THEN_shows_all_cards() {
        When.app.launch()
        When.navigation.navigateToPuzzles()

        Then.puzzleDashboard
            .isDisplayed()
            .hasAllCards()
    }

    @Test
    fun GIVEN_light_mode_WHEN_navigated_to_dashboard_THEN_shows_light_background() {
        Given.settings.lightMode()

        When.app.launch()
        When.navigation.navigateToPuzzles()

        Then.puzzleDashboard.isDisplayed()
        Then.theme.isLightMode()
    }

    @Test
    fun GIVEN_dark_mode_WHEN_navigated_to_dashboard_THEN_shows_dark_background() {
        Given.settings.darkMode()

        When.app.launch()
        When.navigation.navigateToPuzzles()

        Then.puzzleDashboard.isDisplayed()
        Then.theme.isDarkMode()
    }

    @Test
    fun GIVEN_no_failed_puzzles_WHEN_on_dashboard_THEN_failed_card_is_not_clickable() {
        When.app.launch()
        When.navigation.navigateToPuzzles()

        Then.puzzleDashboard.failedPuzzlesCardIsNotEnabled()
    }

    @Test
    fun GIVEN_failed_puzzles_exist_WHEN_on_dashboard_THEN_failed_card_is_clickable() {
        Given.user.withFailedPuzzles(101, 102, 103)

        When.app.launch()
        When.navigation.navigateToPuzzles()

        Then.puzzleDashboard.failedPuzzlesCardIsEnabled()
    }

    @Test
    fun WHEN_tapping_rated_puzzle_card_THEN_navigates_to_rated_puzzle() {
        Given.puzzle.withRating(PUZZLE_RATING)

        When.app.launch()
        When.navigation.navigateToPuzzles()
        When.puzzleDashboard.openRatedPuzzle()

        Then.ratedPuzzle.isDisplayed()
    }

    @Test
    fun WHEN_tapping_puzzle_rush_card_THEN_navigates_to_puzzle_rush() {
        Given.puzzle.withRating(PUZZLE_RATING)

        When.app.launch()
        When.navigation.navigateToPuzzles()
        When.puzzleDashboard.openPuzzleRush()

        Then.puzzleRush.isDisplayed()
    }

    @Test
    fun WHEN_tapping_puzzle_streak_card_THEN_navigates_to_puzzle_streak() {
        Given.puzzle.withRating(PUZZLE_RATING)

        When.app.launch()
        When.navigation.navigateToPuzzles()
        When.puzzleDashboard.openPuzzleStreak()

        Then.puzzleStreak.isDisplayed()
    }

    @Test
    fun WHEN_tapping_failed_puzzles_card_THEN_navigates_to_failed_puzzles() {
        Given.user.withFailedPuzzles(101, 102)

        When.app.launch()
        When.navigation.navigateToPuzzles()
        When.puzzleDashboard.openFailedPuzzles()

        Then.failedPuzzles.isDisplayed()
    }

    @Test
    fun WHEN_navigating_to_home_tab_THEN_shows_home_screen() {
        When.app.launch()
        When.navigation.navigateToPuzzles()
        Then.puzzleDashboard.isDisplayed()

        When.navigation.navigateToHome()

        Then.homeScreen.isDisplayed()
    }

    @Test
    fun WHEN_navigating_to_board_vis_tab_THEN_shows_board_vis_dashboard() {
        When.app.launch()
        When.navigation.navigateToPuzzles()
        Then.puzzleDashboard.isDisplayed()

        When.navigation.navigateToBoardVis()

        Then.boardVisDashboard.isDisplayed()
    }

    @Test
    fun WHEN_navigating_to_blind_mode_tab_THEN_shows_blind_mode_screen() {
        When.app.launch()
        When.navigation.navigateToPuzzles()
        Then.puzzleDashboard.isDisplayed()

        When.navigation.navigateToBlindMode()

        Then.blindMode.isDisplayed()
    }

    @Test
    fun WHEN_opening_drawer_THEN_drawer_is_displayed() {
        When.app.launch()
        When.navigation.navigateToPuzzles()

        When.navigation.openDrawer()

        Then.navigation.drawerIsOpen()
    }

    @Test
    fun GIVEN_on_puzzle_screen_WHEN_going_back_THEN_returns_to_dashboard() {
        Given.puzzle.withRating(PUZZLE_RATING)

        When.app.launch()
        When.navigation.navigateToPuzzles()
        When.puzzleDashboard.openPuzzleRush()
        Then.puzzleRush.isDisplayed()

        When.navigation.goBack()

        Then.puzzleDashboard.isDisplayed()
    }

    private companion object {
        const val PUZZLE_RATING: Int = 1200
    }
}
