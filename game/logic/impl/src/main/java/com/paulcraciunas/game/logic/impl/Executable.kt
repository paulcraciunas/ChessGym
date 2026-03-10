package com.paulcraciunas.game.logic.impl

import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.diagnostics.PlayedMovesLog
import com.paulcraciunas.game.logic.api.state.CheckCount
import com.paulcraciunas.game.logic.impl.board.Board
import com.paulcraciunas.game.logic.impl.plies.Playable
import com.paulcraciunas.game.logic.impl.plies.PlyFactory

internal abstract class Executable {
    abstract val board: Board
    abstract val info: MutableGameInfo
    abstract val plyFactory: PlyFactory
    abstract val plies: MutableList<Playable>

    abstract fun isRunning(): Boolean
    abstract fun recomputeState()
    abstract fun savePly(playable: Playable)

    fun execute(ply: Ply) {
        assert(plies.contains(ply))
        val playable = plies.find { it == ply }!!

        // Execute and keep track
        playable.resolve(plies.filter { it.piece == ply.piece && it.to == ply.to }
            .disambiguate())
        playable.exec(board)
        savePly(playable)
        PlayedMovesLog.record(playable.algebraic())

        // Update state
        info.update(playable, checkCount = checkCount(info.turn.other()))
        updateState()
    }

    fun checkCount(turn: Side) =
        board.king(turn)?.let { plyFactory.checkCount(it, board, turn.other()) } ?: CheckCount.None

    fun updateState() {
        computeAvailablePlies()
        updateResolution()
    }

    private fun updateResolution() { // Important to call after updating game state
        recomputeState()
        if (!isRunning()) {
            plies.clear()
        }
    }

    private fun computeAvailablePlies() {
        plies.clear()
        plies.addAll(plyFactory.allLegalPlies(board, info))
    }
}

private fun List<Playable>.disambiguate(): Ply.Disambiguate = when {
    size >= 3 -> Ply.Disambiguate.Both
    size == 2 -> if (get(0).from.file == get(1).from.file) Ply.Disambiguate.Rank else Ply.Disambiguate.File
    else -> Ply.Disambiguate.None
}
