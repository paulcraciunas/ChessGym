package com.paulcraciunas.screens.common.model

import androidx.compose.runtime.Immutable
import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import kotlinx.coroutines.delay

/**
 * Helper class that encapsulates common ViewModel logic.
 *
 * This class manages the interaction between [BoardViewData2] and ViewModels,
 * providing a unified API for handling gameplay across different modes.
 *
 * Usage:
 * ```
 * val helper = PuzzleViewModelHelper()
 * helper.load(puzzle)
 * // ...
 * val result = helper.handleSquareClick(selection)
 * _uiState.updatePlaying {
 *     it.copy(data = result.data, promotion = result.promotion, isAnimating = result.isOver)
 * }
 * if (result.isOver) {
 *     onPuzzleCompleted(result)
 * }
 * ```
 *
 * Note: Don't forget to set the [autoPromote] flag
 */
class PuzzleViewModelHelper {
    private lateinit var puzzleData: PuzzleData
    private lateinit var puzzle: Puzzle
    var autoPromote: Boolean = false

    fun load(puzzle: Puzzle): PuzzleData {
        this.puzzle = puzzle
        puzzle.start()
        puzzle.playNextMove() // The first move always belongs to the opponent
        puzzleData = PuzzleData(
            rating = puzzle.rating,
            player = puzzle.player,
            id = puzzle.id,
            boardData = BoardViewData2.from(
                board = puzzle.board,
                lastMove = puzzle.info.lastPly.asPair()
            ),
            captured = updateCaptured(),
        )
        return puzzleData
    }

    fun current(): PuzzleData = puzzleData

    fun handleSquareClick(selection: Locus): OnSquareClick2 {
        var promotion: Promotion2? = null
        puzzleData = if (puzzleData.boardData.selection != null) {
            val current = puzzleData.boardData.selection!!
            when {
                current == selection || !canPlay(current, selection) -> {
                    puzzleData.copy(boardData = puzzleData.boardData.clearSelection())
                }
                canPromote(current, selection) -> {
                    if (autoPromote) {
                        puzzleData.copy(
                            boardData = promote(from = current, to = selection, result = Piece.Queen),
                            captured = updateCaptured()
                        )
                    } else {
                        promotion = Promotion2(showChooser = true, at = selection)
                        puzzleData
                    }
                }
                else -> puzzleData.copy(boardData = play(current, selection), captured = updateCaptured())
            }
        } else if (puzzle.board.has(puzzle.player, selection)) {
            puzzleData.copy(boardData = puzzleData.boardData.select(at = selection, moves = moves(from = selection)))
        } else {
            puzzleData
        }
        return OnSquareClick2(
            data = puzzleData,
            promotion = promotion,
            isOver = isOver(),
            isSuccess = isSuccess(),
        )
    }

    fun promote(to: Piece, at: Locus): OnSquareClick2 = OnSquareClick2(
        data = puzzleData.copy(boardData = promote(from = puzzleData.boardData.selection!!, to = at, result = to)),
        promotion = null,
        isOver = isOver(),
        isSuccess = isSuccess(),
    )

    fun hint(): PuzzleData {
        assert(puzzle.state == Puzzle.State.InProgress)

        val hintSquare = puzzle.nextExpectedMove()!!.first
        val moves = moves(hintSquare)
        return puzzleData.copy(boardData = puzzleData.boardData.clearSelection().select(hintSquare, moves))
    }

    fun resign() = puzzle.resign()

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
        if (isOver()) return null
        puzzle.playNextMove()
        puzzleData = puzzleData.copy(boardData = reloadBoard(), captured = updateCaptured())
        return puzzleData
    }

    @Immutable
    data class OnSquareClick2(
        val data: PuzzleData,
        val promotion: Promotion2?,
        val isOver: Boolean,
        val isSuccess: Boolean,
    )

    @Immutable
    data class Promotion2(
        val showChooser: Boolean,
        val at: Locus,
    )

    internal fun hasSolutionMoves(): Boolean = !isOver()
    private fun isOver(): Boolean = puzzle.state.isOver()
    private fun isSuccess(): Boolean = puzzle.state == Puzzle.State.Success

    private fun moves(from: Locus): List<Locus> = puzzle.plies(from).map { it.to }
    private fun canPlay(from: Locus, to: Locus): Boolean = puzzle.ply(from, to) != null
    private fun canPromote(from: Locus, to: Locus): Boolean = puzzle.ply(from, to)?.isPromotion() ?: false
    private fun promote(from: Locus, to: Locus, result: Piece): BoardViewData2 {
        assert(canPromote(from, to))

        puzzle.ply(from, to)!!.promote(result)
        return play(from, to)
    }

    private fun play(from: Locus, to: Locus): BoardViewData2 {
        assert(canPlay(from, to))
        puzzle.play(from, to)
        if (!isOver()) {
            puzzle.playNextMove()
        }
        return reloadBoard()
    }

    private fun reloadBoard(): BoardViewData2 = BoardViewData2.from(
        board = puzzle.board,
        lastMove = puzzle.info.lastPly.asPair(),
        withAnimation = true
    )

    private fun updateCaptured(): PuzzleData.Captured {
        val playerCaptured = mutableListOf<Piece>()
        val otherCaptured = mutableListOf<Piece>()
        worthSortedPieces.forEach { piece ->
            val otherCount = (piece.defaultCount - puzzle.board.pieces(puzzle.player.other(), piece).size).coerceAtLeast(0)
            val playerCount = (piece.defaultCount - puzzle.board.pieces(puzzle.player, piece).size).coerceAtLeast(0)
            repeat(otherCount) {
                playerCaptured.add(piece)
            }
            repeat(playerCount) {
                otherCaptured.add(piece)
            }
        }
        return PuzzleData.Captured(
            byPlayer = playerCaptured.joinToString(separator = "") { it.unicode },
            byOpponent = otherCaptured.joinToString(separator = "") { it.unicode },
        )
    }
}

private fun Ply?.asPair(): Pair<Locus, Locus>? = if (this != null) from to to else null
private val worthSortedPieces = listOf(Piece.Queen, Piece.Rook, Piece.Bishop, Piece.Knight, Piece.Pawn)

private const val SOLUTION_MOVE_DELAY_MS = 600L
