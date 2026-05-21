package com.paulcraciunas.chessgym.dsl.actions

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.paulcraciunas.chessgym.di.TestClockTimersModule
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.screens.boardvis.squares.ui.FindTheSquareTags
import com.paulcraciunas.screens.common.board.ChessBoardTags
import com.paulcraciunas.screens.common.controls.SideSelectionTags

class FindTheSquareScreenActions(private val rule: ComposeTestRule) {

    fun selectWhiteSide(): FindTheSquareScreenActions = apply {
        rule.onNodeWithTag(SideSelectionTags.WHITE).performClick()
        rule.waitForIdle()
    }

    fun selectBlackSide(): FindTheSquareScreenActions = apply {
        rule.onNodeWithTag(SideSelectionTags.BLACK).performClick()
        rule.waitForIdle()
    }

    fun selectRandomSide(): FindTheSquareScreenActions = apply {
        rule.onNodeWithTag(SideSelectionTags.RANDOM).performClick()
        rule.waitForIdle()
    }

    fun clickPlay(): FindTheSquareScreenActions = apply {
        rule.onNodeWithTag(FindTheSquareTags.PLAY_BUTTON).performClick()
        rule.waitForIdle()
    }

    fun clickPlayAgain(): FindTheSquareScreenActions = apply {
        rule.onNodeWithTag(FindTheSquareTags.PLAY_AGAIN_BUTTON).performClick()
        rule.waitForIdle()
    }

    fun clickSquare(locus: Locus): FindTheSquareScreenActions = apply {
        rule.onNodeWithTag(ChessBoardTags.square(locus)).performClick()
        rule.waitForIdle()
    }

    fun waitForGameToEnd(timeoutMillis: Long): FindTheSquareScreenActions = apply {
        TestClockTimersModule.defaultTimer.advanceUntilIdle()
        rule.waitUntil(timeoutMillis = timeoutMillis) {
            rule.onAllNodes(hasTestTag(FindTheSquareTags.GAME_SUMMARY)).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
