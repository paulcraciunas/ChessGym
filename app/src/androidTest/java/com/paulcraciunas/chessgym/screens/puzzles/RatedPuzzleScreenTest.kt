package com.paulcraciunas.chessgym.screens.puzzles

import com.paulcraciunas.chessgym.base.BaseUiTest
import com.paulcraciunas.chessgym.dsl.Given
import com.paulcraciunas.chessgym.dsl.Then
import com.paulcraciunas.chessgym.dsl.When
import com.paulcraciunas.screens.common.theme.DarkBackground
import com.paulcraciunas.screens.common.theme.LightBackground
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
        When.appIsLaunched()
        When.homeScreen.navigateToPuzzles()
        When.ratedPuzzle.open()

        Then.ratedPuzzle
            .isDisplayed()
            .isPlaying()
            .hasBackButton()
    }

    @Test
    fun GIVEN_light_mode_WHEN_opened_THEN_shows_light_background() {
        Given.settings.lightMode()

        When.appIsLaunched()
        When.homeScreen.navigateToPuzzles()
        When.ratedPuzzle.open()

        Then.ratedPuzzle
            .isDisplayed()
            .hasBackgroundColor(LightBackground)
    }

    @Test
    fun GIVEN_dark_mode_WHEN_opened_THEN_shows_dark_background() {
        Given.settings.darkMode()

        When.appIsLaunched()
        When.homeScreen.navigateToPuzzles()
        When.ratedPuzzle.open()

        Then.ratedPuzzle
            .isDisplayed()
            .hasBackgroundColor(DarkBackground)
    }

    @Test
    fun WHEN_playing_all_expected_moves_THEN_puzzle_is_solved() {
        When.appIsLaunched()
        When.homeScreen.navigateToPuzzles()

        When.ratedPuzzle.open()
        Then.ratedPuzzle.isPlaying()

        When.ratedPuzzle.playToEnd()
        Then.ratedPuzzle.isFinishedWithSuccess()
    }

    @Test
    fun WHEN_playing_a_wrong_first_move_THEN_puzzle_fails() {
        When.appIsLaunched()
        When.homeScreen.navigateToPuzzles()
        When.ratedPuzzle
            .open()
            .playWrongMove()

        Then.ratedPuzzle.isFinishedWithFailure()
    }

    @Test
    fun WHEN_requesting_hint_THEN_puzzle_stays_in_play_until_moves_exhausted() {
        When.appIsLaunched()
        When.homeScreen.navigateToPuzzles()
        When.ratedPuzzle.open()
            .requestHint()
        Then.ratedPuzzle.isPlaying()

        // User can still complete the puzzle after asking for a hint.
        When.ratedPuzzle.playToEndWithSelection()
        Then.ratedPuzzle.isFinishedWithSuccess()
    }

    @Test
    fun WHEN_abandoning_and_confirming_THEN_puzzle_fails() {
        When.appIsLaunched()
        When.homeScreen.navigateToPuzzles()

        When.ratedPuzzle
            .open()
            .requestAbandon()
        Then.ratedPuzzle.abandonDialogIsShown()

        When.ratedPuzzle.confirmAbandon()
        Then.ratedPuzzle
            .abandonDialogIsDismissed()
            .isFinishedWithFailure()
    }

    @Test
    fun WHEN_abandoning_and_dismissing_THEN_puzzle_continues() {
        When.appIsLaunched()
        When.homeScreen.navigateToPuzzles()

        When.ratedPuzzle.open()
            .requestAbandon()
        Then.ratedPuzzle.abandonDialogIsShown()

        When.ratedPuzzle.dismissAbandon()
        Then.ratedPuzzle
            .abandonDialogIsDismissed()
            .isPlaying()
    }

    @Test
    fun GIVEN_playing_WHEN_pressing_back_THEN_shows_abandon_dialog() {
        When.appIsLaunched()
        When.homeScreen.navigateToPuzzles()

        When.ratedPuzzle.open()
        When.navigation.goBack()

        Then.ratedPuzzle
            .abandonDialogIsShown()
            .isDisplayed()
    }

    @Test
    fun GIVEN_finished_puzzle_WHEN_pressing_back_THEN_navigates_to_dashboard() {
        When.appIsLaunched()
        When.homeScreen.navigateToPuzzles()

        When.ratedPuzzle.open()
            .playToEnd()
        Then.ratedPuzzle.isFinishedWithSuccess()

        When.navigation.goBack()
        Then.puzzleDashboard.isDisplayed()
    }

    @Test
    fun GIVEN_finished_puzzle_WHEN_play_next_THEN_loads_a_new_puzzle() {
        When.appIsLaunched()
        When.homeScreen.navigateToPuzzles()

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
         * Rating at which one puzzle exists in `test_puzzles.db` and which matches the default
         * user rating of 1200 (so `GetRatedPuzzle` can load it without expanding its search).
         */
        const val PUZZLE_RATING: Int = 1200
    }
}
