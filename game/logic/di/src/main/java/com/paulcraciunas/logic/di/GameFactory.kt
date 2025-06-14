package com.paulcraciunas.logic.di

import com.paulcraciunas.game.logic.impl.plies.PlyFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameFactory @Inject constructor(
    private val plyFactory: PlyFactory
){
    fun builder(): Builder = Builder(plyFactory)
}
