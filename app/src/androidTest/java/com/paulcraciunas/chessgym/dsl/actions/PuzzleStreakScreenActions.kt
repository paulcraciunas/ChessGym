package com.paulcraciunas.chessgym.dsl.actions

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.paulcraciunas.chessgym.di.TestPuzzleInterceptor
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.screens.common.board.ChessBoardTags
import com.paulcraciunas.screens.common.controls.DefaultPuzzleControlsTags
import com.paulcraciunas.screens.common.design.components.ChessGymDialogTags
import com.paulcraciunas.screens.common.dialogs.PromotionDialogTags
import com.paulcraciunas.screens.puzzles.dashboard.ui.PuzzleDashboardTags
import com.paulcraciunas.screens.puzzles.streak.ui.PuzzleStreakScreenTags

/**
 * User actions that can be performed on the Puzzle Streak screen.
 *
 * Move orchestration leans on the actual [Puzzle] captured by [TestPuzzleInterceptor].
 * Puzzle Streak loads one puzzle at a time, so the interceptor always has the correct puzzle.
 */
class PuzzleStreakScreenActions(private val rule: ComposeTestRule) {

    /**
     * Navigates to the Puzzle Streak screen from the Puzzle Dashboard and waits for the puzzle
     * to finish loading so subsequent actions can read the puzzle state safely.
     */
    fun open(): PuzzleStreakScreenActions = apply {
        rule.onNodeWithTag(PuzzleDashboardTags.Cards.PUZZLE_STREAK)
            .performScrollTo()
            .performClick()
        rule.waitForIdle()
        rule.waitUntil(timeoutMillis = PUZZLE_LOAD_TIMEOUT_MS) {
            TestPuzzleInterceptor.currentPuzzle != null
        }
    }

    /**
     * Plays all correct moves for the **current** puzzle only.
     *
     * Captures the puzzle instance up-front so that the loop exits as soon as that specific
     * puzzle is solved, even if the VM has already loaded the next puzzle into the interceptor.
     */
    fun playToEnd(): PuzzleStreakScreenActions = apply {
        val puzzle = requirePuzzle()
        while (!puzzle.state.isOver()) {
            playCorrectMove()
        }
    }

    fun playCorrectMove(
        promoteTo: Piece = Piece.Queen,
    ): PuzzleStreakScreenActions = apply {
        val puzzle = requirePuzzle()
        val (from, to) = requireNotNull(puzzle.nextExpectedMove()) {
            "Puzzle has no more expected moves"
        }
        val isPromotion = puzzle.ply(from, to)?.isPromotion() == true
        clickSquares(from, to)
        if (isPromotion) {
            choosePromotion(promoteTo)
        }
    }

    fun playWrongMove(): PuzzleStreakScreenActions = apply {
        val puzzle = requirePuzzle()
        val expected = requireNotNull(puzzle.nextExpectedMove()) {
            "Puzzle has no more expected moves"
        }
        val wrong = findLegalMoveOtherThan(puzzle, expected)
            ?: error("Could not find a legal move different from the expected one")
        clickSquares(wrong.first, wrong.second)
    }

    fun requestHint(): PuzzleStreakScreenActions = apply {
        rule.onNodeWithTag(DefaultPuzzleControlsTags.HINT).performClick()
        rule.waitForIdle()
    }

    fun requestAbandon(): PuzzleStreakScreenActions = apply {
        rule.onNodeWithTag(DefaultPuzzleControlsTags.ABANDON).performClick()
        rule.waitForIdle()
    }

    fun confirmAbandon(): PuzzleStreakScreenActions = apply {
        rule.onNodeWithTag(ChessGymDialogTags.CONFIRM).performClick()
        rule.waitForIdle()
    }

    fun dismissAbandon(): PuzzleStreakScreenActions = apply {
        rule.onNodeWithTag(ChessGymDialogTags.DISMISS).performClick()
        rule.waitForIdle()
    }

    fun dismissSummary(): PuzzleStreakScreenActions = apply {
        rule.onNodeWithTag(PuzzleStreakScreenTags.Summary.DISMISS).performClick()
        rule.waitForIdle()
    }

    fun startNewStreak(): PuzzleStreakScreenActions = apply {
        rule.onNodeWithTag(PuzzleStreakScreenTags.Ended.NEW_STREAK).performClick()
        rule.waitForIdle()
        rule.waitUntil(timeoutMillis = PUZZLE_LOAD_TIMEOUT_MS) {
            TestPuzzleInterceptor.currentPuzzle?.state?.isOver() == false
        }
    }

    private fun clickSquares(from: Locus, to: Locus): PuzzleStreakScreenActions = apply {
        clickSquare(from)
        clickSquare(to)
    }

    private fun clickSquare(locus: Locus): PuzzleStreakScreenActions = apply {
        rule.onNodeWithTag(ChessBoardTags.square(locus)).performClick()
        rule.waitForIdle()
    }

    private fun choosePromotion(piece: Piece): PuzzleStreakScreenActions = apply {
        rule.onNodeWithTag(PromotionDialogTags.choice(piece)).performClick()
        rule.waitForIdle()
    }

    private fun requirePuzzle(): Puzzle = requireNotNull(TestPuzzleInterceptor.currentPuzzle) {
        "No puzzle has been loaded yet. Did you forget to open the Puzzle Streak screen?"
    }

    private fun findLegalMoveOtherThan(
        puzzle: Puzzle,
        expected: Pair<Locus, Locus>,
    ): Pair<Locus, Locus>? {
        for (file in File.entries) {
            for (rank in Rank.entries) {
                val from = Locus(file, rank)
                val plies = puzzle.plies(from)
                for (ply in plies) {
                    val move = from to ply.to
                    if (move != expected && !ply.isPromotion()) {
                        return move
                    }
                }
            }
        }
        return null
    }

    private companion object {
        const val PUZZLE_LOAD_TIMEOUT_MS: Long = 5_000L
    }
}
