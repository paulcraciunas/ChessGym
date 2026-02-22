package com.paulcraciunas.game.logic.impl

import com.paulcraciunas.game.logic.api.Builder
import com.paulcraciunas.game.logic.api.GameFactory
import com.paulcraciunas.game.logic.api.MoveValidator
import com.paulcraciunas.game.logic.api.PuzzleInteractor
import com.paulcraciunas.game.logic.impl.plies.PlyFactory

class RealGameFactory : GameFactory {
    private val plyFactory: PlyFactory = PlyFactory()
    private val moveValidator: MoveValidator = RealMoveValidator()

    override fun builder(): Builder = RealBuilder(plyFactory)
    override fun puzzleInteractor(): PuzzleInteractor = RealPuzzleInteractor()
    override fun moveValidator(): MoveValidator = moveValidator
}
