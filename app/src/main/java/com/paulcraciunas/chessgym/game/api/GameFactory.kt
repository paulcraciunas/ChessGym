package com.paulcraciunas.chessgym.game.api

import com.paulcraciunas.chessgym.game.Game
import com.paulcraciunas.chessgym.game.io.FenSerializer
import com.paulcraciunas.chessgym.game.io.PgnSerializer
import com.paulcraciunas.chessgym.game.io.SerializeException

object GameFactory {
    fun new(): IGame = Game()

    @Throws(SerializeException::class)
    fun fromPgn(pgn: String): IGame = PgnSerializer.from(pgn)

    @Throws(SerializeException::class)
    fun fromFen(fen: String): IGame = FenSerializer.from(fen)
}
