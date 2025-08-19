package com.paulcraciunas.game.logic.impl

import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.impl.board.Board
import com.paulcraciunas.game.logic.impl.plies.Playable
import com.paulcraciunas.game.logic.impl.plies.PlyFactory
import java.util.ArrayDeque
import java.util.Queue

internal class MutablePuzzle(
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
    val moves: Queue<String> = ArrayDeque(expectedMoves)

    override fun start() {
        state = Puzzle.State.InProgress
        info.inCheckCount = checkCount(info.turn)
        updateState()
    }
    override fun plies(from: Locus): List<Ply> = plies.filter { it.from == from }
    override fun ply(from: Locus, to: Locus): Ply? = plies.firstOrNull { it.from == from && it.to == to }

    override fun play(ply: Ply) {
        assert(isRunning())
        assert(moves.isNotEmpty())

        val expected = moves.poll()
        val promotedPiece = if (ply.isPromotion()) ply.algebraic().last().lowercase() else ""
        // check if the move is the first in the list of expected moves
        if (expected == "${ply.from}${ply.to}$promotedPiece") {
            execute(ply)
            if (moves.isEmpty()) { // Now check if we have any expected moves left
                state = Puzzle.State.Success
            }
        } else {
            // If the move played wasn't the expected one, we failed
            state = Puzzle.State.Failed
        }
    }

    override fun play(from: Locus, to: Locus) = play(plies(from).first { it.to == to })

    override fun playNextMove() {
        assert(isRunning())
        assert(moves.isNotEmpty())

        val move = moveAdapter.from(moves.peek())
        plies(move.from).first { it.to == move.to }.apply {
            move.promotion?.let { promote(it) }
            play(this)
        }
    }

    override fun resign() {
        assert(isRunning())

        state = Puzzle.State.Failed
    }

    override fun isRunning(): Boolean = state == Puzzle.State.InProgress

    override fun recomputeState() {
        if (moves.isEmpty()) {
            state = Puzzle.State.Success
        }
    }

    override fun savePly(playable: Playable) {}
}
