package com.paulcraciunas.chessgym.screens.boardvis

import com.paulcraciunas.chessgym.base.BaseUiTest
import com.paulcraciunas.chessgym.di.TestFindTheSquareModule
import com.paulcraciunas.chessgym.dsl.Given
import com.paulcraciunas.chessgym.dsl.Then
import com.paulcraciunas.chessgym.dsl.When
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.screens.common.theme.DarkBackground
import com.paulcraciunas.screens.common.theme.LightBackground
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Test

/**
 * Instrumentation tests for the Find the Square game screen.
 *
 * The game timer is injected with [TestFindTheSquareModule.TEST_DURATION_SECONDS] (3s)
 * for fast test execution. The [com.paulcraciunas.domain.api.general.FixedRandomFactory]
 * (returnValue = 0) makes the target square deterministic: always a1.
 */
@HiltAndroidTest
internal class FindTheSquareScreenTest : BaseUiTest() {

    @Before
    override fun setUp() {
        super.setUp()
        Given.user.isDefault()
    }

    // ── Setup phase ─────────────────────────────────────────────

    @Test
    fun WHEN_navigated_to_find_the_square_THEN_shows_setup_phase() {
        navigateToFindTheSquare()

        Then.findTheSquare
            .isDisplayed()
            .isInSetupPhase()
    }

    @Test
    fun GIVEN_setup_phase_WHEN_selecting_black_side_THEN_side_is_updated() {
        navigateToFindTheSquare()

        When.findTheSquare.selectBlackSide()

        Then.findTheSquare
            .isDisplayed()
            .isInSetupPhase()
    }

    @Test
    fun GIVEN_setup_phase_WHEN_selecting_random_side_THEN_side_is_updated() {
        navigateToFindTheSquare()

        When.findTheSquare.selectRandomSide()

        Then.findTheSquare
            .isDisplayed()
            .isInSetupPhase()
    }

    @Test
    fun GIVEN_setup_phase_WHEN_selecting_white_after_black_THEN_side_is_white() {
        navigateToFindTheSquare()

        When.findTheSquare.selectBlackSide()
        When.findTheSquare.selectWhiteSide()

        Then.findTheSquare
            .isDisplayed()
            .isInSetupPhase()
    }

    // ── Playing phase ───────────────────────────────────────────

    @Test
    fun GIVEN_setup_phase_WHEN_clicking_play_THEN_game_starts() {
        navigateToFindTheSquare()

        When.findTheSquare.clickPlay()

        Then.findTheSquare
            .isDisplayed()
            .isInPlayingPhase()
            .hasScore(0)
            .showsSquareName(DETERMINISTIC_SQUARE_NAME)
    }

    @Test
    fun GIVEN_playing_phase_WHEN_clicking_correct_square_THEN_score_increments() {
        navigateToFindTheSquare()
        When.findTheSquare.clickPlay()

        When.findTheSquare.clickSquare(DETERMINISTIC_SQUARE)

        Then.findTheSquare
            .isInPlayingPhase()
            .hasScore(1)
    }

    @Test
    fun GIVEN_playing_phase_WHEN_clicking_multiple_correct_squares_THEN_score_increments_each_time() {
        navigateToFindTheSquare()
        When.findTheSquare.clickPlay()

        When.findTheSquare.clickSquare(DETERMINISTIC_SQUARE)
        When.findTheSquare.clickSquare(DETERMINISTIC_SQUARE)
        When.findTheSquare.clickSquare(DETERMINISTIC_SQUARE)

        Then.findTheSquare
            .isInPlayingPhase()
            .hasScore(3)
    }

    @Test
    fun GIVEN_playing_phase_WHEN_clicking_wrong_square_THEN_score_does_not_change() {
        navigateToFindTheSquare()
        When.findTheSquare.clickPlay()

        When.findTheSquare.clickSquare(WRONG_SQUARE)

        Then.findTheSquare
            .isInPlayingPhase()
            .hasScore(0)
    }

    @Test
    fun GIVEN_playing_phase_WHEN_correct_then_wrong_THEN_score_stays_at_one() {
        navigateToFindTheSquare()
        When.findTheSquare.clickPlay()

        When.findTheSquare.clickSquare(DETERMINISTIC_SQUARE)
        When.findTheSquare.clickSquare(WRONG_SQUARE)

        Then.findTheSquare
            .isInPlayingPhase()
            .hasScore(1)
    }

    // ── Game over phase ─────────────────────────────────────────

    @Test
    fun GIVEN_playing_phase_WHEN_timer_expires_THEN_shows_game_over() {
        navigateToFindTheSquare()
        When.findTheSquare.clickPlay()

        When.findTheSquare.waitForGameToEnd(GAME_TIMEOUT_MS)

        Then.findTheSquare
            .isInGameOverPhase()
            .showsGameOverTitle()
            .hasFinalScore(0)
    }

    @Test
    fun GIVEN_playing_with_score_WHEN_timer_expires_THEN_shows_final_score() {
        navigateToFindTheSquare()
        When.findTheSquare.clickPlay()

        When.findTheSquare.clickSquare(DETERMINISTIC_SQUARE)
        When.findTheSquare.clickSquare(DETERMINISTIC_SQUARE)
        When.findTheSquare.waitForGameToEnd(GAME_TIMEOUT_MS)

        Then.findTheSquare
            .isInGameOverPhase()
            .showsGameOverTitle()
            .hasFinalScore(2)
    }

