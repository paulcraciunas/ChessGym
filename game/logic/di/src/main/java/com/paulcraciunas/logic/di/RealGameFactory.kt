package com.paulcraciunas.logic.di

import com.paulcraciunas.game.logic.impl.plies.PlyFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealGameFactory @Inject constructor(
    private val plyFactory: PlyFactory
) : GameFactory {
    override fun builder(): Builder = RealBuilder(plyFactory)
}
