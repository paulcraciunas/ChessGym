package com.paulcraciunas.chessgym.dsl.assertions

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import com.paulcraciunas.screens.boardvis.dashboard.ui.BoardVisDashboardTags

class BoardVisDashboardAssertions(private val rule: ComposeTestRule) {
    fun isDisplayed(): BoardVisDashboardAssertions = apply {
        rule.onNodeWithTag(BoardVisDashboardTags.SCREEN).assertIsDisplayed()
    }

    fun hasFindTheSquareCard(): BoardVisDashboardAssertions = apply {
        rule.onNodeWithTag(BoardVisDashboardTags.Cards.FIND_THE_SQUARE)
            .performScrollTo()
            .assertIsDisplayed()
    }

    fun hasMoveThePieceCard(): BoardVisDashboardAssertions = apply {
        rule.onNodeWithTag(BoardVisDashboardTags.Cards.MOVE_THE_PIECE)
            .performScrollTo()
            .assertIsDisplayed()
    }

    fun hasAllCards(): BoardVisDashboardAssertions = apply {
        hasFindTheSquareCard()
        hasMoveThePieceCard()
    }
}
