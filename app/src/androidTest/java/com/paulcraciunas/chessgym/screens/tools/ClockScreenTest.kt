package com.paulcraciunas.chessgym.screens.tools

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
internal class ClockScreenTest : BaseUiTest() {

    @Before
    override fun setUp() {
        super.setUp()
        Given.settings.puzzlesDownloaded()
        Given.user.isDefault()
    }

    @Test
    fun WHEN_navigated_to_clock_THEN_shows_setup_phase() {
        navigateToClock()

        Then.clockScreen
            .isDisplayed()
            .isInSetupPhase()
    }

    @Test
    fun GIVEN_setup_phase_WHEN_selecting_different_time_THEN_ui_updates() {
        navigateToClock()

        When.clockScreen.selectTime(10)

        Then.clockScreen
            .isDisplayed()
            .isInSetupPhase()
    }

    @Test
    fun GIVEN_setup_phase_WHEN_selecting_different_increment_THEN_ui_updates() {
        navigateToClock()

        When.clockScreen.selectIncrement(5)

        Then.clockScreen
            .isDisplayed()
            .isInSetupPhase()
    }

    @Test
    fun GIVEN_setup_phase_WHEN_tapping_white_THEN_game_starts() {
        navigateToClock()

        When.clockScreen.tapWhite()

        Then.clockScreen
            .isDisplayed()
            .isInPlayingPhase()
            .showsStopButton()
    }

    @Test
    fun GIVEN_setup_phase_WHEN_tapping_black_THEN_game_starts() {
        navigateToClock()

        When.clockScreen.tapBlack()

        Then.clockScreen
            .isDisplayed()
            .isInPlayingPhase()
            .showsStopButton()
    }

    @Test
    fun GIVEN_playing_phase_WHEN_tapping_active_side_THEN_switches_player() {
        navigateToClock()
        When.clockScreen.tapWhite()
        Then.clockScreen.isInPlayingPhase()

        When.clockScreen.tapWhite()

        Then.clockScreen
            .isInPlayingPhase()
            .whiteButtonIsDisabled()
            .blackButtonIsEnabled()
    }

    @Test
    fun GIVEN_playing_phase_WHEN_stop_THEN_returns_to_setup() {
        navigateToClock()
        When.clockScreen.tapWhite()
        Then.clockScreen.isInPlayingPhase()

        When.clockScreen.stop()

        Then.clockScreen.isInSetupPhase()
    }

    @Test
    fun GIVEN_playing_phase_WHEN_white_timer_expires_THEN_shows_finished() {
        navigateToClock()
        When.clockScreen.tapWhite()
        Then.clockScreen.isInPlayingPhase()

        Given.clock.expireWhiteTimer()
        When.compose.waitForIdle()

        Then.clockScreen
            .isInFinishedPhase()
            .showsNewGameButton()
    }

    @Test
    fun GIVEN_playing_phase_WHEN_black_timer_expires_THEN_shows_finished() {
        navigateToClock()
        When.clockScreen.tapWhite()
        When.clockScreen.tapWhite()
        Then.clockScreen.isInPlayingPhase()

        Given.clock.expireBlackTimer()
        When.compose.waitForIdle()

        Then.clockScreen
            .isInFinishedPhase()
            .showsNewGameButton()
    }

    @Test
    fun GIVEN_finished_phase_WHEN_new_game_THEN_returns_to_setup() {
        navigateToClock()
        When.clockScreen.tapWhite()
        Given.clock.expireWhiteTimer()
        When.compose.waitForIdle()
        Then.clockScreen.isInFinishedPhase()

        When.clockScreen.newGame()

        Then.clockScreen.isInSetupPhase()
    }

    @Test
    fun GIVEN_light_mode_WHEN_navigated_to_clock_THEN_shows_light_background() {
        Given.settings.lightMode()

        navigateToClock()

        Then.clockScreen
            .isDisplayed()
            .hasBackgroundColor(LightBackground)
    }

    @Test
    fun GIVEN_dark_mode_WHEN_navigated_to_clock_THEN_shows_dark_background() {
        Given.settings.darkMode()

        navigateToClock()

        Then.clockScreen
            .isDisplayed()
            .hasBackgroundColor(DarkBackground)
    }

    @Test
    fun WHEN_going_back_THEN_returns_to_tools_dashboard() {
        navigateToClock()
        Then.clockScreen.isDisplayed()

        When.navigation.goBack()

        Then.toolsDashboard.isDisplayed()
    }

    private fun navigateToClock() {
        When.appIsLaunched()
        When.navigation.navigateToTools()
        When.toolsDashboard.openClock()
    }
}
