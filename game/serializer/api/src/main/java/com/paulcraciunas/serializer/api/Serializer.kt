package com.paulcraciunas.serializer.api

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.state.GameInfo

interface Serializer {
    @Throws(SerializeException::class)
    fun serialize(gameString: String): Pair<IBoard, GameInfo>
    @Throws(SerializeException::class)
    fun from(gameString: String): Game
    fun of(game: Game): String
}

class SerializeException(reason: String) : RuntimeException(reason)
