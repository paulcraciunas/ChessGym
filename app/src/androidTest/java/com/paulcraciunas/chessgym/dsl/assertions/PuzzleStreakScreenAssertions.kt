package com.paulcraciunas.chessgym.dsl.assertions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.SemanticsKeys
import com.paulcraciunas.screens.common.dialogs.AbandonConfirmationDialogTags
import com.paulcraciunas.screens.puzzles.streak.ui.PuzzleStreakScreenTags

class PuzzleStreakScreenAssertions(private val rule: ComposeTestRule) {

    fun isDisplayed(): PuzzleStreakScreenAssertions = apply {
        rule.onNodeWithTag(PuzzleStreakScreenTags.SCREEN).assertIsDisplayed()
    }

    fun hasBackgroundColor(color: Color): PuzzleStreakScreenAssertions = apply {
        rule.onNodeWithTag(PuzzleStreakScreenTags.SCREEN).assert(
            SemanticsMatcher.expectValue(SemanticsKeys.BackgroundColor, color)
        )
    }

    fun isPlaying(): PuzzleStreakScreenAssertions = apply {
        rule.onAllNodes(hasTestTag(PuzzleStreakScreenTags.Ended.CONTROLS))
            .assertCountEquals(0)
        rule.onAllNodes(hasTestTag(PuzzleStreakScreenTags.Summary.DIALOG))
            .assertCountEquals(0)
    }

    fun hasBackButton(): PuzzleStreakScreenAssertions = apply {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val description = context.getString(R.string.nav_drawer_back)
        rule.onAllNodesWithContentDescription(description).assertCountEquals(1)
    }

    fun hasStreakCount(count: Int): PuzzleStreakScreenAssertions = apply {
        rule.onNodeWithTag(PuzzleStreakScreenTags.STREAK_COUNTER)
            .assertIsDisplayed()
            .assertTextEquals(count.toString())
    }

    fun hasNoStreakCounter(): PuzzleStreakScreenAssertions = apply {
        rule.onAllNodes(hasTestTag(PuzzleStreakScreenTags.STREAK_COUNTER))
            .assertCountEquals(0)
    }

    fun isStreakEnded(): PuzzleStreakScreenAssertions = apply {
        waitForStreakEnded()
    }

    fun summaryDialogIsShown(): PuzzleStreakScreenAssertions = apply {
        waitForSummary()
        rule.onNodeWithTag(PuzzleStreakScreenTags.Summary.DIALOG).assertIsDisplayed()
    }

    fun summaryDialogIsDismissed(): PuzzleStreakScreenAssertions = apply {
        rule.onAllNodes(hasTestTag(PuzzleStreakScreenTags.Summary.DIALOG))
            .assertCountEquals(0)
    }

    fun summaryShowsNewHighScore(): PuzzleStreakScreenAssertions = apply {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        rule.onNodeWithText(context.getString(R.string.generic_new_high_score))
            .assertIsDisplayed()
    }

    fun summaryDoesNotShowNewHighScore(): PuzzleStreakScreenAssertions = apply {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        rule.onAllNodesWithText(context.getString(R.string.generic_new_high_score))
            .assertCountEquals(0)
    }

    fun abandonDialogIsShown(): PuzzleStreakScreenAssertions = apply {
        rule.onNodeWithTag(AbandonConfirmationDialogTags.DIALOG).assertIsDisplayed()
    }

    fun abandonDialogIsDismissed(): PuzzleStreakScreenAssertions = apply {
        rule.onAllNodes(hasTestTag(AbandonConfirmationDialogTags.DIALOG))
            .assertCountEquals(0)
    }

    fun hasEndedControls(): PuzzleStreakScreenAssertions = apply {
        rule.onNodeWithTag(PuzzleStreakScreenTags.Ended.CONTROLS).assertIsDisplayed()
    }

    /**
     * Waits for the streak ended controls to appear. After a wrong move or abandon, the VM
     * may animate the solution before transitioning to the StreakEnded state.
     */
    private fun waitForStreakEnded() {
        rule.waitUntil(timeoutMillis = STREAK_ENDED_TIMEOUT_MS) {
            rule.onAllNodes(hasTestTag(PuzzleStreakScreenTags.Ended.CONTROLS))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        rule.onNodeWithTag(PuzzleStreakScreenTags.Ended.CONTROLS).assertIsDisplayed()
    }

    private fun waitForSummary() {
        rule.waitUntil(timeoutMillis = STREAK_ENDED_TIMEOUT_MS) {
            rule.onAllNodes(hasTestTag(PuzzleStreakScreenTags.Summary.DIALOG))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private companion object {
        const val STREAK_ENDED_TIMEOUT_MS: Long = 15_000L
    }
}
