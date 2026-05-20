package com.paulcraciunas.screens.common.model

import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.PuzzleInteractor
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import kotlinx.coroutines.delay

/**
 * Helper class that encapsulates common puzzle ViewModel logic.
 *
 * This class manages the interaction between [BoardViewDataBuilder] and [PuzzleInteractor],
 * providing a unified API for handling puzzle gameplay across different puzzle modes.
 *
 * Usage:
 * ```
 * val helper = PuzzleViewModelHelper(boardViewBuilder, puzzleInteractor)
 * helper.load(puzzle)
 *
 * when (val result = helper.handleSquareClick(selection)) {
 *     is ClickResult.SelectionCleared -> updateState()
 *     is ClickResult.PromotionRequired -> showPromotionDialog(result.at)
 *     is ClickResult.MovePlayed -> handleMoveResult()
 *     is ClickResult.Selected -> updateState()
 * }
 * ```
 */
class PuzzleViewModelHelper(
    private val puzzleInteractor: PuzzleInteractor,
) {
    private val boardViewBuilder = BoardViewDataBuilder()

    var autoPromote: Boolean = false

    val rating: Int
        get() = puzzleInteractor.rating

    val player: Side
        get() = puzzleInteractor.player

    val id: Int?
        get() = puzzleInteractor.id

    fun load(puzzle: Puzzle): PuzzleData {
        puzzleInteractor.load(puzzle)
        boardViewBuilder.load(puzzle)
        refreshBoardWithAnimation()

        return buildPuzzleData()
    }

    fun buildPuzzleData(): PuzzleData = PuzzleData(
        rating = rating,
        player = player,
        boardData = boardViewBuilder.build(),
        captured = puzzleInteractor.captured,
    )

    fun handleSquareClick(selection: Locus): OnSquareClick {
        var promotionAt: Locus? = null
        if (boardViewBuilder.selected != null) {
            val current = boardViewBuilder.selected!!
            when {
                current == selection || !puzzleInteractor.canPlay(current, selection) -> boardViewBuilder.clearSelection()
                puzzleInteractor.canPromote(current, selection) -> {
                    if (autoPromote) {
                        puzzleInteractor.promote(current, selection, Piece.Queen)
                        refreshBoardWithAnimation()
                    } else {
                        promotionAt = selection
                    }
                }
                else -> {
                    puzzleInteractor.play(current, selection)
                    refreshBoardWithAnimation()
                }
            }
        } else {
            val moves = puzzleInteractor.moves(selection)
            if (moves.isNotEmpty()) {
                boardViewBuilder.withSelection(selection, moves)
            }
        }
        return OnSquareClick(
            data = buildPuzzleData(),
            promotion = promotionAt?.let { Promotion(showChooser = true, at = it) },
            isOver = puzzleInteractor.isOver(),
            isSuccess = puzzleInteractor.isSuccess(),
        )
    }

    fun promote(to: Piece, at: Locus): OnSquareClick {
        puzzleInteractor.promote(boardViewBuilder.selected!!, at, to)
        refreshBoardWithAnimation()

        return OnSquareClick(
            data = buildPuzzleData(),
            promotion = null,
            isOver = puzzleInteractor.isOver(),
            isSuccess = puzzleInteractor.isSuccess(),
        )
    }

    fun hint(): Locus {
        val hintSquare = puzzleInteractor.hint()
        val moves = puzzleInteractor.moves(hintSquare)
        boardViewBuilder.withSelection(hintSquare, moves)
        return hintSquare
    }

    fun resign() {
        puzzleInteractor.resign()
    }

    suspend fun playSolution(updateUiState: (PuzzleData) -> Boolean) {
        while (hasSolutionMoves()) {
            delay(SOLUTION_MOVE_DELAY_MS)
            val nextData = playNextSolutionMove() ?: break
            // Update board state if still showing solution
            if (!updateUiState(nextData)) {
                break // User navigated away or state changed
            }
        }
    }

    /**
     * Plays the next expected move in the solution sequence.
     * This is used to animate the solution when the user resigns.
     *
     * @return The updated [PuzzleData] after playing the move, or null if the puzzle is already over.
     */
    fun playNextSolutionMove(): PuzzleData? {
        if (puzzleInteractor.isOver()) return null
        puzzleInteractor.playNextMove()
        refreshBoardWithAnimation()
        return buildPuzzleData()
    }

    fun hasSolutionMoves(): Boolean = !puzzleInteractor.isOver()

    private fun refreshBoardWithAnimation() {
        boardViewBuilder.refresh()
        puzzleInteractor.lastPly?.let { lastPly ->
            boardViewBuilder.withAnimatingPiece(lastPly.from, lastPly.to)
        }
    }

    data class OnSquareClick(
        val data: PuzzleData,
        val promotion: Promotion?,
        val isOver: Boolean,
        val isSuccess: Boolean,
    )

    data class Promotion(
        val showChooser: Boolean,
        val at: Locus,
    )
}

private const val SOLUTION_MOVE_DELAY_MS = 600L