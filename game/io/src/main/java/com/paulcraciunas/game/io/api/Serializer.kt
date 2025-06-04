package com.paulcraciunas.game.io.api

import com.paulcraciunas.game.logic.Game
import javax.inject.Qualifier

@Qualifier
internal annotation class SerializerFen

@Qualifier
internal annotation class SerializerPgn

interface Serializer {
    @Throws(SerializeException::class)
    fun from(gameString: String): Game
    fun of(game: Game): String
}

class SerializeException(reason: String) : RuntimeException(reason)
