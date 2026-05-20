package com.paulcraciunas.chessgym.screens.puzzles

import com.paulcraciunas.chessgym.base.BaseUiTest
import com.paulcraciunas.chessgym.di.TestPuzzleInterceptor
import com.paulcraciunas.chessgym.dsl.Given
import com.paulcraciunas.chessgym.dsl.Then
import com.paulcraciunas.chessgym.dsl.When
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Test

@HiltAndroidTest
internal class PuzzleStreakScreenTest : BaseUiTest() {

    @Before
    override fun setUp() {
        super.setUp()
        Given.settings
            .puzzlesDownloaded()
            .noAnimations()
        Given.user.isDefault()
    }

    @Test
    fun GIVEN_existing_streak_WHEN_opened_THEN_shows_streak_screen() {
        Given.user.withPuzzleStreak(currentCount = STREAK_COUNT, lastPuzzleId = PUZZLE_ID)

        When.appIsLaunched()
        When.navigation.navigateToPuzzles()
        When.puzzleStreak.open()

        Then.puzzleStreak
            .isDisplayed()
            .isPlaying()
            .hasBackButton()
    }

    @Test
    fun GIVEN_light_mode_WHEN_opened_THEN_shows_light_background() {
        Given.settings.lightMode()
        Given.user.withPuzzleStreak(currentCount = STREAK_COUNT, lastPuzzleId = PUZZLE_ID)

        When.appIsLaunched()
        When.navigation.navigateToPuzzles()
        When.puzzleStreak.open()

        Then.puzzleStreak.isDisplayed()
        Then.theme.isLightMode()
    }

    @Test
    fun GIVEN_dark_mode_WHEN_opened_THEN_shows_dark_background() {
        Given.settings.darkMode()
        Given.user.withPuzzleStreak(currentCount = STREAK_COUNT, lastPuzzleId = PUZZLE_ID)

        When.appIsLaunched()
        When.navigation.navigateToPuzzles()
        When.puzzleStreak.open()

        Then.puzzleStreak.isDisplayed()
        Then.theme.isDarkMode()
    }

    @Test
    fun GIVEN_existing_streak_WHEN_opened_THEN_shows_correct_streak_count() {
        Given.user.withPuzzleStreak(currentCount = STREAK_COUNT, lastPuzzleId = PUZZLE_ID)

        When.appIsLaunched()
        When.navigation.navigateToPuzzles()
        When.puzzleStreak.open()

        Then.puzzleStreak.hasStreakCount(STREAK_COUNT)
    }

    @Test
    fun GIVEN_new_streak_WHEN_opened_THEN_shows_no_counter() {
        Given.user.withPuzzleStreak(currentCount = 0, lastPuzzleId = PUZZLE_ID)

        When.appIsLaunched()
        When.navigation.navigateToPuzzles()
        When.puzzleStreak.open()

        Then.puzzleStreak
            .isDisplayed()
            .hasNoStreakCounter()
    }

    @Test
    fun WHEN_solving_puzzle_THEN_streak_continues_with_next_puzzle() {
        Given.settings.autoNext()
        Given.user.withPuzzleStreak(currentCount = STREAK_FOR_NEXT, lastPuzzleId = PUZZLE_ID)

        When.appIsLaunched()
        When.navigation.navigateToPuzzles()
        When.puzzleStreak.open()
        Then.puzzleStreak.hasStreakCount(STREAK_FOR_NEXT)

        val loadCountBefore = TestPuzzleInterceptor.loadCount
        When.puzzleStreak.playToEnd()
        composeRule.waitUntil(timeoutMillis = NEXT_PUZZLE_TIMEOUT_MS) {
            TestPuzzleInterceptor.loadCount > loadCountBefore
        }

        Then.puzzleStreak
            .isDisplayed()
            .isPlaying()
            .hasStreakCount(STREAK_FOR_NEXT + 1)
    }

    @Test
    fun WHEN_playing_wrong_move_THEN_streak_ends_with_summary() {
        Given.user.withPuzzleStreak(currentCount = STREAK_COUNT, lastPuzzleId = PUZZLE_ID)

        When.appIsLaunched()
        When.navigation.navigateToPuzzles()
        When.puzzleStreak
            .open()
            .playWrongMove()

        Then.puzzleStreak
            .isStreakEnded()
            .summaryDialogIsShown()
    }

    @Test
    fun WHEN_requesting_hint_THEN_stays_in_play() {
        Given.user.withPuzzleStreak(currentCount = STREAK_COUNT, lastPuzzleId = PUZZLE_ID)

        When.appIsLaunched()
        When.navigation.navigateToPuzzles()
        When.puzzleStreak
            .open()
            .requestHint()

        Then.puzzleStreak.isPlaying()
    }

    @Test
    fun WHEN_abandoning_and_confirming_THEN_streak_ends() {
        Given.user.withPuzzleStreak(currentCount = STREAK_COUNT, lastPuzzleId = PUZZLE_ID)

        When.appIsLaunched()
        When.navigation.navigateToPuzzles()
        When.puzzleStreak
            .open()
            .requestAbandon()
        Then.puzzleStreak.abandonDialogIsShown()

        When.puzzleStreak.confirmAbandon()
        Then.puzzleStreak
            .abandonDialogIsDismissed()
            .isStreakEnded()
            .summaryDialogIsShown()
    }

