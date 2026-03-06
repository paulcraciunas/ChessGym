package com.paulcraciunas.game.engine.impl.uci

import com.paulcraciunas.game.engine.api.EngineMove
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank

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
            return parseUciMove(parts[1])
        }

        private fun parseUciMove(moveString: String): EngineMove {
            require(moveString.length in 4..5) {
                "Invalid UCI move format: $moveString"
            }
            val from = parseLocus(moveString[0], moveString[1])
            val to = parseLocus(moveString[2], moveString[3])
            val promotion = if (moveString.length == 5) {
                parsePromotionPiece(moveString[4])
            } else null
            return EngineMove(from = from, to = to, promotion = promotion)
        }

        private fun parseLocus(fileChar: Char, rankChar: Char): Locus {
            val file = File.entries.getOrNull(fileChar - 'a') ?: throw IllegalArgumentException("Invalid file: $fileChar")
            val rank = Rank.entries.getOrNull(rankChar - '1') ?: throw IllegalArgumentException("Invalid rank: $rankChar")
            return Locus(file, rank)
        }

        private fun parsePromotionPiece(char: Char): Piece = when (char) {
            'q' -> Piece.Queen
            'r' -> Piece.Rook
            'b' -> Piece.Bishop
            'n' -> Piece.Knight
            else -> throw IllegalArgumentException("Invalid promotion piece: $char")
        }
    }
}
