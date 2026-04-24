package com.paulcraciunas.chessgym.dsl.actions

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.paulcraciunas.screens.puzzles.dashboard.ui.PuzzleDashboardTags

class PuzzleDashboardActions(private val rule: ComposeTestRule) {

    fun openRatedPuzzle(): PuzzleDashboardActions = clickCard(PuzzleDashboardTags.Cards.RATED_PUZZLE)

    fun openPuzzleRush(): PuzzleDashboardActions = clickCard(PuzzleDashboardTags.Cards.PUZZLE_RUSH)

    fun openPuzzleStreak(): PuzzleDashboardActions = clickCard(PuzzleDashboardTags.Cards.PUZZLE_STREAK)

    fun openFailedPuzzles(): PuzzleDashboardActions = clickCard(PuzzleDashboardTags.Cards.FAILED_PUZZLES)

    private fun clickCard(tag: String): PuzzleDashboardActions = apply {
        rule.onNodeWithTag(tag).performScrollTo().performClick()
        rule.waitForIdle()
    }
}
