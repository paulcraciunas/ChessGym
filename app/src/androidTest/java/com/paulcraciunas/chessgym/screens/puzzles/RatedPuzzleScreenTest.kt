package com.paulcraciunas.chessgym.screens.puzzles

import com.paulcraciunas.chessgym.base.BaseUiTest
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Test

@HiltAndroidTest
internal class RatedPuzzleScreenTest : BaseUiTest() {

    @Before
    override fun setUp() {
        super.setUp()
        // The rated puzzle screen is only reachable once puzzles have been downloaded.
        Given.settings
            .puzzlesDownloaded()
            // Stabilizes move/solution playback and avoids flakiness from board transitions.
            .noAnimations()
        // Default user has rating 1200; pick a puzzle whose rating matches so we land on a
        // deterministic puzzle from `test_puzzles.db`.
        Given.user.isDefault()
        Given.puzzle.withRating(PUZZLE_RATING)
    }

    @Test
    fun GIVEN_puzzle_loaded_WHEN_opened_THEN_shows_rated_puzzle_screen() {
        When.app.launch()
        When.navigation.navigateToPuzzles()
        When.ratedPuzzle.open()

        Then.ratedPuzzle
            .isDisplayed()
            .isPlaying()
            .hasBackButton()
    }

    @Test
    fun GIVEN_light_mode_WHEN_opened_THEN_shows_light_background() {
        Given.settings.lightMode()

        When.app.launch()
        When.navigation.navigateToPuzzles()
        When.ratedPuzzle.open()

        Then.ratedPuzzle.isDisplayed()
        Then.theme.isLightMode()
    }

    @Test
    fun GIVEN_dark_mode_WHEN_opened_THEN_shows_dark_background() {
        Given.settings.darkMode()

        When.app.launch()
        When.navigation.navigateToPuzzles()
        When.ratedPuzzle.open()

        Then.ratedPuzzle.isDisplayed()
        Then.theme.isDarkMode()
    }

    @Test
    fun WHEN_playing_all_expected_moves_THEN_puzzle_is_solved() {
        When.app.launch()
        When.navigation.navigateToPuzzles()

        When.ratedPuzzle.open()
        Then.ratedPuzzle.isPlaying()

        When.ratedPuzzle.playToEnd()
        Then.ratedPuzzle.isFinishedWithSuccess()
    }

    @Test
    fun WHEN_playing_a_wrong_first_move_THEN_puzzle_fails() {
        When.app.launch()
        When.navigation.navigateToPuzzles()
        When.ratedPuzzle
            .open()
            .playWrongMove()

        Then.ratedPuzzle.isFinishedWithFailure()
    }

    @Test
    fun WHEN_requesting_hint_THEN_puzzle_stays_in_play_until_moves_exhausted() {
        When.app.launch()
        When.navigation.navigateToPuzzles()
        When.ratedPuzzle.open()
            .requestHint()
        Then.ratedPuzzle.isPlaying()

        // User can still complete the puzzle after asking for a hint.
        When.ratedPuzzle.playToEndWithSelection()
        Then.ratedPuzzle.isFinishedWithSuccess()
    }

    @Test
    fun WHEN_abandoning_and_confirming_THEN_puzzle_fails() {
        When.app.launch()
        When.navigation.navigateToPuzzles()

        When.ratedPuzzle
            .open()
            .requestAbandon()
        Then.ratedPuzzle.abandonDialogIsShown()

        When.ratedPuzzle.confirmAbandon()
        Then.ratedPuzzle
            .abandonDialogIsNotShown()
            .isFinishedWithFailure()
    }

    @Test
    fun WHEN_abandoning_and_dismissing_THEN_puzzle_continues() {
        When.app.launch()
        When.navigation.navigateToPuzzles()

        When.ratedPuzzle.open()
            .requestAbandon()
        Then.ratedPuzzle.abandonDialogIsShown()

        When.ratedPuzzle.dismissAbandon()
        Then.ratedPuzzle
            .abandonDialogIsNotShown()
            .isPlaying()
    }

    @Test
    fun GIVEN_playing_WHEN_pressing_back_THEN_does_not_show_abandon_dialog_and_goes_back() {
        When.app.launch()
        When.navigation.navigateToPuzzles()

        When.ratedPuzzle.open()
        When.navigation.goBack()

        Then.ratedPuzzle
            .abandonDialogIsNotShown()
        Then.puzzleDashboard.isDisplayed()
    }

    @Test
    fun GIVEN_playing_WHEN_making_a_move_and_pressing_back_THEN_shows_abandon_dialog() {
        When.app.launch()
        When.navigation.navigateToPuzzles()

        When.ratedPuzzle.open()
            .playCorrectMove()
        When.navigation.goBack()

        Then.ratedPuzzle
            .abandonDialogIsShown()
            .isDisplayed()
    }

    @Test
    fun GIVEN_finished_puzzle_WHEN_pressing_back_THEN_navigates_to_dashboard() {
        When.app.launch()
        When.navigation.navigateToPuzzles()

        When.ratedPuzzle.open()
            .playToEnd()
        Then.ratedPuzzle.isFinishedWithSuccess()

        When.navigation.goBack()
        Then.puzzleDashboard.isDisplayed()
    }

    @Test
    fun GIVEN_finished_puzzle_WHEN_play_next_THEN_loads_a_new_puzzle() {
        When.app.launch()
        When.navigation.navigateToPuzzles()

        When.ratedPuzzle.open()
            .playToEnd()
        Then.ratedPuzzle.isFinishedWithSuccess()

        When.ratedPuzzle.playNext()
        Then.ratedPuzzle
            .isDisplayed()
            .isPlaying()
    }

    private companion object {
        /**
         * Rating at which one puzzle exists in `test_puzzles.db` and has multiple moves.
         */
        const val PUZZLE_RATING: Int = 1250
    }
}
