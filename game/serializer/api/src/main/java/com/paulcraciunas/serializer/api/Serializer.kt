package com.paulcraciunas.serializer.api

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Puzzle

interface Serializer {
    @Throws(SerializeException::class)
    fun from(gameString: String): Game
    fun of(game: Game): String
    fun of(puzzle: Puzzle): String
}

class SerializeException(reason: String) : RuntimeException(reason)
