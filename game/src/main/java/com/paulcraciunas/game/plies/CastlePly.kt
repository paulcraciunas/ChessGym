package com.paulcraciunas.game.plies

import com.paulcraciunas.game.Side
import com.paulcraciunas.game.board.Board
import com.paulcraciunas.game.board.File
import com.paulcraciunas.game.board.Locus
import com.paulcraciunas.game.board.Piece
import com.paulcraciunas.game.board.Rank

class CastlePly(override val turn: Side, val type: Type) : Ply {
    override val piece = Piece.King
    override val from = type.from(turn)
    override val to = type.end(turn)

    override fun exec(on: Board) {
        on.move(from = from, to = to, turn = turn)
        on.move(from = type.rook(turn), to = type.pass(turn), turn = turn)
    }

    override fun undo(on: Board) {
        on.move(from = to, to = from, turn = turn)
        on.move(from = type.pass(turn), to = type.rook(turn), turn = turn)
    }

    override fun captured(): Piece? = null
    override fun isPawnMoveOrCapture(): Boolean = false
    override fun algebraic(): String = if (type == Type.KingSide) "O-O" else "O-O-O"

    enum class Type(val passFile: File, val endFile: File, val rookFile: File, val extra: File?) {
        KingSide(passFile = File.f, endFile = File.g, rookFile = File.h, extra = null),
        QueenSide(passFile = File.d, endFile = File.c, rookFile = File.a, extra = File.b);

        fun from(turn: Side): Locus = Locus(File.e, rank(turn))
        fun pass(turn: Side): Locus = Locus(passFile, rank(turn))
        fun extraPass(turn: Side): Locus? = extra?.let { Locus(it, rank(turn)) }
        fun end(turn: Side): Locus = Locus(endFile, rank(turn))
        fun rook(turn: Side): Locus = Locus(rookFile, rank(turn))

        private fun rank(turn: Side): Rank = if (turn == Side.WHITE) Rank.`1` else Rank.`8`
    }
}
