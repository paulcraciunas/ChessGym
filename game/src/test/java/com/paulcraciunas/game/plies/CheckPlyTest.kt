package com.paulcraciunas.game.plies

import com.paulcraciunas.game.Side
import com.paulcraciunas.game.board.Piece
import com.paulcraciunas.game.loc
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CheckPlyTest {
    @Test
    fun `WHEN decorating a standard ply THEN return correct algebraic string`() {
        val ply = StandardPly(Side.WHITE, Piece.Knight, "b1".loc(), "d2".loc())

        assertEquals("Nd2+", CheckPly(ply).algebraic())
    }

    @Test
    fun `WHEN decorating a castle ply THEN return correct algebraic string`() {
        val ply = CastlePly(Side.WHITE, CastlePly.Type.KingSide)

        assertEquals("O-O+", CheckPly(ply).algebraic())
    }

    @Test
    fun `WHEN decorating a promotion ply THEN return correct algebraic string`() {
        val ply = PromotionPly(Side.WHITE, "e7".loc(), "e8".loc())
        ply.accept(Piece.Queen)

        assertEquals("e8=Q+", CheckPly(ply).algebraic())
    }
}
