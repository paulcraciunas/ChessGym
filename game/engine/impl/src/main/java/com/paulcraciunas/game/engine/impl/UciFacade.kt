package com.paulcraciunas.game.engine.impl

import com.paulcraciunas.game.engine.impl.uci.UciCommand
import com.paulcraciunas.game.engine.impl.uci.UciResponse

internal interface UciFacade {
    fun startEngine()
    suspend fun <T: UciResponse> execute(uciCommand: UciCommand): T
}
