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
internal class ToolsDashboardScreenTest : BaseUiTest() {

    @Before
    override fun setUp() {
        super.setUp()
        Given.settings.puzzlesDownloaded()
        Given.user.isDefault()
    }

    @Test
    fun WHEN_navigated_to_tools_dashboard_THEN_shows_all_cards() {
        When.appIsLaunched()
        When.navigation.navigateToTools()

        Then.toolsDashboard
            .isDisplayed()
            .hasAllCards()
    }

    @Test
    fun GIVEN_light_mode_WHEN_navigated_to_tools_dashboard_THEN_shows_light_background() {
        Given.settings.lightMode()

        When.appIsLaunched()
        When.navigation.navigateToTools()

        Then.toolsDashboard
            .isDisplayed()
            .hasBackgroundColor(LightBackground)
    }

    @Test
    fun GIVEN_dark_mode_WHEN_navigated_to_tools_dashboard_THEN_shows_dark_background() {
        Given.settings.darkMode()

        When.appIsLaunched()
        When.navigation.navigateToTools()

        Then.toolsDashboard
            .isDisplayed()
            .hasBackgroundColor(DarkBackground)
    }

    @Test
    fun WHEN_tapping_clock_card_THEN_navigates_to_clock() {
        When.appIsLaunched()
        When.navigation.navigateToTools()
        When.toolsDashboard.openClock()

        Then.clockScreen.isDisplayed()
    }

    @Test
    fun WHEN_tapping_analysis_card_THEN_navigates_to_analysis() {
        When.appIsLaunched()
        When.navigation.navigateToTools()
        When.toolsDashboard.openAnalysis()

        Then.analysisScreen.isDisplayed()
    }

    @Test
    fun WHEN_tapping_import_game_card_THEN_navigates_to_import_game() {
        When.appIsLaunched()
        When.navigation.navigateToTools()
        When.toolsDashboard.openImportGame()

        Then.importGame.isDisplayed()
    }

    @Test
    fun WHEN_navigating_to_home_tab_THEN_shows_home_screen() {
        When.appIsLaunched()
        When.navigation.navigateToTools()
        Then.toolsDashboard.isDisplayed()

        When.navigation.navigateToHome()

        Then.homeScreen.isDisplayed()
    }

    @Test
    fun WHEN_navigating_to_puzzles_tab_THEN_shows_puzzles_dashboard() {
        When.appIsLaunched()
        When.navigation.navigateToTools()
        Then.toolsDashboard.isDisplayed()

        When.navigation.navigateToPuzzles()

        Then.puzzleDashboard.isDisplayed()
    }

    @Test
    fun WHEN_opening_drawer_THEN_drawer_is_displayed() {
        When.appIsLaunched()
        When.navigation.navigateToTools()

        When.navigation.openDrawer()

        Then.navigation.drawerIsOpen()
    }

    @Test
    fun GIVEN_on_clock_screen_WHEN_going_back_THEN_returns_to_dashboard() {
        When.appIsLaunched()
        When.navigation.navigateToTools()
        When.toolsDashboard.openClock()
        Then.clockScreen.isDisplayed()

        When.navigation.goBack()

        Then.toolsDashboard.isDisplayed()
    }

    @Test
    fun GIVEN_on_import_game_screen_WHEN_going_back_THEN_returns_to_dashboard() {
        When.appIsLaunched()
        When.navigation.navigateToTools()
        When.toolsDashboard.openImportGame()
        Then.importGame.isDisplayed()

        When.navigation.goBack()

        Then.toolsDashboard.isDisplayed()
    }

    @Test
    fun GIVEN_on_analysis_screen_WHEN_going_back_THEN_returns_to_dashboard() {
        When.appIsLaunched()
        When.navigation.navigateToTools()
        When.toolsDashboard.openAnalysis()
        Then.analysisScreen.isDisplayed()

        When.navigation.goBack()

        Then.toolsDashboard.isDisplayed()
    }
}
