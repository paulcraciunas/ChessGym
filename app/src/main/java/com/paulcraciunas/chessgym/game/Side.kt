package com.paulcraciunas.chessgym.game

import com.paulcraciunas.chessgym.game.board.Rank

enum class Side {
    WHITE,
    BLACK;

    fun other(): Side = when (this) {
        WHITE -> BLACK
        BLACK -> WHITE
    }

    fun promotion(): Rank = if (this == WHITE) Rank.`8` else Rank.`1`
    fun pawnStart(): Rank = if (this == WHITE) Rank.`2` else Rank.`7`
}