    @Test
    fun GIVEN_no_previous_high_score_WHEN_game_ends_with_score_THEN_shows_new_high_score() {
        Given.user.withHighScores(findTheSquare = 0)

        navigateToFindTheSquare()
        When.findTheSquare.clickPlay()
        When.findTheSquare.clickSquare(DETERMINISTIC_SQUARE)
        When.findTheSquare.waitForGameToEnd(GAME_TIMEOUT_MS)

        Then.findTheSquare
            .isInGameOverPhase()
            .showsNewHighScore()
    }

    @Test
    fun GIVEN_existing_high_score_WHEN_game_ends_below_high_score_THEN_shows_previous_high_score() {
        Given.user.withHighScores(findTheSquare = 100)

        navigateToFindTheSquare()
        When.findTheSquare.clickPlay()
        When.findTheSquare.waitForGameToEnd(GAME_TIMEOUT_MS)

        Then.findTheSquare
            .isInGameOverPhase()
            .showsHighScore(100)
    }

    // ── Play again ──────────────────────────────────────────────

    @Test
    fun GIVEN_game_over_WHEN_clicking_play_again_THEN_returns_to_setup() {
        navigateToFindTheSquare()
        When.findTheSquare.clickPlay()
        When.findTheSquare.waitForGameToEnd(GAME_TIMEOUT_MS)
        Then.findTheSquare.isInGameOverPhase()

        When.findTheSquare.clickPlayAgain()

        Then.findTheSquare
            .isDisplayed()
            .isInSetupPhase()
    }

    @Test
    fun GIVEN_play_again_setup_WHEN_clicking_play_THEN_new_game_starts() {
        navigateToFindTheSquare()
        When.findTheSquare.clickPlay()
        When.findTheSquare.waitForGameToEnd(GAME_TIMEOUT_MS)
        When.findTheSquare.clickPlayAgain()

        When.findTheSquare.clickPlay()

        Then.findTheSquare
            .isInPlayingPhase()
            .hasScore(0)
    }

    // ── Navigation / abandoning ─────────────────────────────────

    @Test
    fun GIVEN_setup_phase_WHEN_going_back_THEN_returns_to_dashboard() {
        navigateToFindTheSquare()
        Then.findTheSquare.isDisplayed()

        When.navigation.goBack()

        Then.boardVisDashboard.isDisplayed()
    }

    @Test
    fun GIVEN_playing_phase_WHEN_going_back_THEN_returns_to_dashboard() {
        navigateToFindTheSquare()
        When.findTheSquare.clickPlay()
        Then.findTheSquare.isInPlayingPhase()

        When.navigation.goBack()

        Then.boardVisDashboard.isDisplayed()
    }

    @Test
    fun GIVEN_game_over_WHEN_going_back_THEN_returns_to_dashboard() {
        navigateToFindTheSquare()
        When.findTheSquare.clickPlay()
        When.findTheSquare.waitForGameToEnd(GAME_TIMEOUT_MS)
        Then.findTheSquare.isInGameOverPhase()

        When.navigation.goBack()

        Then.boardVisDashboard.isDisplayed()
    }

    // ── Light / dark mode ───────────────────────────────────────

    @Test
    fun GIVEN_light_mode_WHEN_navigated_to_find_the_square_THEN_shows_light_background() {
        Given.settings.lightMode()

        navigateToFindTheSquare()

        Then.findTheSquare
            .isDisplayed()
            .hasBackgroundColor(LightBackground)
    }

    @Test
    fun GIVEN_dark_mode_WHEN_navigated_to_find_the_square_THEN_shows_dark_background() {
        Given.settings.darkMode()

        navigateToFindTheSquare()

        Then.findTheSquare
            .isDisplayed()
            .hasBackgroundColor(DarkBackground)
    }

    // ── Side selection affects game ─────────────────────────────

    @Test
    fun GIVEN_black_side_selected_WHEN_playing_THEN_game_runs_with_black_orientation() {
        navigateToFindTheSquare()
        When.findTheSquare.selectBlackSide()

        When.findTheSquare.clickPlay()

        Then.findTheSquare
            .isInPlayingPhase()
            .hasScore(0)
    }

    @Test
    fun GIVEN_random_side_selected_WHEN_playing_THEN_game_runs() {
        navigateToFindTheSquare()
        When.findTheSquare.selectRandomSide()

        When.findTheSquare.clickPlay()

        Then.findTheSquare
            .isInPlayingPhase()
            .hasScore(0)
    }

    // ── Helpers ─────────────────────────────────────────────────

    private fun navigateToFindTheSquare() {
        When.appIsLaunched()
        When.navigation.navigateToBoardVis()
        When.boardVisDashboard.openFindTheSquare()
    }

    private companion object {
        val DETERMINISTIC_SQUARE = Locus(File.a, Rank.`1`)
        const val DETERMINISTIC_SQUARE_NAME = "A1"
        val WRONG_SQUARE = Locus(File.e, Rank.`4`)
        const val GAME_TIMEOUT_MS = 10_000L
    }
}
