package com.paulcraciunas.game.logic.impl

import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.diagnostics.PlayedMovesLog
import com.paulcraciunas.game.logic.api.state.CheckCount
import com.paulcraciunas.game.logic.impl.board.Board
import com.paulcraciunas.game.logic.impl.plies.Playable
import com.paulcraciunas.game.logic.impl.plies.PlyFactory
import java.util.ArrayDeque
import java.util.Queue

internal class MutablePuzzle(
    override val id: Int? = null,
    override val rating: Int,
    override val player: Side,
    override var state: Puzzle.State = Puzzle.State.Idle,
    override val info: MutableGameInfo,
    override val board: Board,
    override val expectedMoves: List<String>,
    override val plyFactory: PlyFactory = PlyFactory(),
    override val plies: MutableList<Playable> = mutableListOf()
) : Puzzle, Executable() {
    private val moveAdapter = MoveAdapter()
    private val remainingMoves: Queue<String> = ArrayDeque(expectedMoves)

    override fun start() {
        PlayedMovesLog.reset()
        state = Puzzle.State.InProgress
        info.inCheckCount = checkCount(info.turn)
        updateState()
    }
    override fun plies(from: Locus): List<Ply> = plies.filter { it.from == from }
    override fun ply(from: Locus, to: Locus): Ply? = plies.firstOrNull { it.from == from && it.to == to }

    override fun play(ply: Ply) {
        assert(isRunning())
        assert(remainingMoves.isNotEmpty())

        val expected = remainingMoves.poll()
        val promotedPiece = if (ply.isPromotion()) ply.algebraic().last().lowercase() else ""
        execute(ply)
        // check if the move is the first in the list of expected moves
        if (expected == "${ply.from}${ply.to}$promotedPiece") {
            if (remainingMoves.isEmpty()) { // Now check if we have any expected moves left
                state = Puzzle.State.Success
            }
        } else {
            // If the move played wasn't the expected one, check if it resulted in checkmate
            // (alternate checkmate moves should be accepted as success)
            state = if (info.inCheckCount != CheckCount.None && plies.isEmpty()) {
                Puzzle.State.Success
            } else {
                Puzzle.State.Failed
            }
        }
    }

    override fun play(from: Locus, to: Locus) = play(plies(from).first { it.to == to })

    override fun playNextMove() {
        assert(isRunning())
        assert(remainingMoves.isNotEmpty())

        val move = moveAdapter.from(remainingMoves.peek()) // We just peek here
        plies(move.from).first { it.to == move.to }.apply {
            move.promotion?.let { promote(it) }
            play(this) // we remove the move from 'remainingMoves' inside
        }
    }

    override fun resign() {
        assert(isRunning())

        state = Puzzle.State.Failed
    }

    override fun nextExpectedMove(): Pair<Locus, Locus>? = remainingMoves.peek()?.let {
        val move = moveAdapter.from(it)
        move.from to move.to
    }

    override fun isRunning(): Boolean = state == Puzzle.State.InProgress

    override fun recomputeState() {
        if (remainingMoves.isEmpty()) {
            state = Puzzle.State.Success
        }
    }

    override fun saveInfo() {}
}
