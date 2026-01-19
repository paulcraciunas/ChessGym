package com.paulcraciunas.game.logic.impl

import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.PuzzleInteractor
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

class RealPuzzleInteractor : PuzzleInteractor {
    private val _captured = mutableMapOf<Side, MutableList<Piece>>().apply {
        this[Side.WHITE] = mutableListOf()
        this[Side.BLACK] = mutableListOf()
    }
    private var _puzzle: MutablePuzzle? = null
    private val puzzle: Puzzle
        get() = _puzzle!!
    override var captured = _captured
    override val id: Int?
        get() = puzzle.id
    override val rating: Int
        get() = puzzle.rating
    override val player: Side
        get() = puzzle.player
    override val lastPly: Ply?
        get() = puzzle.info.lastPly

    override fun load(puzzle: Puzzle) {
        _puzzle = puzzle as MutablePuzzle

        puzzle.start()
        puzzle.playNextMove() // The first move always belongs to the opponent
        updateCaptured()
    }

    override fun canPlay(from: Locus, to: Locus): Boolean = puzzle.ply(from, to) != null
    override fun moves(from: Locus): List<Locus> = puzzle.plies(from).map { it.to }
    override fun play(from: Locus, to: Locus) {
        assert(canPlay(from, to))
        // TODO Paul: maybe we should change this to exposing a flow
        // so that we can push these 2 states separately
        // and then in the UI, we can add a delay of 100ms or something
        // to show the puzzle actually progressing?!
        // TODO Paul: test how it looks without the flow and then decide
        puzzle.play(from, to)
        if (!isOver()) {
            puzzle.playNextMove()
        }
        updateCaptured()
    }

    override fun canPromote(from: Locus, to: Locus): Boolean = puzzle.ply(from, to)?.isPromotion() ?: false
    override fun promote(from: Locus, to: Locus, result: Piece) {
        assert(canPromote(from, to))

        puzzle.ply(from, to)!!.promote(result)
        play(from, to)
    }

    override fun hint(): Locus {
        assert(puzzle.state == Puzzle.State.InProgress)

        return puzzle.nextExpectedMove()!!.first
    }

    override fun resign() {
        puzzle.resign()
    }

    override fun isOver(): Boolean = puzzle.state.isOver()

    override fun isSuccess(): Boolean = puzzle.state == Puzzle.State.Success

    private fun updateCaptured() {
        _captured[Side.WHITE]!!.clear()
        _captured[Side.BLACK]!!.clear()
        for (side in Side.entries) {
            for (piece in Piece.entries) {
                val missing = piece.startingCount() - puzzle.board.pieces(side, piece).size
                (0 until missing).forEach { _ ->
                    _captured[side.other()]!!.add(piece)
                }
            }
        }
    }
}

private fun Piece.startingCount(): Int = when (this) {
    Piece.Pawn -> 8
    Piece.Knight -> 2
    Piece.Bishop -> 2
    Piece.Rook -> 2
    Piece.Queen -> 1
    Piece.King -> 0 // Don't care about the king, even if we have king-less puzzles
}
