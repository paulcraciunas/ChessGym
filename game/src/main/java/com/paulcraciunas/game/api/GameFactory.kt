package com.paulcraciunas.game.api

import com.paulcraciunas.game.Game
import com.paulcraciunas.game.io.FenSerializer
import com.paulcraciunas.game.io.PgnSerializer
import com.paulcraciunas.game.io.SerializeException

object GameFactory {
    fun new(): IGame = Game()

    @Throws(SerializeException::class)
    fun fromPgn(pgn: String): IGame = PgnSerializer.from(pgn)

    @Throws(SerializeException::class)
    fun fromFen(fen: String): IGame = FenSerializer.from(fen)
}
