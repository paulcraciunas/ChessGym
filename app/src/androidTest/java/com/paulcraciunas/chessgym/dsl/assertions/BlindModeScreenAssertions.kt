package com.paulcraciunas.chessgym.dsl.assertions

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import com.paulcraciunas.screens.blindmode.ui.BlindModeScreenTags

class BlindModeScreenAssertions(private val rule: ComposeTestRule) {
    fun isDisplayed(): BlindModeScreenAssertions = apply {
        rule.onNodeWithTag(BlindModeScreenTags.SCREEN).assertIsDisplayed()
    }
}
