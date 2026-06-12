package com.paulcraciunas.chessgym.screens.boardvis

import com.paulcraciunas.chessgym.base.BaseUiTest
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Test

@HiltAndroidTest
internal class BoardVisDashboardScreenTest : BaseUiTest() {

    @Before
    override fun setUp() {
        super.setUp()
        Given.user.isDefault()
    }

    @Test
    fun WHEN_navigated_to_board_vis_dashboard_THEN_shows_all_cards() {
        When.app.launch()
        When.navigation.navigateToBoardVis()

        Then.boardVisDashboard
            .isDisplayed()
            .hasAllCards()
    }

    @Test
    fun GIVEN_light_mode_WHEN_navigated_to_dashboard_THEN_shows_light_background() {
        Given.settings.lightMode()

        When.app.launch()
        When.navigation.navigateToBoardVis()

        Then.boardVisDashboard.isDisplayed()
        Then.theme.isLightMode()
    }

    @Test
    fun GIVEN_dark_mode_WHEN_navigated_to_dashboard_THEN_shows_dark_background() {
        Given.settings.darkMode()

        When.app.launch()
        When.navigation.navigateToBoardVis()

        Then.boardVisDashboard.isDisplayed()
        Then.theme.isDarkMode()
    }

    @Test
    fun WHEN_tapping_find_the_square_card_THEN_navigates_to_find_the_square() {
        When.app.launch()
        When.navigation.navigateToBoardVis()
        When.boardVisDashboard.openFindTheSquare()

        Then.findTheSquare.isDisplayed()
    }

    @Test
    fun WHEN_tapping_knight_path_card_THEN_navigates_to_knight_path() {
        When.app.launch()
        When.navigation.navigateToBoardVis()
        When.boardVisDashboard.openKnightPath()

        Then.knightPath.isDisplayed()
    }

    @Test
    fun WHEN_navigating_to_home_tab_THEN_shows_home_screen() {
        When.app.launch()
        When.navigation.navigateToBoardVis()
        Then.boardVisDashboard.isDisplayed()

        When.navigation.navigateToHome()

        Then.homeScreen.isDisplayed()
    }

    @Test
    fun WHEN_navigating_to_puzzles_tab_THEN_shows_puzzle_dashboard() {
        Given.settings.puzzlesDownloaded()

        When.app.launch()
        When.navigation.navigateToBoardVis()
        Then.boardVisDashboard.isDisplayed()

        When.navigation.navigateToPuzzles()

        Then.puzzleDashboard.isDisplayed()
    }

    @Test
    fun GIVEN_on_find_the_square_WHEN_going_back_THEN_returns_to_dashboard() {
        When.app.launch()
        When.navigation.navigateToBoardVis()
        When.boardVisDashboard.openFindTheSquare()
        Then.findTheSquare.isDisplayed()

        When.navigation.goBack()

        Then.boardVisDashboard.isDisplayed()
    }

    @Test
    fun GIVEN_on_knight_path_WHEN_going_back_THEN_returns_to_dashboard() {
        When.app.launch()
        When.navigation.navigateToBoardVis()
        When.boardVisDashboard.openKnightPath()
        Then.knightPath.isDisplayed()

        When.navigation.goBack()

        Then.boardVisDashboard.isDisplayed()
    }

    @Test
    fun WHEN_opening_drawer_THEN_drawer_is_displayed() {
        When.app.launch()
        When.navigation.navigateToBoardVis()

        When.navigation.openDrawer()

        Then.navigation.drawerIsOpen()
    }
}
