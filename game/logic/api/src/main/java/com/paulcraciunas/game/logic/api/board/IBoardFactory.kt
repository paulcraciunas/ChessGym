package com.paulcraciunas.game.logic.api.board

interface IBoardFactory {
    fun defaultBoard(): IBoard
    fun emptyBoard(): IBoard
}
