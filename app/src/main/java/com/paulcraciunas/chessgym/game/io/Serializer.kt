package com.paulcraciunas.chessgym.game.io

import com.paulcraciunas.chessgym.game.Game

interface Serializer {
    @Throws(SerializeException::class)
    fun from(game: String): Game
}

class SerializeException(reason: String) : RuntimeException(reason)
