package com.paulcraciunas.game.logic.impl

import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.board.Board
import com.paulcraciunas.game.logic.impl.plies.Playable
import com.paulcraciunas.game.logic.impl.plies.PlyFactory
import java.util.Queue

class MutablePuzzle(
    override val rating: Int,
    override val player: Side,
    override var state: Puzzle.State = Puzzle.State.Idle,
    override val info: MutableGameInfo,
    override val board: Board,
    private val moves: Queue<String>,
    override val plyFactory: PlyFactory = PlyFactory(),
) : Puzzle, Executable() {

    override fun start() {
        state = Puzzle.State.InProgress
        info.inCheckCount = checkCount(info.turn)
        updateState()
    }

    override fun play(ply: Ply) {
        assert(moves.isNotEmpty())
        assert(state == Puzzle.State.InProgress)

        val expected = moves.poll()
        val promotedPiece = if (ply.isPromotion()) ply.algebraic().last().lowercase() else ""
        // check if the move is the first in the list of expected moves
        if (expected == "${ply.from}${ply.to}$promotedPiece") {
            execute(ply)
            // TODO Paul: also play the move for the opponent?!
            if (moves.isEmpty()) { // Now check if we have any expected moves left
                state = Puzzle.State.Success
            }
        } else {
            // If the move played wasn't the expected one, we failed
            state = Puzzle.State.Failed
        }
    }

    override fun play(from: Locus, to: Locus) = play(info.plies(from).first { it.to == to })

    override fun abandon() {
        assert(state == Puzzle.State.InProgress)

        state = Puzzle.State.Failed
    }

    override fun hint(): Piece {
        assert(state == Puzzle.State.InProgress)
        assert(moves.isNotEmpty())

        val from = Locus.from(moves.peek().substring(0, 2))!!
        return board.at(from)!!
    }

    override fun isRunning(): Boolean = state == Puzzle.State.InProgress

    override fun recomputeState() {
        if (moves.isEmpty()) {
            state = Puzzle.State.Success
        }
    }

    override fun savePly(playable: Playable) {}
}
