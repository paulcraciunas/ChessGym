package com.paulcraciunas.game.logic.plies

import com.paulcraciunas.game.logic.api.CastleType
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.plies.CastlePly
import com.paulcraciunas.game.logic.impl.plies.CheckPly
import com.paulcraciunas.game.logic.impl.plies.PromotionPly
import com.paulcraciunas.game.logic.impl.plies.StandardPly
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CheckPlyTest {
    @Test
    fun `WHEN decorating a standard ply THEN return correct algebraic string`() {
        val ply = StandardPly(Side.WHITE, Piece.Knight, Locus.b1, Locus.d2)

        assertEquals("Nd2+", CheckPly(ply).algebraic())
    }

    @Test
    fun `WHEN decorating a castle ply THEN return correct algebraic string`() {
        val ply = CastlePly(Side.WHITE, CastleType.KingSide)

        assertEquals("O-O+", CheckPly(ply).algebraic())
    }

    @Test
    fun `WHEN decorating a promotion ply THEN return correct algebraic string`() {
        val ply = PromotionPly(Side.WHITE, Locus.e7, Locus.e8)
        ply.promote(Piece.Queen)

        assertEquals("e8=Q+", CheckPly(ply).algebraic())
    }
}
