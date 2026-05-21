package com.paulcraciunas.chessgym.dsl.actions

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.paulcraciunas.chessgym.navigation.BottomNavItem
import com.paulcraciunas.chessgym.navigation.BottomNavigationTags
import com.paulcraciunas.screens.common.AppBarTags
import com.paulcraciunas.screens.common.AppDrawerTags

/**
 * Shared navigation actions available from any screen: bottom-bar tab switching,
 * app-bar back/menu buttons.
 */
class NavigationActions(private val rule: ComposeTestRule) {

    fun goBack(): NavigationActions = performAction(AppBarTags.BACK_BUTTON)

    fun openDrawer(): NavigationActions = performAction(AppBarTags.HOME_BUTTON)

    fun navigateToHome(): NavigationActions =
        performAction(BottomNavigationTags.tagFor(BottomNavItem.Home))

    fun navigateToPuzzles(): NavigationActions =
        performAction(BottomNavigationTags.tagFor(BottomNavItem.PuzzleDashboard))

    fun navigateToBoardVis(): NavigationActions =
        performAction(BottomNavigationTags.tagFor(BottomNavItem.BoardVisualization))

    fun navigateToBlindMode(): NavigationActions =
        performAction(BottomNavigationTags.tagFor(BottomNavItem.BlindMode))

    fun navigateToTools(): NavigationActions =
        performAction(BottomNavigationTags.tagFor(BottomNavItem.ToolsDashboard))

    fun navigateToSignIn(): NavigationActions = apply {
        openDrawer()
        rule.onNodeWithTag(AppDrawerTags.SIGN_IN).performClick()
        rule.waitForIdle()
    }

    private fun performAction(tag: String): NavigationActions = apply {
        rule.onNodeWithTag(tag).performClick()
        rule.waitForIdle()
    }
}
