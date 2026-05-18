package com.paulcraciunas.game.logic.api

interface GameFactory {
    fun builder(): Builder
    fun puzzleInteractor(): PuzzleInteractor
    fun gameInteractor(): GameInteractor
    fun moveValidator(): MoveValidator
}
