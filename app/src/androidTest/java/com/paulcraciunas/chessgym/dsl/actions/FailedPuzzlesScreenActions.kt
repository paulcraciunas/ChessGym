package com.paulcraciunas.chessgym.dsl.actions

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.paulcraciunas.chessgym.di.TestPuzzleInterceptor
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.board.ChessBoardTags
import com.paulcraciunas.screens.common.design.components.ChessGymDialogTags
import com.paulcraciunas.screens.common.dialogs.PromotionDialogTags
import com.paulcraciunas.screens.puzzles.dashboard.ui.PuzzleDashboardTags

/**
 * User actions that can be performed on the Failed Puzzles screen.
 *
 * Move orchestration (correct/incorrect moves) leans on the actual [Puzzle] captured by
 * [TestPuzzleInterceptor]. Because that [Puzzle] is the same mutable instance the view model uses,
 * calling [Puzzle.nextExpectedMove] between actions always reflects the current puzzle state.
 *
 * Note: This works reliably only when there is a single failed puzzle, because batch loading
 * causes the interceptor to capture the last puzzle in the batch rather than the currently
 * playing one.
 */
class FailedPuzzlesScreenActions(private val rule: ComposeTestRule) {

    /**
     * Navigates to the Failed Puzzles screen from the Puzzle Dashboard and waits for the puzzle
     * to finish loading so subsequent actions can read the puzzle state safely.
     */
    fun open(): FailedPuzzlesScreenActions = apply {
        rule.onNodeWithTag(PuzzleDashboardTags.Cards.FAILED_PUZZLES)
            .performScrollTo()
            .performClick()
        rule.waitForIdle()
        rule.waitUntil(timeoutMillis = PUZZLE_LOAD_TIMEOUT_MS) {
            TestPuzzleInterceptor.currentPuzzle != null
        }
    }

    fun playToEnd(): FailedPuzzlesScreenActions = apply {
        while (TestPuzzleInterceptor.currentPuzzle?.state?.isOver() == false) {
            playCorrectMove()
        }
    }

    /**
     * Plays the next expected move, driving the puzzle towards a successful completion.
     * Automatically selects a queen if the move is a promotion.
     */
    fun playCorrectMove(
        promoteTo: Piece = Piece.Queen,
    ): FailedPuzzlesScreenActions = apply {
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

    /**
     * Plays any legal move that is *not* the next expected move. Used to fail a puzzle.
     */
    fun playWrongMove(): FailedPuzzlesScreenActions = apply {
        val puzzle = requirePuzzle()
        val expected = requireNotNull(puzzle.nextExpectedMove()) {
            "Puzzle has no more expected moves"
        }
        val wrong = findLegalMoveOtherThan(puzzle, expected)
            ?: error("Could not find a legal move different from the expected one")
        clickSquares(wrong.first, wrong.second)
    }

    fun dismissCompletion(): FailedPuzzlesScreenActions = apply {
        rule.onNodeWithTag(ChessGymDialogTags.DISMISS).performClick()
        rule.waitForIdle()
    }

    private fun clickSquares(from: Locus, to: Locus): FailedPuzzlesScreenActions = apply {
        clickSquare(from)
        clickSquare(to)
    }

    private fun clickSquare(locus: Locus): FailedPuzzlesScreenActions = apply {
        rule.onNodeWithTag(ChessBoardTags.square(locus)).performClick()
        rule.waitForIdle()
    }

    private fun choosePromotion(piece: Piece): FailedPuzzlesScreenActions = apply {
        rule.onNodeWithTag(PromotionDialogTags.choice(piece)).performClick()
        rule.waitForIdle()
    }

    private fun requirePuzzle(): Puzzle = requireNotNull(TestPuzzleInterceptor.currentPuzzle) {
        "No puzzle has been loaded yet. Did you forget to call Given.user.withFailedPuzzles(...)?"
    }

    private fun findLegalMoveOtherThan(
        puzzle: Puzzle,
        expected: Pair<Locus, Locus>,
    ): Pair<Locus, Locus>? {
        Locus.entries.forEach { loc ->
            val plies = puzzle.plies(loc)
            for (ply in plies) {
                val move = loc to ply.to
                if (move != expected && !ply.isPromotion()) {
                    return move
                }
            }
        }
        return null
    }

    private companion object {
        const val PUZZLE_LOAD_TIMEOUT_MS: Long = 5_000L
    }
}
