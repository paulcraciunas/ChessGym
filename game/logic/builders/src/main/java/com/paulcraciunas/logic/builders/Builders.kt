package com.paulcraciunas.logic.builders

import com.paulcraciunas.game.logic.api.GameFactory
import com.paulcraciunas.game.logic.api.MoveValidator
import com.paulcraciunas.game.logic.api.board.IBoardFactory
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.game.logic.impl.board.BoardFactory

object Builders {
    private val gameFactory = RealGameFactory()

    fun gameFactory(): GameFactory = gameFactory
    fun boardFactory(): IBoardFactory = BoardFactory
    fun moveValidator(): MoveValidator = gameFactory.moveValidator()
}
