package com.paulcraciunas.chessgym.dsl.assertions

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import com.paulcraciunas.screens.puzzles.streak.ui.PuzzleStreakScreenTags

class PuzzleStreakScreenAssertions(private val rule: ComposeTestRule) {
    fun isDisplayed(): PuzzleStreakScreenAssertions = apply {
        rule.onNodeWithTag(PuzzleStreakScreenTags.SCREEN).assertIsDisplayed()
    }
}
