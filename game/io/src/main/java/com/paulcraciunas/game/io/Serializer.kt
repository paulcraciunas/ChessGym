package com.paulcraciunas.game.io

import com.paulcraciunas.game.Game

internal interface Serializer {
    @Throws(SerializeException::class)
    fun from(gameString: String): Game
    fun of(game: Game): String
}

class SerializeException(reason: String) : RuntimeException(reason)
