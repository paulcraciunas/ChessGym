package com.paulcraciunas.game.io.api

import com.paulcraciunas.game.logic.Game
import com.paulcraciunas.game.logic.GameState
import com.paulcraciunas.game.logic.board.Board
import javax.inject.Qualifier

@Qualifier
internal annotation class SerializerFen

@Qualifier
internal annotation class SerializerPgn

interface Serializer {
    @Throws(SerializeException::class)
    fun serialize(gameString: String): Pair<Board, GameState>
    @Throws(SerializeException::class)
    fun from(gameString: String): Game
    fun of(game: Game): String
}

class SerializeException(reason: String) : RuntimeException(reason)
