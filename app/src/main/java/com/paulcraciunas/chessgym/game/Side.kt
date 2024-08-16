package com.paulcraciunas.chessgym.game

enum class Side {
    WHITE,
    BLACK;

    fun toggle(): Side =
        when (this) {
            WHITE -> BLACK
            BLACK -> WHITE
        }
}

fun Char.toSide(): Side = when (this) {
    'w', 'W' -> Side.WHITE
    'b', 'B' -> Side.BLACK
    else -> throw IllegalArgumentException("Unknown side")
}
