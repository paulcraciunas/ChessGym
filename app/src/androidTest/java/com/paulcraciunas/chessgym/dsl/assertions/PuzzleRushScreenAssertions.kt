package com.paulcraciunas.chessgym.dsl.assertions

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import com.paulcraciunas.screens.puzzles.rush.ui.PuzzleRushScreenTags

class PuzzleRushScreenAssertions(private val rule: ComposeTestRule) {
    fun isDisplayed(): PuzzleRushScreenAssertions = apply {
        rule.onNodeWithTag(PuzzleRushScreenTags.SCREEN).assertIsDisplayed()
    }
}
