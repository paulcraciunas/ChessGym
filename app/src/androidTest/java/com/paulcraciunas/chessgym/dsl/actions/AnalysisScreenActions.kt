package com.paulcraciunas.chessgym.dsl.actions

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.screens.common.board.ChessBoardTags
import com.paulcraciunas.screens.common.controls.MoveNavigationTags

class AnalysisScreenActions(private val rule: ComposeTestRule) {

    fun clickSquare(square: String): AnalysisScreenActions = apply {
        rule.onNodeWithTag(ChessBoardTags.square(Locus.from(square)!!)).performClick()
        rule.waitForIdle()
    }

    fun nextMove(): AnalysisScreenActions = apply {
        rule.onNodeWithTag(MoveNavigationTags.NEXT).performClick()
        rule.waitForIdle()
    }

    fun previousMove(): AnalysisScreenActions = apply {
        rule.onNodeWithTag(MoveNavigationTags.PREVIOUS).performClick()
        rule.waitForIdle()
    }

    fun jumpToStart(): AnalysisScreenActions = apply {
        rule.onNodeWithTag(MoveNavigationTags.JUMP_TO_START).performClick()
        rule.waitForIdle()
    }

    fun jumpToEnd(): AnalysisScreenActions = apply {
        rule.onNodeWithTag(MoveNavigationTags.JUMP_TO_END).performClick()
        rule.waitForIdle()
    }
}
