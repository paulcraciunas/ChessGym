package com.paulcraciunas.chessgym.screens.puzzles

import com.paulcraciunas.chessgym.base.BaseUiTest
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Test

@HiltAndroidTest
internal class FailedPuzzlesScreenTest : BaseUiTest() {

    @Before
    override fun setUp() {
        super.setUp()
        Given.settings
            .puzzlesDownloaded()
            .noAnimations()
        Given.user.isDefault()
    }

    @Test
    fun GIVEN_puzzle_loaded_WHEN_opened_THEN_shows_failed_puzzles_screen() {
        Given.user.withFailedPuzzles(PUZZLE_ID)

        When.app.launch()
        When.navigation.navigateToPuzzles()
        When.failedPuzzles.open()

        Then.failedPuzzles
            .isDisplayed()
            .isPlaying()
            .hasBackButton()
    }

    @Test
    fun GIVEN_light_mode_WHEN_opened_THEN_shows_light_background() {
        Given.settings.lightMode()
        Given.user.withFailedPuzzles(PUZZLE_ID)

        When.app.launch()
        When.navigation.navigateToPuzzles()
        When.failedPuzzles.open()

        Then.failedPuzzles.isDisplayed()
        Then.theme.isLightMode()
    }

    @Test
    fun GIVEN_dark_mode_WHEN_opened_THEN_shows_dark_background() {
        Given.settings.darkMode()
        Given.user.withFailedPuzzles(PUZZLE_ID)

        When.app.launch()
        When.navigation.navigateToPuzzles()
        When.failedPuzzles.open()

        Then.failedPuzzles.isDisplayed()
        Then.theme.isDarkMode()
    }

    @Test
    fun WHEN_playing_all_correct_moves_THEN_shows_completion_dialog() {
        Given.user.withFailedPuzzles(PUZZLE_ID)

        When.app.launch()
        When.navigation.navigateToPuzzles()
        When.failedPuzzles
            .open()
            .playToEnd()

        Then.failedPuzzles.completionDialogIsShown()
    }

    @Test
    fun WHEN_playing_wrong_move_THEN_shows_completion_dialog() {
        Given.user.withFailedPuzzles(PUZZLE_ID)

        When.app.launch()
        When.navigation.navigateToPuzzles()
        When.failedPuzzles
            .open()
            .playWrongMove()

        Then.failedPuzzles.completionDialogIsShown()
    }

    @Test
    fun GIVEN_completion_dialog_shown_WHEN_dismissing_THEN_dialog_is_dismissed() {
        Given.user.withFailedPuzzles(PUZZLE_ID)

        When.app.launch()
        When.navigation.navigateToPuzzles()
        When.failedPuzzles
            .open()
            .playToEnd()
        Then.failedPuzzles.completionDialogIsShown()

        When.failedPuzzles.dismissCompletion()
        Then.failedPuzzles.completionDialogIsDismissed()
    }

    @Test
    fun GIVEN_playing_WHEN_pressing_back_THEN_returns_to_dashboard() {
        Given.user.withFailedPuzzles(PUZZLE_ID)

        When.app.launch()
        When.navigation.navigateToPuzzles()
        When.failedPuzzles.open()
        Then.failedPuzzles.isPlaying()

        When.navigation.goBack()

        Then.puzzleDashboard.isDisplayed()
    }

    @Test
    fun GIVEN_finished_WHEN_pressing_back_THEN_returns_to_dashboard() {
        Given.user.withFailedPuzzles(PUZZLE_ID)

        When.app.launch()
        When.navigation.navigateToPuzzles()
        When.failedPuzzles
            .open()
            .playToEnd()
        Then.failedPuzzles.completionDialogIsShown()

        When.failedPuzzles.dismissCompletion()
        When.navigation.goBack()

        Then.puzzleDashboard.isDisplayed()
    }

    @Test
    fun GIVEN_all_puzzles_solved_WHEN_returning_to_dashboard_THEN_failed_card_not_clickable() {
        Given.user.withFailedPuzzles(PUZZLE_ID)

        When.app.launch()
        When.navigation.navigateToPuzzles()
        When.failedPuzzles
            .open()
            .playToEnd()
        Then.failedPuzzles.completionDialogIsShown()

        When.failedPuzzles.dismissCompletion()
        When.navigation.goBack()

        Then.puzzleDashboard
            .isDisplayed()
            .failedPuzzlesCardIsNotEnabled()
    }

    @Test
    fun GIVEN_puzzle_failed_again_WHEN_returning_to_dashboard_THEN_failed_card_still_clickable() {
        Given.user.withFailedPuzzles(PUZZLE_ID)

        When.app.launch()
        When.navigation.navigateToPuzzles()
        When.failedPuzzles
            .open()
            .playWrongMove()
        Then.failedPuzzles.completionDialogIsShown()

        When.failedPuzzles.dismissCompletion()
        When.navigation.goBack()

        Then.puzzleDashboard
            .isDisplayed()
            .failedPuzzlesCardIsEnabled()
    }

    @Test
    fun GIVEN_no_failed_puzzles_WHEN_on_dashboard_THEN_failed_card_is_not_clickable() {
        When.app.launch()
        When.navigation.navigateToPuzzles()

        Then.puzzleDashboard.failedPuzzlesCardIsNotEnabled()
    }

    @Test
    fun GIVEN_failed_puzzles_exist_WHEN_on_dashboard_THEN_failed_card_is_clickable() {
        Given.user.withFailedPuzzles(PUZZLE_ID)

        When.app.launch()
        When.navigation.navigateToPuzzles()

        Then.puzzleDashboard.failedPuzzlesCardIsEnabled()
    }

    @Test
    fun GIVEN_failed_puzzle_solved_WHEN_reopening_failed_puzzles_THEN_shows_empty() {
        Given.user.withFailedPuzzles(PUZZLE_ID)

        When.app.launch()
        When.navigation.navigateToPuzzles()
        When.failedPuzzles
            .open()
            .playToEnd()
        Then.failedPuzzles.completionDialogIsShown()

        When.failedPuzzles.dismissCompletion()
        When.navigation.goBack()
        Then.puzzleDashboard.failedPuzzlesCardIsNotEnabled()
    }

    private companion object {
        /**
         * A puzzle ID that exists in `test_puzzles.db`. Using a single puzzle ensures the
         * [com.paulcraciunas.chessgym.di.TestPuzzleInterceptor] captures the correct puzzle
         * for move orchestration in tests.
         */
        const val PUZZLE_ID: Int = 3
    }
}
