package com.paulcraciunas.game.logic.impl

import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.state.CheckCount
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
    private val plyFactory: PlyFactory = PlyFactory(),
) : Puzzle {

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
            playMove(ply)
            // TODO Paul: also play the move for the opponent?!
            if (moves.isEmpty()) { // Now check if we have any expected moves left
                state = Puzzle.State.Success
            }
        } else {
            // If the move played wasn't the expected one, we failed
            state = Puzzle.State.Failed
        }
    }

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

    private fun updateState() {
        computeAvailablePlies()
        updateResolution()
    }

    private fun updateResolution() {
        if (moves.isEmpty()) {
            state = Puzzle.State.Success
            info.plies.clear()
        }
    }

    // TODO Paul: Reuse everything below this line
    private fun computeAvailablePlies() {
        info.plies.clear()
        info.plies.addAll(plyFactory.allLegalPlies(board, info))
    }

    private fun playMove(ply: Ply) {
        assert(state == Puzzle.State.InProgress)
        assert(info.plies.contains(ply))
        val playable = info.plies.find { it == ply }!!

        // Execute and keep track
        playable.resolve(info.plies.filter { it.piece == ply.piece && it.to == ply.to }
            .disambiguate())
        playable.exec(board)

        // Update state
        info.update(playable, checkCount = checkCount(info.turn.other()))
        updateState()
    }

    private fun checkCount(turn: Side) =
        board.king(turn)?.let { plyFactory.checkCount(it, board, turn.other()) } ?: CheckCount.None
}

private fun List<Playable>.disambiguate(): Ply.Disambiguate = when {
    size >= 3 -> Ply.Disambiguate.Both
    size == 2 -> if (get(0).from.file == get(1).from.file) Ply.Disambiguate.Rank else Ply.Disambiguate.File
    else -> Ply.Disambiguate.None
}

