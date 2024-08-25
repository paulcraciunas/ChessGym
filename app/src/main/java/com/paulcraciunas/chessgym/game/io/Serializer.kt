package com.paulcraciunas.chessgym.game.io

import com.paulcraciunas.chessgym.game.Game

interface Serializer {
    @Throws(SerializeException::class)
    fun from(gameString: String): Game
    fun of(game: Game): String
}

class SerializeException(reason: String) : RuntimeException(reason)
