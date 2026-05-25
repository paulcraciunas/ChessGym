package com.paulcraciunas.chessgym.dsl.actions

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.paulcraciunas.chessgym.di.TestPuzzleInterceptor
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.board.ChessBoardTags
import com.paulcraciunas.screens.common.controls.DefaultPuzzleControlsTags
import com.paulcraciunas.screens.common.design.components.ChessGymDialogTags
import com.paulcraciunas.screens.common.dialogs.PromotionDialogTags
import com.paulcraciunas.screens.puzzles.dashboard.ui.PuzzleDashboardTags
import com.paulcraciunas.screens.puzzles.rated.ui.RatedPuzzleScreenTags

/**
 * User actions that can be performed on the Rated Puzzle screen.
 *
 * Move orchestration (correct/incorrect/hinted moves) leans on the actual [Puzzle] captured by
 * [TestPuzzleInterceptor]. Because that [Puzzle] is the same mutable instance the view model uses,
 * calling [Puzzle.nextExpectedMove] between actions always reflects the current puzzle state.
 */
class RatedPuzzleScreenActions(private val rule: ComposeTestRule) {

    /**
     * Navigates to the Rated Puzzle screen from the Puzzle Dashboard and waits for the puzzle
     * to finish loading so subsequent actions can read the puzzle state safely.
     */
    fun open(): RatedPuzzleScreenActions = apply {
        rule.onNodeWithTag(PuzzleDashboardTags.Cards.RATED_PUZZLE).performClick()
        rule.waitForIdle()
        rule.waitUntil(timeoutMillis = PUZZLE_LOAD_TIMEOUT_MS) {
            TestPuzzleInterceptor.currentPuzzle != null
        }
    }

    fun playToEnd(): RatedPuzzleScreenActions = apply {
        while (TestPuzzleInterceptor.currentPuzzle?.state?.isOver() == false) {
            playCorrectMove()
        }
    }

    fun playToEndWithSelection(
        promoteTo: Piece = Piece.Queen,
    ): RatedPuzzleScreenActions = apply {
        // Given a square is already selected, play the first move
        val puzzle = requirePuzzle()
        val (from, to) = requireNotNull(puzzle.nextExpectedMove()) {
            "Puzzle has no more expected moves"
        }
        val isPromotion = puzzle.ply(from, to)?.isPromotion() == true
        clickSquare(to)
        if (isPromotion) {
            choosePromotion(promoteTo)
        }
        // Then, play till the end
        playToEnd()
    }

    /**
     * Plays the next expected move, driving the puzzle towards a successful completion.
     * Automatically selects a queen if the move is a promotion.
     */
    fun playCorrectMove(
        promoteTo: Piece = Piece.Queen,
    ): RatedPuzzleScreenActions = apply {
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
    fun playWrongMove(): RatedPuzzleScreenActions = apply {
        val puzzle = requirePuzzle()
        val expected = requireNotNull(puzzle.nextExpectedMove()) {
            "Puzzle has no more expected moves"
        }
        val wrong = findLegalMoveOtherThan(puzzle, expected)
            ?: error("Could not find a legal move different from the expected one")
        clickSquares(wrong.first, wrong.second)
    }

    /**
     * Plays a pre-chosen from/to sequence. Useful for carefully orchestrated scenarios like
     * performing a wrong move that lands on a promotion square.
     */
    fun clickSquares(from: Locus, to: Locus): RatedPuzzleScreenActions = apply {
        clickSquare(from)
        clickSquare(to)
    }

    fun clickSquare(locus: Locus): RatedPuzzleScreenActions = apply {
        rule.onNodeWithTag(ChessBoardTags.square(locus)).performClick()
        rule.waitForIdle()
    }

    fun requestHint(): RatedPuzzleScreenActions = apply {
        rule.onNodeWithTag(DefaultPuzzleControlsTags.HINT).performClick()
        rule.waitForIdle()
    }

    fun requestAbandon(): RatedPuzzleScreenActions = apply {
        rule.onNodeWithTag(DefaultPuzzleControlsTags.ABANDON).performClick()
        rule.waitForIdle()
    }

    fun confirmAbandon(): RatedPuzzleScreenActions = apply {
        rule.onNodeWithTag(ChessGymDialogTags.CONFIRM).performClick()
        rule.waitForIdle()
    }

    fun dismissAbandon(): RatedPuzzleScreenActions = apply {
        rule.onNodeWithTag(ChessGymDialogTags.DISMISS).performClick()
        rule.waitForIdle()
    }

    fun choosePromotion(piece: Piece): RatedPuzzleScreenActions = apply {
        rule.onNodeWithTag(PromotionDialogTags.choice(piece)).performClick()
        rule.waitForIdle()
    }

    fun playNext(): RatedPuzzleScreenActions = apply {
        rule.onNodeWithTag(RatedPuzzleScreenTags.Finished.PLAY_NEXT).performClick()
        rule.waitForIdle()
    }

    private fun requirePuzzle(): Puzzle = requireNotNull(TestPuzzleInterceptor.currentPuzzle) {
        "No puzzle has been loaded yet. Did you forget to call Given.puzzle.withRating(...)?"
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
