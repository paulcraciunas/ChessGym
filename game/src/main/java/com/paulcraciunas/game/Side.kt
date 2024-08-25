package com.paulcraciunas.game

enum class Side {
    WHITE,
    BLACK;

    fun other(): Side = when (this) {
        WHITE -> BLACK
        BLACK -> WHITE
    }
}
