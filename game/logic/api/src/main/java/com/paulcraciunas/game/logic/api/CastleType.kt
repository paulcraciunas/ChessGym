package com.paulcraciunas.game.logic.api

import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Rank

enum class CastleType(val passFile: File, val endFile: File, val rookFile: File, val extra: File?) {
    KingSide(passFile = File.f, endFile = File.g, rookFile = File.h, extra = null),
    QueenSide(passFile = File.d, endFile = File.c, rookFile = File.a, extra = File.b);

    fun from(turn: Side): Locus = Locus(File.e, rank(turn))
    fun pass(turn: Side): Locus = Locus(passFile, rank(turn))
    fun extraPass(turn: Side): Locus? = extra?.let { Locus(it, rank(turn)) }
    fun end(turn: Side): Locus = Locus(endFile, rank(turn))
    fun rook(turn: Side): Locus = Locus(rookFile, rank(turn))

    private fun rank(turn: Side): Rank = if (turn == Side.WHITE) Rank.`1` else Rank.`8`
}
