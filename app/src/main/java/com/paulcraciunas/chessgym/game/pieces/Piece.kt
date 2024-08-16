package com.paulcraciunas.chessgym.game.pieces

import com.paulcraciunas.chessgym.game.Delta
import com.paulcraciunas.chessgym.game.Direction
import com.paulcraciunas.chessgym.game.Position
import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.toPosition
import com.paulcraciunas.chessgym.game.toSide

abstract class Piece(
    val side: Side,
    private var position: Position,
) {
    constructor(side: Char, algebraicPos: String) : this(side.toSide(), algebraicPos.toPosition())

    abstract val symbol: Char?
    abstract val value: Int
    var moved: Boolean = false // Important for checking castling, en-passent
        private set

    fun getPosition(): Position = position

    fun moveTo(newPosition: Position) {
        assert(newPosition.isValid())

        position = newPosition
        moved = true
    }

    abstract fun allPossibleMoves(): List<Position>

    protected fun MutableList<Position>.addMovesInDirection(
        direction: Direction,
    ) {
        var newPos = position.next(direction)
        while (newPos.isValid()) {
            add(newPos)
            newPos = newPos.next(direction)
        }
    }

    protected fun MutableList<Position>.addMoves(
        deltas: List<Delta>
    ) {
        var newPos: Position
        deltas.forEach {
            newPos = position.next(it)
            if (newPos.isValid() && newPos != position) {
                add(newPos)
            }
        }
    }
}

