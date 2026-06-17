package com.paulcraciunas.chessgym.screens.boardvis

import com.paulcraciunas.chessgym.base.BaseUiTest
import com.paulcraciunas.chessgym.di.TestFindTheSquareModule
import com.paulcraciunas.domain.api.GenerateRandomLoci
import com.paulcraciunas.domain.di.RandomLociModule
import com.paulcraciunas.game.logic.api.board.Locus
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Test

/**
 * Instrumentation tests for the Find the Square game screen.
 *
 * The game timer is injected with [TestFindTheSquareModule.TEST_DURATION_SECONDS] (3s)
 * for fast test execution. The [com.paulcraciunas.domain.api.general.FixedRandomFactory]
 * (returnValue = 0) makes the target square deterministic: always a1.
 */
@UninstallModules(RandomLociModule::class)
@HiltAndroidTest
internal class FindTheSquareScreenTest : BaseUiTest() {

    @BindValue
    val fakeRandomLoci: GenerateRandomLoci = FakeRandomLoci()

    @Before
    override fun setUp() {
        super.setUp()
        Given.user.isDefault()
    }

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

    @Test
    fun GIVEN_setup_phase_WHEN_clicking_play_THEN_game_starts() {
        navigateToFindTheSquare()

        When.findTheSquare.clickPlay()

        Then.findTheSquare
            .isDisplayed()
            .isInPlayingPhase()
            .hasScore(0)
            .showsSquareName(fakeRandomLoci.currentSquareName)
    }

    @Test
    fun GIVEN_playing_phase_WHEN_clicking_correct_square_THEN_score_increments() {
        navigateToFindTheSquare()
        When.findTheSquare.clickPlay()

        When.findTheSquare.clickSquare(fakeRandomLoci.currentSquare)

        Then.findTheSquare
            .isInPlayingPhase()
            .hasScore(1)
    }

    @Test
    fun GIVEN_playing_phase_WHEN_clicking_multiple_correct_squares_THEN_score_increments_each_time() {
        navigateToFindTheSquare()
        When.findTheSquare.clickPlay()

        When.findTheSquare.clickSquare(fakeRandomLoci.currentSquare)
        When.findTheSquare.clickSquare(fakeRandomLoci.currentSquare)
        When.findTheSquare.clickSquare(fakeRandomLoci.currentSquare)

        Then.findTheSquare
            .isInPlayingPhase()
            .hasScore(3)
    }

    @Test
    fun GIVEN_playing_phase_WHEN_clicking_wrong_square_THEN_score_does_not_change() {
        navigateToFindTheSquare()
        When.findTheSquare.clickPlay()

        When.findTheSquare.clickSquare(fakeRandomLoci.wrongSquare)

        Then.findTheSquare
            .isInPlayingPhase()
            .hasScore(0)
    }

    @Test
    fun GIVEN_playing_phase_WHEN_correct_then_wrong_THEN_score_stays_at_one() {
        navigateToFindTheSquare()
        When.findTheSquare.clickPlay()

        When.findTheSquare.clickSquare(fakeRandomLoci.currentSquare)
        When.findTheSquare.clickSquare(fakeRandomLoci.wrongSquare)

        Then.findTheSquare
            .isInPlayingPhase()
            .hasScore(1)
    }

    @Test
    fun GIVEN_playing_phase_WHEN_timer_expires_THEN_shows_game_over() {
        navigateToFindTheSquare()
        When.findTheSquare.clickPlay()

        When.clock.advanceSeconds(GAME_TIMEOUT_SECONDS)

        Then.findTheSquare
            .isInGameOverPhase()
            .showsGameOverTitle()
            .hasFinalScore(0)
    }

    @Test
    fun GIVEN_playing_with_score_WHEN_timer_expires_THEN_shows_final_score() {
        navigateToFindTheSquare()
        When.findTheSquare.clickPlay()

        When.findTheSquare.clickSquare(fakeRandomLoci.currentSquare)
        When.findTheSquare.clickSquare(fakeRandomLoci.currentSquare)
        When.clock.advanceSeconds(GAME_TIMEOUT_SECONDS)

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
        When.findTheSquare.clickSquare(fakeRandomLoci.currentSquare)
        When.clock.advanceSeconds(GAME_TIMEOUT_SECONDS)

        Then.findTheSquare
            .isInGameOverPhase()
            .showsNewHighScore()
    }

    @Test
    fun GIVEN_existing_high_score_WHEN_game_ends_below_high_score_THEN_shows_previous_high_score() {
        Given.user.withHighScores(findTheSquare = 100)

        navigateToFindTheSquare()
        When.findTheSquare.clickPlay()
        When.clock.advanceSeconds(GAME_TIMEOUT_SECONDS)

        Then.findTheSquare
            .isInGameOverPhase()
            .showsHighScore(100)
    }

    @Test
    fun GIVEN_game_over_WHEN_clicking_play_again_THEN_returns_to_setup() {
        navigateToFindTheSquare()
        When.findTheSquare.clickPlay()
        When.clock.advanceSeconds(GAME_TIMEOUT_SECONDS)
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
        When.clock.advanceSeconds(GAME_TIMEOUT_SECONDS)
        When.findTheSquare.clickPlayAgain()

        When.findTheSquare.clickPlay()

        Then.findTheSquare
            .isInPlayingPhase()
            .hasScore(0)
    }

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
        When.clock.advanceSeconds(GAME_TIMEOUT_SECONDS)
        Then.findTheSquare.isInGameOverPhase()

        When.navigation.goBack()

        Then.boardVisDashboard.isDisplayed()
    }

    @Test
    fun GIVEN_light_mode_WHEN_navigated_to_find_the_square_THEN_shows_light_background() {
        Given.settings.lightMode()

        navigateToFindTheSquare()

        Then.findTheSquare.isDisplayed()
        Then.theme.isLightMode()
    }

    @Test
    fun GIVEN_dark_mode_WHEN_navigated_to_find_the_square_THEN_shows_dark_background() {
        Given.settings.darkMode()

        navigateToFindTheSquare()

        Then.findTheSquare.isDisplayed()
        Then.theme.isDarkMode()
    }

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

    private fun navigateToFindTheSquare() {
        When.app.launch()
        When.navigation.navigateToBoardVis()
        When.boardVisDashboard.openFindTheSquare()
    }

    private val GenerateRandomLoci.currentSquare: Locus
        get() = (this as FakeRandomLoci).lastGenerated
    private val GenerateRandomLoci.wrongSquare: Locus
        get() = (this as FakeRandomLoci).wrong
    private val GenerateRandomLoci.currentSquareName: String
        get() = (this as FakeRandomLoci).lastGenerated.toString().uppercase()

    private companion object {
        const val GAME_TIMEOUT_SECONDS = 31
    }

    private class FakeRandomLoci : GenerateRandomLoci {
        private var currentIndex = 0

        var lastGenerated: Locus = Locus.a1
            private set
        val wrong: Locus
            get() = Locus.entries[(currentIndex + 6) % 64]

        override fun invoke(): Locus = next().also { lastGenerated = it }

        private fun next(): Locus = Locus.entries[currentIndex++ % 64]
    }
}
