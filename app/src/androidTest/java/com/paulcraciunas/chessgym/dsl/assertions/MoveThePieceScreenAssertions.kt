package com.paulcraciunas.chessgym.dsl.assertions

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import com.paulcraciunas.screens.boardvis.pieces.ui.MoveThePieceTags

class MoveThePieceScreenAssertions(private val rule: ComposeTestRule) {
    fun isDisplayed(): MoveThePieceScreenAssertions = apply {
        rule.onNodeWithTag(MoveThePieceTags.SCREEN).assertIsDisplayed()
    }
}
