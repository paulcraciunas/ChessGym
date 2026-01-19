package com.paulcraciunas.game.logic.impl

import com.paulcraciunas.game.logic.api.Builder
import com.paulcraciunas.game.logic.api.GameFactory
import com.paulcraciunas.game.logic.api.PuzzleInteractor
import com.paulcraciunas.game.logic.impl.plies.PlyFactory

class RealGameFactory : GameFactory {
    private val plyFactory: PlyFactory = PlyFactory()

    override fun builder(): Builder = RealBuilder(plyFactory)
    override fun puzzleInteractor(): PuzzleInteractor = RealPuzzleInteractor()
}
