package com.paulcraciunas.chessgym.dsl.assertions

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import com.paulcraciunas.screens.puzzles.failed.ui.FailedPuzzlesScreenTags

class FailedPuzzlesScreenAssertions(private val rule: ComposeTestRule) {
    fun isDisplayed(): FailedPuzzlesScreenAssertions = apply {
        rule.onNodeWithTag(FailedPuzzlesScreenTags.SCREEN).assertIsDisplayed()
    }
}
