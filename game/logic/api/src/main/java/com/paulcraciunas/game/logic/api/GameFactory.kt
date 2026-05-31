package com.paulcraciunas.game.logic.api

interface GameFactory {
    fun builder(): Builder
    fun moveValidator(): MoveValidator
}
