package com.paulcraciunas.chessgym.dsl.assertions

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import com.paulcraciunas.screens.boardvis.pieces.ui.KnightPathTags

class KnightPathScreenAssertions(private val rule: ComposeTestRule) {
    fun isDisplayed(): KnightPathScreenAssertions = apply {
        rule.onNodeWithTag(KnightPathTags.SCREEN).assertIsDisplayed()
    }
}
