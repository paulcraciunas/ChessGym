package com.paulcraciunas.chessgym.dsl.assertions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.platform.app.InstrumentationRegistry
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.SemanticsKeys
import com.paulcraciunas.screens.puzzles.failed.ui.FailedPuzzlesScreenTags

class FailedPuzzlesScreenAssertions(private val rule: ComposeTestRule) {

    fun isDisplayed(): FailedPuzzlesScreenAssertions = apply {
        rule.onNodeWithTag(FailedPuzzlesScreenTags.SCREEN).assertIsDisplayed()
    }

    fun hasBackgroundColor(color: Color): FailedPuzzlesScreenAssertions = apply {
        rule.onNodeWithTag(FailedPuzzlesScreenTags.SCREEN).assert(
            SemanticsMatcher.expectValue(SemanticsKeys.BackgroundColor, color)
        )
    }

    fun isPlaying(): FailedPuzzlesScreenAssertions = apply {
        rule.onAllNodes(hasTestTag(FailedPuzzlesScreenTags.Completion.DIALOG))
            .assertCountEquals(0)
    }

    fun hasBackButton(): FailedPuzzlesScreenAssertions = apply {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val description = context.getString(R.string.nav_drawer_back)
        rule.onAllNodesWithContentDescription(description).assertCountEquals(1)
    }

    fun completionDialogIsShown(): FailedPuzzlesScreenAssertions = apply {
        waitForCompletion()
        rule.onNodeWithTag(FailedPuzzlesScreenTags.Completion.DIALOG).assertIsDisplayed()
    }

    fun completionDialogIsDismissed(): FailedPuzzlesScreenAssertions = apply {
        rule.onAllNodes(hasTestTag(FailedPuzzlesScreenTags.Completion.DIALOG))
            .assertCountEquals(0)
    }

    /**
     * Waits for the completion dialog to appear. This is necessary because the VM processes
     * move results asynchronously and may animate board transitions before showing the dialog.
     */
    private fun waitForCompletion() {
        rule.waitUntil(timeoutMillis = COMPLETION_TIMEOUT_MS) {
            rule.onAllNodes(hasTestTag(FailedPuzzlesScreenTags.Completion.DIALOG))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private companion object {
        const val COMPLETION_TIMEOUT_MS: Long = 15_000L
    }
}
