package com.paulcraciunas.game.logic.impl

import com.paulcraciunas.game.logic.api.Builder
import com.paulcraciunas.game.logic.api.GameFactory
import com.paulcraciunas.game.logic.impl.plies.PlyFactory

class RealGameFactory(
    private val plyFactory: PlyFactory
) : GameFactory {
    override fun builder(): Builder = RealBuilder(plyFactory)
}
