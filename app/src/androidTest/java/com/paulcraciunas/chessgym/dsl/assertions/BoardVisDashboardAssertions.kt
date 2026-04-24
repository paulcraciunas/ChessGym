package com.paulcraciunas.chessgym.dsl.assertions

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import com.paulcraciunas.screens.boardvis.dashboard.ui.BoardVisDashboardTags

class BoardVisDashboardAssertions(private val rule: ComposeTestRule) {
    fun isDisplayed(): BoardVisDashboardAssertions = apply {
        rule.onNodeWithTag(BoardVisDashboardTags.SCREEN).assertIsDisplayed()
    }
}
