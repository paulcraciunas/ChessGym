package com.paulcraciunas.game.plies.strategies

import com.paulcraciunas.game.CheckCount
import com.paulcraciunas.game.GameState
import com.paulcraciunas.game.Side
import com.paulcraciunas.game.board.Board
import com.paulcraciunas.game.board.Locus
import com.paulcraciunas.game.board.Piece
import com.paulcraciunas.game.plies.Ply
import com.paulcraciunas.game.plies.StandardPly

internal typealias Next = (Locus) -> Locus?

internal abstract class PlyStrategy {
    abstract val piece: Piece

    protected open fun canMoveInCheck(count: CheckCount): Boolean = count != CheckCount.Two
    protected open fun directions(): Collection<Next> = emptyList()
    protected open fun simpleMoves(): Collection<Next> = emptyList()
    protected open fun MutableList<Ply>.addComplexPlies(from: Locus, on: Board, with: GameState) {}

    open fun canAttack(from: Locus, to: Locus, on: Board, turn: Side): Boolean {
        assert(on.has(piece, turn, from))
        assert(from != to)

        return mutableListOf<Ply>().apply {
            addPliesInDirections(turn, from, on) { it == to }
            addSimplePlies(turn, from, on, simpleMoves()) { it == to }
        }.any { it.to == to }
    }

    fun plies(from: Locus, on: Board, with: GameState): List<Ply> {
        assert(on.has(piece, with.turn, from))

        return mutableListOf<Ply>().apply {
            if (canMoveInCheck(with.inCheckCount)) {
                addPliesInDirections(with.turn, from, on)
                addSimplePlies(with.turn, from, on, simpleMoves())
                addComplexPlies(from, on, with)
            }
        }
    }

    protected fun MutableList<Ply>.addSimplePlies(
        side: Side,
        from: Locus,
        on: Board,
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

    private fun MutableList<Ply>.addPliesInDirections(
        side: Side,
        from: Locus,
        on: Board,
        verify: (Locus) -> Boolean = { true },
    ) {
        directions().forEach { direction ->
            addPliesInDirection(side, from, on, direction, verify)
        }
    }

    private fun MutableList<Ply>.addPliesInDirection(
        side: Side,
        from: Locus,
        on: Board,
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
