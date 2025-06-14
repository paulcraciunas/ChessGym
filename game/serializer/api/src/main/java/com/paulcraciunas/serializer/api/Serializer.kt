package com.paulcraciunas.serializer.api

import com.paulcraciunas.game.logic.api.Game

interface Serializer {
    @Throws(SerializeException::class)
    fun from(gameString: String): Game
    fun of(game: Game): String
}

class SerializeException(reason: String) : RuntimeException(reason)
