package com.paulcraciunas.chessgym.dsl.assertions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import com.paulcraciunas.screens.common.SemanticsKeys
import com.paulcraciunas.screens.puzzles.dashboard.ui.PuzzleDashboardTags

class PuzzleDashboardAssertions(private val rule: ComposeTestRule) {

    fun isDisplayed(): PuzzleDashboardAssertions = apply {
        rule.onNodeWithTag(PuzzleDashboardTags.SCREEN).assertIsDisplayed()
    }

    fun hasBackgroundColor(color: Color): PuzzleDashboardAssertions = apply {
        rule.onNodeWithTag(PuzzleDashboardTags.SCREEN).assert(
            SemanticsMatcher.expectValue(SemanticsKeys.BackgroundColor, color)
        )
    }

    fun hasRatedPuzzleCard(): PuzzleDashboardAssertions = apply {
        rule.onNodeWithTag(PuzzleDashboardTags.Cards.RATED_PUZZLE)
            .performScrollTo()
            .assertIsDisplayed()
    }

    fun hasPuzzleRushCard(): PuzzleDashboardAssertions = apply {
        rule.onNodeWithTag(PuzzleDashboardTags.Cards.PUZZLE_RUSH)
            .performScrollTo()
            .assertIsDisplayed()
    }

    fun hasPuzzleStreakCard(): PuzzleDashboardAssertions = apply {
        rule.onNodeWithTag(PuzzleDashboardTags.Cards.PUZZLE_STREAK)
            .performScrollTo()
            .assertIsDisplayed()
    }

    fun hasFailedPuzzlesCard(): PuzzleDashboardAssertions = apply {
        rule.onNodeWithTag(PuzzleDashboardTags.Cards.FAILED_PUZZLES)
            .performScrollTo()
            .assertIsDisplayed()
    }

    fun failedPuzzlesCardIsClickable(): PuzzleDashboardAssertions = apply {
        rule.onNode(
            hasTestTag(PuzzleDashboardTags.Cards.FAILED_PUZZLES) and hasClickAction()
        ).performScrollTo().assertIsDisplayed()
    }

    fun failedPuzzlesCardIsNotClickable(): PuzzleDashboardAssertions = apply {
        rule.onNode(
            hasTestTag(PuzzleDashboardTags.Cards.FAILED_PUZZLES) and !hasClickAction()
        ).performScrollTo().assertIsDisplayed()
    }

    fun hasAllCards(): PuzzleDashboardAssertions = apply {
        hasRatedPuzzleCard()
        hasPuzzleRushCard()
        hasPuzzleStreakCard()
        hasFailedPuzzlesCard()
    }
}
