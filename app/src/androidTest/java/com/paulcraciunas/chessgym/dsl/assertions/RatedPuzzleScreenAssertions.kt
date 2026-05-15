package com.paulcraciunas.chessgym.dsl.assertions

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.platform.app.InstrumentationRegistry
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.dialogs.AbandonConfirmationDialogTags
import com.paulcraciunas.screens.common.dialogs.PromotionDialogTags
import com.paulcraciunas.screens.puzzles.rated.ui.RatedPuzzleScreenTags

class RatedPuzzleScreenAssertions(private val rule: ComposeTestRule) {

    fun isDisplayed(): RatedPuzzleScreenAssertions = apply {
        rule.onNodeWithTag(RatedPuzzleScreenTags.SCREEN).assertIsDisplayed()
    }

    fun isFinishedWithSuccess(): RatedPuzzleScreenAssertions = apply {
        waitForFinished()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        rule.onNodeWithTag(RatedPuzzleScreenTags.Finished.RESULT_TEXT)
            .assertTextEquals(context.getString(R.string.generic_success))
    }

    fun isFinishedWithFailure(): RatedPuzzleScreenAssertions = apply {
        waitForFinished()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        rule.onNodeWithTag(RatedPuzzleScreenTags.Finished.RESULT_TEXT)
            .assertTextEquals(context.getString(R.string.generic_failed))
    }

    fun isPlaying(): RatedPuzzleScreenAssertions = apply {
        rule.onNodeWithTag(RatedPuzzleScreenTags.Finished.CONTROLS).assertIsNotDisplayed()
    }

    /**
     * Waits for the finished controls to appear. This is necessary after abandonment because the
     * VM animates the remaining solution moves (600ms each) before transitioning to Finished.
     */
    private fun waitForFinished() {
        rule.waitUntil(timeoutMillis = FINISHED_TIMEOUT_MS) {
            rule.onAllNodes(hasTestTag(RatedPuzzleScreenTags.Finished.CONTROLS))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        rule.onNodeWithTag(RatedPuzzleScreenTags.Finished.CONTROLS).assertIsDisplayed()
    }

    fun abandonDialogIsShown(): RatedPuzzleScreenAssertions = apply {
        rule.onNodeWithTag(AbandonConfirmationDialogTags.DIALOG).assertIsDisplayed()
    }

    fun abandonDialogIsDismissed(): RatedPuzzleScreenAssertions = apply {
        rule.onAllNodes(hasTestTag(AbandonConfirmationDialogTags.DIALOG)).assertCountEquals(0)
    }

    fun promotionDialogIsShown(): RatedPuzzleScreenAssertions = apply {
        rule.onNodeWithTag(PromotionDialogTags.DIALOG).assertIsDisplayed()
    }

    fun promotionDialogIsDismissed(): RatedPuzzleScreenAssertions = apply {
        rule.onAllNodes(hasTestTag(PromotionDialogTags.DIALOG)).assertCountEquals(0)
    }

    fun hasBackButton(): RatedPuzzleScreenAssertions = apply {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val description = context.getString(R.string.nav_drawer_back)
        rule.onAllNodesWithContentDescription(description).assertCountEquals(1)
    }

    private companion object {
        /**
         * Upper bound on how long to wait for the finished controls to appear. The solution
         * playback uses a 600ms delay per move; even a long puzzle (say 10 remaining plies)
         * finishes well within this window.
         */
        const val FINISHED_TIMEOUT_MS: Long = 15_000L
    }
}
