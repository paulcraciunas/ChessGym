package com.paulcraciunas.chessgym.dsl.actions

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.paulcraciunas.screens.common.AppBarTags

class NavigationActions(private val rule: ComposeTestRule) {
    /**
     * Clicks the back button in the App Bar and waits for the transition to complete.
     */
    fun goBack(): NavigationActions = performAction(AppBarTags.BACK_BUTTON)

    /**
     * Clicks the home/menu button in the App Bar.
     */
    fun goHome(): NavigationActions = performAction(AppBarTags.HOME_BUTTON)

    private fun performAction(tag: String): NavigationActions = apply {
        rule.onNodeWithTag(tag).performClick()
        rule.waitForIdle()
    }
}
