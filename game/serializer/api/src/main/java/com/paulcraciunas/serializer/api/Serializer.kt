package com.paulcraciunas.serializer.api

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Puzzle

interface Serializer {
    @Throws(SerializeException::class)
    fun from(gameString: String): Game
    fun of(game: Game): String
    fun of(puzzle: Puzzle): String

    companion object {
        const val STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    }
}

class SerializeException(reason: String) : RuntimeException(reason)
