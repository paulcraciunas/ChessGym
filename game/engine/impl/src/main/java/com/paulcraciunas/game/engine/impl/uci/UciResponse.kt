package com.paulcraciunas.game.engine.impl.uci

import com.paulcraciunas.game.engine.api.EngineMove
import com.paulcraciunas.game.engine.api.UciMoveParser

private const val BEST_MOVE_TOKEN = "bestmove"
private const val UCI_OK_TOKEN = "uciok"
private const val READY_OK_TOKEN = "readyok"

internal sealed class UciResponse {
    object Done : UciResponse()
    object Initialized : UciResponse()
    object Ready : UciResponse()
    class BestMove(val engineMove: EngineMove) : UciResponse()
}

internal sealed class ResponseValidator {
    abstract fun validate(from: String?): Boolean

    object Done : ResponseValidator() {
        override fun validate(from: String?): Boolean = true
    }

    object Initialized : ResponseValidator() {
        override fun validate(from: String?): Boolean = from?.trim() == UCI_OK_TOKEN
    }

    object Ready : ResponseValidator() {
        override fun validate(from: String?): Boolean = from?.trim() == READY_OK_TOKEN
    }

    object BestMove : ResponseValidator() {
        override fun validate(from: String?): Boolean = from?.startsWith(BEST_MOVE_TOKEN) == true
    }
}

internal sealed class ResponseFactory(val validator: ResponseValidator) {
    abstract fun construct(from: String? = null): UciResponse?

    object DoneFactory : ResponseFactory(ResponseValidator.Done) {
        override fun construct(from: String?): UciResponse? = if (validator.validate(from)) UciResponse.Done else null
    }

    object InitializedFactory : ResponseFactory(ResponseValidator.Initialized) {
        override fun construct(from: String?): UciResponse? = if (validator.validate(from)) UciResponse.Initialized else null
    }

    object ReadyFactory : ResponseFactory(ResponseValidator.Ready) {
        override fun construct(from: String?): UciResponse? = if (validator.validate(from)) UciResponse.Ready else null
    }

    object BestMoveFactory : ResponseFactory(ResponseValidator.BestMove) {
        override fun construct(from: String?): UciResponse? =
            if (validator.validate(from)) UciResponse.BestMove(engineMove = parseBestMove(from!!))
            else null

        private fun parseBestMove(from: String): EngineMove {
            val parts = from.trim().split(" ")
            require(parts.size >= 2 && parts[0] == BEST_MOVE_TOKEN) {
                "Invalid bestmove line: $from"
            }
            return UciMoveParser.parse(parts[1])
                ?: throw IllegalArgumentException("Invalid UCI move: ${parts[1]}")
        }
    }
}
