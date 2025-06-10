package com.paulcraciunas.game.logic.api

enum class Side(val code: Int) {
    WHITE(code = 0),
    BLACK(code = 1);

    fun other(): Side = when (this) {
        WHITE -> BLACK
        BLACK -> WHITE
    }

    companion object {
        fun fromCode(side: Int): Side =
            if (side in 0..1) Side.entries[side]
            else throw IllegalArgumentException("Wrong decimal value. Expecting [0 - 1]")
    }
}