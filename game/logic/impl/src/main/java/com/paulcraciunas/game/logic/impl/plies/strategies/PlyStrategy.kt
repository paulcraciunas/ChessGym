package com.paulcraciunas.game.logic.impl.plies.strategies

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.state.CheckCount
import com.paulcraciunas.game.logic.impl.GameState
import com.paulcraciunas.game.logic.impl.plies.Playable
import com.paulcraciunas.game.logic.impl.plies.StandardPly

internal typealias Next = (Locus) -> Locus?

internal abstract class PlyStrategy {
    abstract val piece: Piece

    protected open fun canMoveInCheck(count: CheckCount): Boolean = count != CheckCount.Two
    protected open fun directions(): Collection<Next> = emptyList()
    protected open fun simpleMoves(): Collection<Next> = emptyList()
    protected open fun MutableList<Playable>.addComplexPlies(from: Locus, on: IBoard, with: GameState) {}

    open fun canAttack(from: Locus, to: Locus, on: IBoard, turn: Side): Boolean {
        assert(on.has(piece, turn, from))
        assert(from != to)

        return mutableListOf<Playable>().apply {
            addPliesInDirections(turn, from, on) { it == to }
            addSimplePlies(turn, from, on, simpleMoves()) { it == to }
        }.any { it.to == to }
    }

    fun plies(from: Locus, on: IBoard, with: GameState): List<Playable> {
        assert(on.has(piece, with.turn, from))

        return mutableListOf<Playable>().apply {
            if (canMoveInCheck(with.inCheckCount)) {
                addPliesInDirections(with.turn, from, on)
                addSimplePlies(with.turn, from, on, simpleMoves())
                addComplexPlies(from, on, with)
            }
        }
    }

    protected fun MutableList<Playable>.addSimplePlies(
        side: Side,
        from: Locus,
        on: IBoard,
        nexts: Collection<Next>,
        verify: (Locus) -> Boolean = { true },
    ) {
        nexts.forEach { next ->
            val loc = next(from)
            if (loc != null && !on.has(side, loc) && verify(loc)) {
                add(StandardPly(side, piece, from = from, to = loc, captured = on.at(loc)))
            }
        }
    }

    private fun MutableList<Playable>.addPliesInDirections(
        side: Side,
        from: Locus,
        on: IBoard,
        verify: (Locus) -> Boolean = { true },
    ) {
        directions().forEach { direction ->
            addPliesInDirection(side, from, on, direction, verify)
        }
    }

    private fun MutableList<Playable>.addPliesInDirection(
        side: Side,
        from: Locus,
        on: IBoard,
        next: Next,
        verify: (Locus) -> Boolean,
    ) {
        var loc = next(from)
        // Stop if we're out of the board, if we met an ally
        while (loc != null && !on.has(side, loc)) {
            // Only add if the location is verified
            if (verify(loc)) {
                add(StandardPly(side, piece, from = from, to = loc, captured = on.at(loc)))
            }
            // If we reach an enemy, stop
            if (on.at(loc) != null) {
                return
            }
            loc = next(loc)
        }
    }
}
