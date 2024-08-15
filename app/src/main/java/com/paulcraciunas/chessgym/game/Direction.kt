package com.paulcraciunas.chessgym.game

interface Delta {
    val dX: Int
    val dY: Int
}

enum class Direction(override val dX: Int, override val dY: Int) : Delta {
    LEFT(dX = -1, dY = 0),
    RIGHT(dX = 1, dY = 0),
    TOP(dX = 0, dY = 1),
    DOWN(dX = 0, dY = -1),
    TOP_LEFT(dX = -1, dY = 1),
    TOP_RIGHT(dX = 1, dY = 1),
    DOWN_LEFT(dX = -1, dY = -1),
    DOWN_RIGHT(dX = 1, dY = -1),
}
