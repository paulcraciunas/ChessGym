package com.paulcraciunas.chessgym.dsl.assertions

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import com.paulcraciunas.screens.puzzles.dashboard.ui.PuzzleDashboardTags

class PuzzleDashboardAssertions(private val rule: ComposeTestRule) {
    fun isDisplayed(): PuzzleDashboardAssertions = apply {
        rule.onNodeWithTag(PuzzleDashboardTags.SCREEN).assertIsDisplayed()
    }
}
