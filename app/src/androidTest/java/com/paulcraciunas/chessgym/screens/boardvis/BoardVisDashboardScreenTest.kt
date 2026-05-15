package com.paulcraciunas.chessgym.screens.boardvis

import com.paulcraciunas.chessgym.base.BaseUiTest
import com.paulcraciunas.chessgym.dsl.Given
import com.paulcraciunas.chessgym.dsl.Then
import com.paulcraciunas.chessgym.dsl.When
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
        When.appIsLaunched()
        When.navigation.navigateToBoardVis()

        Then.boardVisDashboard
            .isDisplayed()
            .hasAllCards()
    }

    @Test
    fun GIVEN_light_mode_WHEN_navigated_to_dashboard_THEN_shows_light_background() {
        Given.settings.lightMode()

        When.appIsLaunched()
        When.navigation.navigateToBoardVis()

        Then.boardVisDashboard.isDisplayed()
        Then.theme.isLightMode()
    }

    @Test
    fun GIVEN_dark_mode_WHEN_navigated_to_dashboard_THEN_shows_dark_background() {
        Given.settings.darkMode()

        When.appIsLaunched()
        When.navigation.navigateToBoardVis()

        Then.boardVisDashboard.isDisplayed()
        Then.theme.isDarkMode()
    }

    @Test
    fun WHEN_tapping_find_the_square_card_THEN_navigates_to_find_the_square() {
        When.appIsLaunched()
        When.navigation.navigateToBoardVis()
        When.boardVisDashboard.openFindTheSquare()

        Then.findTheSquare.isDisplayed()
    }

    @Test
    fun WHEN_tapping_move_the_piece_card_THEN_navigates_to_move_the_piece() {
        When.appIsLaunched()
        When.navigation.navigateToBoardVis()
        When.boardVisDashboard.openMoveThePiece()

        Then.moveThePiece.isDisplayed()
    }

    @Test
    fun WHEN_navigating_to_home_tab_THEN_shows_home_screen() {
        When.appIsLaunched()
        When.navigation.navigateToBoardVis()
        Then.boardVisDashboard.isDisplayed()

        When.navigation.navigateToHome()

        Then.homeScreen.isDisplayed()
    }

    @Test
    fun WHEN_navigating_to_puzzles_tab_THEN_shows_puzzle_dashboard() {
        Given.settings.puzzlesDownloaded()

        When.appIsLaunched()
        When.navigation.navigateToBoardVis()
        Then.boardVisDashboard.isDisplayed()

        When.navigation.navigateToPuzzles()

        Then.puzzleDashboard.isDisplayed()
    }

    @Test
    fun GIVEN_on_find_the_square_WHEN_going_back_THEN_returns_to_dashboard() {
        When.appIsLaunched()
        When.navigation.navigateToBoardVis()
        When.boardVisDashboard.openFindTheSquare()
        Then.findTheSquare.isDisplayed()

        When.navigation.goBack()

        Then.boardVisDashboard.isDisplayed()
    }

    @Test
    fun GIVEN_on_move_the_piece_WHEN_going_back_THEN_returns_to_dashboard() {
        When.appIsLaunched()
        When.navigation.navigateToBoardVis()
        When.boardVisDashboard.openMoveThePiece()
        Then.moveThePiece.isDisplayed()

        When.navigation.goBack()

        Then.boardVisDashboard.isDisplayed()
    }

    @Test
    fun WHEN_opening_drawer_THEN_drawer_is_displayed() {
        When.appIsLaunched()
        When.navigation.navigateToBoardVis()

        When.navigation.openDrawer()

        Then.navigation.drawerIsOpen()
    }
}
