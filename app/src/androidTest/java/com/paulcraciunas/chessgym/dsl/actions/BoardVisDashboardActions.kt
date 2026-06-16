package com.paulcraciunas.chessgym.dsl.actions

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.paulcraciunas.screens.boardvis.dashboard.ui.BoardVisDashboardTags

class BoardVisDashboardActions(private val rule: ComposeTestRule) {

    fun openFindTheSquare(): BoardVisDashboardActions = clickCard(
        BoardVisDashboardTags.Cards.FIND_THE_SQUARE
    )

    fun openKnightPath(): BoardVisDashboardActions = clickCard(
        BoardVisDashboardTags.Cards.KNIGHT_PATH
    )

    private fun clickCard(tag: String): BoardVisDashboardActions = apply {
        rule.onNodeWithTag(tag).performScrollTo().performClick()
        rule.waitForIdle()
    }
}
