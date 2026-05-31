package com.paulcraciunas.chessgym.dsl.assertions

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.paulcraciunas.screens.boardvis.squares.ui.FindTheSquareTags
import com.paulcraciunas.screens.common.controls.SideSelectionTags

class FindTheSquareScreenAssertions(private val rule: ComposeTestRule) {

    fun isDisplayed(): FindTheSquareScreenAssertions = apply {
        rule.onNodeWithTag(FindTheSquareTags.SCREEN).assertIsDisplayed()
    }

    fun isInSetupPhase(): FindTheSquareScreenAssertions = apply {
        rule.onNodeWithTag(FindTheSquareTags.PLAY_BUTTON).assertIsDisplayed()
        rule.onNodeWithTag(SideSelectionTags.WHITE).assertIsDisplayed()
        rule.onNodeWithTag(SideSelectionTags.BLACK).assertIsDisplayed()
        rule.onNodeWithTag(SideSelectionTags.RANDOM).assertIsDisplayed()
    }

    fun isInPlayingPhase(): FindTheSquareScreenAssertions = apply {
        rule.onNodeWithTag(FindTheSquareTags.PLAY_BUTTON).assertDoesNotExist()
        rule.onNodeWithTag(SideSelectionTags.WHITE).assertDoesNotExist()
    }

    fun isInGameOverPhase(): FindTheSquareScreenAssertions = apply {
        rule.onNodeWithTag(FindTheSquareTags.GAME_SUMMARY).assertIsDisplayed()
        rule.onNodeWithTag(FindTheSquareTags.PLAY_AGAIN_BUTTON).assertIsDisplayed()
    }

    fun hasScore(score: Int): FindTheSquareScreenAssertions = apply {
        rule.onNodeWithText("CURRENT SCORE").assertIsDisplayed()
    }

    fun hasFinalScore(score: Int): FindTheSquareScreenAssertions = apply {
        rule.onNodeWithText("FINAL SCORE: $score").assertIsDisplayed()
    }

    fun showsGameOverTitle(): FindTheSquareScreenAssertions = apply {
        rule.onNodeWithText("Time's Up!").assertIsDisplayed()
    }

    fun showsNewHighScore(): FindTheSquareScreenAssertions = apply {
        rule.onNodeWithText("New high score").assertIsDisplayed()
    }

    fun showsHighScore(highScore: Int): FindTheSquareScreenAssertions = apply {
        rule.onNodeWithText("HIGH SCORE: $highScore").assertIsDisplayed()
    }

    fun showsSquareName(squareName: String): FindTheSquareScreenAssertions = apply {
        rule.onNodeWithText(squareName.uppercase()).assertIsDisplayed()
    }
}