    @Test
    fun WHEN_abandoning_and_dismissing_THEN_continues_playing() {
        Given.user.withPuzzleStreak(currentCount = STREAK_COUNT, lastPuzzleId = PUZZLE_ID)

        When.appIsLaunched()
        When.navigation.navigateToPuzzles()
        When.puzzleStreak
            .open()
            .requestAbandon()
        Then.puzzleStreak.abandonDialogIsShown()

        When.puzzleStreak.dismissAbandon()
        Then.puzzleStreak
            .abandonDialogIsDismissed()
            .isPlaying()
    }

    @Test
    fun GIVEN_summary_shown_WHEN_dismissing_THEN_dialog_dismissed_and_ended_controls_shown() {
        Given.user.withPuzzleStreak(currentCount = STREAK_COUNT, lastPuzzleId = PUZZLE_ID)

        When.appIsLaunched()
        When.navigation.navigateToPuzzles()
        When.puzzleStreak
            .open()
            .playWrongMove()
        Then.puzzleStreak.summaryDialogIsShown()

        When.puzzleStreak.dismissSummary()
        Then.puzzleStreak
            .summaryDialogIsDismissed()
            .hasEndedControls()
    }

    @Test
    fun GIVEN_new_high_score_WHEN_streak_ends_THEN_summary_shows_new_high_score() {
        Given.user
            .withHighScores(puzzleStreak = 0)
            .withPuzzleStreak(currentCount = STREAK_COUNT, lastPuzzleId = PUZZLE_ID)

        When.appIsLaunched()
        When.navigation.navigateToPuzzles()
        When.puzzleStreak
            .open()
            .playWrongMove()

        Then.puzzleStreak
            .summaryDialogIsShown()
            .summaryShowsNewHighScore()
    }

    @Test
    fun GIVEN_no_new_high_score_WHEN_streak_ends_THEN_summary_does_not_show_new_high_score() {
        Given.user
            .withHighScores(puzzleStreak = 100)
            .withPuzzleStreak(currentCount = STREAK_COUNT, lastPuzzleId = PUZZLE_ID)

        When.appIsLaunched()
        When.navigation.navigateToPuzzles()
        When.puzzleStreak
            .open()
            .playWrongMove()

        Then.puzzleStreak
            .summaryDialogIsShown()
            .summaryDoesNotShowNewHighScore()
    }

    @Test
    fun GIVEN_streak_ended_WHEN_starting_new_streak_THEN_new_game_starts() {
        Given.user.withPuzzleStreak(currentCount = STREAK_COUNT, lastPuzzleId = PUZZLE_ID)

        When.appIsLaunched()
        When.navigation.navigateToPuzzles()
        When.puzzleStreak
            .open()
            .playWrongMove()
        Then.puzzleStreak.summaryDialogIsShown()

        When.puzzleStreak.dismissSummary()
        // OnStreakComplete resets currentCount=0 and lastPuzzleId=null. The base target
        // rating (400) falls outside the test DB range, so we seed a loadable puzzle.
        Given.user.withPuzzleStreak(currentCount = 0, lastPuzzleId = PUZZLE_ID)
        When.puzzleStreak.startNewStreak()

        Then.puzzleStreak
            .isDisplayed()
            .isPlaying()
            .hasNoStreakCounter()
    }

    @Test
    fun GIVEN_playing_WHEN_pressing_back_THEN_returns_to_dashboard() {
        Given.user.withPuzzleStreak(currentCount = STREAK_COUNT, lastPuzzleId = PUZZLE_ID)

        When.appIsLaunched()
        When.navigation.navigateToPuzzles()
        When.puzzleStreak.open()
        Then.puzzleStreak.isPlaying()

        When.navigation.goBack()

        Then.puzzleDashboard.isDisplayed()
    }

    @Test
    fun GIVEN_streak_ended_WHEN_pressing_back_THEN_returns_to_dashboard() {
        Given.user.withPuzzleStreak(currentCount = STREAK_COUNT, lastPuzzleId = PUZZLE_ID)

        When.appIsLaunched()
        When.navigation.navigateToPuzzles()
        When.puzzleStreak
            .open()
            .playWrongMove()
        Then.puzzleStreak.isStreakEnded()

        When.puzzleStreak.dismissSummary()
        When.navigation.goBack()

        Then.puzzleDashboard.isDisplayed()
    }

    private companion object {
        /**
         * A puzzle ID that exists in `test_puzzles.db`. Puzzle Streak loads one puzzle at a time,
         * so the [com.paulcraciunas.chessgym.di.TestPuzzleInterceptor] always has the correct
         * puzzle for move orchestration.
         */
        const val PUZZLE_ID: Int = 3

        /**
         * Streak count used for most tests. Non-zero so the counter is visible.
         * Must be high enough that the user is considered to have an active streak.
         */
        const val STREAK_COUNT: Int = 5

        /**
         * Streak count chosen so that after solving the current puzzle, the next target rating
         * falls within `test_puzzles.db`'s range (1000–1500). With count 15, the next puzzle
         * targets rating 400 + 16 * 50 = 1200, which has an exact match at ID 177.
         */
        const val STREAK_FOR_NEXT: Int = 15

        const val NEXT_PUZZLE_TIMEOUT_MS: Long = 10_000L
    }
}
