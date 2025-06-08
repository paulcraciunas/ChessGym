package com.paulcraciunas.serializer.api

import com.paulcraciunas.game.logic.api.IGame
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.state.IGameState

interface Serializer {
    @Throws(SerializeException::class)
    fun serialize(gameString: String): Pair<IBoard, IGameState>
    @Throws(SerializeException::class)
    fun from(gameString: String): IGame
    fun of(game: IGame): String
}

class SerializeException(reason: String) : RuntimeException(reason)
