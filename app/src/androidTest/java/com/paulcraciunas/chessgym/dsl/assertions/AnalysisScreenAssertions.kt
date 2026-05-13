package com.paulcraciunas.chessgym.dsl.assertions

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import com.paulcraciunas.screens.common.board.ChessBoardTags
import com.paulcraciunas.screens.tools.analysis.ui.AnalysisScreenTags

class AnalysisScreenAssertions(private val rule: ComposeTestRule) {

    fun isDisplayed(): AnalysisScreenAssertions = apply {
        rule.onNodeWithTag(AnalysisScreenTags.SCREEN).assertIsDisplayed()
    }

    fun showsEvaluationBar(): AnalysisScreenAssertions = apply {
        rule.onNodeWithTag(AnalysisScreenTags.EVALUATION_BAR).assertIsDisplayed()
    }

    fun showsEngineLines(): AnalysisScreenAssertions = apply {
        rule.onNodeWithTag(AnalysisScreenTags.ENGINE_LINES).assertIsDisplayed()
    }

    fun showsBorders(): AnalysisScreenAssertions = apply {
        rule.onAllNodesWithTag(ChessBoardTags.BORDER).assertCountEquals(2)
    }

    fun doesNotShowBorders(): AnalysisScreenAssertions = apply {
        rule.onNodeWithTag(ChessBoardTags.BORDER).assertDoesNotExist()
    }
}
