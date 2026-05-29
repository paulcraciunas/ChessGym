package com.paulcraciunas.screens.data

import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.state.GameInfo

class PuzzlePlayableBoard(
    val puzzle: Puzzle,
) : PlayableBoard {
    override val board: IBoard get() = puzzle.board
    override val info: GameInfo get() = puzzle.info
    override val player: Side get() = puzzle.player
    override val activeSide: Side get() = puzzle.player
    override val rating: Int get() = puzzle.rating
    override val id: Int? get() = puzzle.id

    override fun initialize() {
        puzzle.start()
        puzzle.playNextMove()
    }

    override fun plies(from: Locus): List<Ply> = puzzle.plies(from)
    override fun ply(from: Locus, to: Locus): Ply? = puzzle.ply(from, to)

    override fun play(from: Locus, to: Locus) {
        puzzle.play(from, to)
        if (!isOver()) puzzle.playNextMove()
    }

    override fun play(ply: Ply) {
        puzzle.play(ply)
        if (!isOver()) puzzle.playNextMove()
    }
    override fun resign(): Unit = puzzle.resign()
    override fun isOver(): Boolean = puzzle.state.isOver()
    override fun outcome(): PlayableData.Outcome? = when {
        !isOver() -> null
        puzzle.state == Puzzle.State.Success -> PlayableData.Outcome.Won
        // puzzles can't draw
        else -> PlayableData.Outcome.Lost
    }
}
