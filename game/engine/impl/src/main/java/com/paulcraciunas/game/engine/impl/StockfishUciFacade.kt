package com.paulcraciunas.game.engine.impl

import com.paulcraciunas.game.engine.impl.uci.UciCommand
import com.paulcraciunas.game.engine.impl.uci.UciResponse
import javax.inject.Inject

internal class StockfishUciFacade @Inject constructor(
    val bridge: StockfishBridge,
) : UciFacade {

    override fun startEngine() {
        bridge.nativeStartEngine()
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : UciResponse> execute(uciCommand: UciCommand): T {
        bridge.nativeSendCommand(cmd = uciCommand.protocol())
        return buildResponse(uciCommand) as T
    }

    private fun buildResponse(uciCommand: UciCommand): UciResponse {
        val factory = uciCommand.responseFactory()
        var response = factory.construct()
        while (response == null) {
            val line = bridge.nativeReadOutput()
            response = factory.construct(line)
        }
        return response
    }
}
