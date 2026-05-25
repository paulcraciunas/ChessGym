package com.paulcraciunas.game.engine.api

import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank

object UciMoveParser {

    fun parse(moveString: String): EngineMove? {
        if (moveString.length !in 4..5) return null
        val from = parseLocus(moveString[0], moveString[1]) ?: return null
        val to = parseLocus(moveString[2], moveString[3]) ?: return null
        val promotion = if (moveString.length == 5) {
            parsePromotionPiece(moveString[4])
        } else null
        return EngineMove(from = from, to = to, promotion = promotion)
    }

    fun format(move: EngineMove): String = buildString {
        append(move.from.file.name)
        append(move.from.rank.ordinal + 1)
        append(move.to.file.name)
        append(move.to.rank.ordinal + 1)
        move.promotion?.let { append(formatPromotionPiece(it)) }
    }

    private fun parseLocus(fileChar: Char, rankChar: Char): Locus? {
        val file = File.entries.getOrNull(fileChar - 'a') ?: return null
        val rank = Rank.entries.getOrNull(rankChar - '1') ?: return null
        return Locus.from(file, rank)
    }

    private fun parsePromotionPiece(char: Char): Piece? = when (char) {
        'q' -> Piece.Queen
        'r' -> Piece.Rook
        'b' -> Piece.Bishop
        'n' -> Piece.Knight
        else -> null
    }

    private fun formatPromotionPiece(piece: Piece): Char = when (piece) {
        Piece.Queen -> 'q'
        Piece.Rook -> 'r'
        Piece.Bishop -> 'b'
        Piece.Knight -> 'n'
        else -> 'q'
    }
}
