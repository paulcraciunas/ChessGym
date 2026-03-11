package com.paulcraciunas.chessgym.dsl.actions

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.paulcraciunas.screens.home.ui.HomeScreenTags

class HomeScreenActions(private val rule: ComposeTestRule) {

    fun expandStats(): HomeScreenActions = apply {
        rule.onNodeWithTag(HomeScreenTags.STATS_CARD).performClick()
        rule.waitForIdle()
    }

    fun expandHighScores(): HomeScreenActions = apply {
        rule.onNodeWithTag(HomeScreenTags.HIGH_SCORES_CARD).performClick()
        rule.waitForIdle()
    }
}
