package com.paulcraciunas.game.engine.impl

import com.paulcraciunas.game.engine.impl.uci.UciCommand
import com.paulcraciunas.game.engine.impl.uci.UciResponse
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import javax.inject.Inject

internal class StockfishUciFacade @Inject constructor(
    val bridge: StockfishBridge,
) : UciFacade {

    override fun startEngine() {
        try {
            bridge.nativeStartEngine()
        } catch (e: Exception) {
            Timber.w(e, "Failed to start Stockfish engine")
            throw e
        }
    }

    override fun shutdownEngine() {
        try {
            bridge.nativeShutdownEngine()
        } catch (e: Exception) {
            Timber.w(e, "Failed to shutdown Stockfish engine")
            throw e
        }
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : UciResponse> execute(uciCommand: UciCommand): T {
        try {
            bridge.nativeSendCommand(cmd = uciCommand.protocol())
            return withTimeout(RESPONSE_TIMEOUT_MS) {
                buildResponse(uciCommand) as T
            }
        } catch (e: Exception) {
            Timber.w(e, "UCI command failed: %s", uciCommand.protocol())
            throw e
        }
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

    companion object {
        internal const val RESPONSE_TIMEOUT_MS = 30_000L
    }
}
