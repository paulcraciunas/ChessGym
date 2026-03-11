package com.paulcraciunas.chessgym.dsl.actions

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.paulcraciunas.chessgym.navigation.BottomNavItem
import com.paulcraciunas.chessgym.navigation.BottomNavigationTags
import com.paulcraciunas.screens.home.ui.HomeScreenTags

class HomeScreenActions(private val rule: ComposeTestRule) {

    fun expandStats(): HomeScreenActions = apply {
        rule.onNodeWithTag(HomeScreenTags.STATS_CARD).performScrollTo().performClick()
        rule.waitForIdle()
    }

    fun collapseStats(): HomeScreenActions = expandStats()

    fun expandHighScores(): HomeScreenActions = apply {
        rule.onNodeWithTag(HomeScreenTags.HIGH_SCORES_CARD).performScrollTo().performClick()
        rule.waitForIdle()
    }

    fun collapseHighScores(): HomeScreenActions = expandHighScores()

    fun navigateToPuzzles() {
        rule.onNodeWithTag(BottomNavigationTags.tagFor(BottomNavItem.PuzzleDashboard)).performClick()
        rule.waitForIdle()
    }
}
