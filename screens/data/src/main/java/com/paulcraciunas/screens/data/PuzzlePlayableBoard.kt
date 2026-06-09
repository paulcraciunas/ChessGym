package com.paulcraciunas.screens.data

import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.state.GameInfo

class PuzzlePlayableBoard(val puzzle: Puzzle) : PlayableBoard {
    override val board: IBoard get() = puzzle.board
    override val info: GameInfo get() = puzzle.info
    override val player: Side get() = puzzle.player
    override val playerSide: Side get() = puzzle.player
    override val rating: Int get() = puzzle.rating
    override val id: Int? get() = puzzle.id
    override val lastMovePly: Ply? get() = puzzle.info.lastPly

    override fun initialize() {
        puzzle.start()
        puzzle.playNextMove()
    }

    override fun plies(from: Locus): List<Ply> = puzzle.plies(from)
    override fun ply(from: Locus, to: Locus): Ply? = puzzle.ply(from, to)
    override fun play(from: Locus, to: Locus): Unit = puzzle.play(from, to)
    override fun play(ply: Ply): Unit = puzzle.play(ply)
    override fun resign(): Unit = puzzle.resign()
    override fun isPlayerTurn(): Boolean = info.turn == player
    override fun isOver(): Boolean = puzzle.state.isOver()
    override fun outcome(): Outcome? = when {
        !isOver() -> null
        puzzle.state == Puzzle.State.Success -> Outcome.Won
        // puzzles can't draw
        else -> Outcome.Lost
    }
}
