package com.paulcraciunas.game.logic

import com.paulcraciunas.game.logic.api.CastleType
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.state.CheckCount
import com.paulcraciunas.game.logic.impl.MutableGameInfo
import com.paulcraciunas.game.logic.impl.plies.StandardPly
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class MutableGameInfoTest {
    private lateinit var underTest: MutableGameInfo

    @Test
    fun `WHEN move is pawn move THEN next resets the ply clock`() {
        underTest = MutableGameInfo()
        val ply = StandardPly(Side.WHITE, Piece.Pawn, Locus.e2, Locus.e4)
        val expected = MutableGameInfo(Side.BLACK, ply)

        underTest.update(ply, CheckCount.None)

        assertEquals(expected, underTest)
    }

    @Test
    fun `WHEN move is a capture THEN next resets the ply clock`() {
        underTest = MutableGameInfo()
        val ply =
            StandardPly(Side.WHITE, Piece.Knight, Locus.g1, Locus.f3, captured = Piece.Bishop)
        val expected = MutableGameInfo(Side.BLACK, ply)

        underTest.update(ply, CheckCount.None)

        assertEquals(expected, underTest)
    }

    @Test
    fun `WHEN move is neither a pawn move nor a capture THEN next increments the ply clock`() {
        underTest = MutableGameInfo()
        val ply = StandardPly(Side.WHITE, Piece.Knight, Locus.g1, Locus.f3)
        val expected = MutableGameInfo(Side.BLACK, ply, plieClock = 1)

        underTest.update(ply, CheckCount.None)

        assertEquals(expected, underTest)
    }

    @Test
    fun `WHEN move from BLACK THEN next increments move count`() {
        underTest = MutableGameInfo(turn = Side.BLACK)
        val ply = StandardPly(Side.BLACK, Piece.Knight, Locus.g8, Locus.f6)
        val expected = MutableGameInfo(Side.WHITE, ply, plieClock = 1, moveIndex = 2)

        underTest.update(ply, CheckCount.None)

        assertEquals(expected, underTest)
    }

    @Test
    fun `WHEN white king moved THEN white can no longer castle`() {
        underTest = MutableGameInfo()
        val ply = StandardPly(Side.WHITE, Piece.King, Locus.e1, Locus.e2)
        val expected = MutableGameInfo(Side.BLACK, ply, plieClock = 1, whiteCastling = setOf())

        underTest.update(ply, CheckCount.None)

        assertEquals(expected, underTest)
    }

    @Test
    fun `WHEN white a1 rook moved THEN white can no longer castle queenSide`() {
        underTest = MutableGameInfo()
        val ply = StandardPly(Side.WHITE, Piece.Rook, Locus.a1, Locus.a2)
        val expected = MutableGameInfo(
            Side.BLACK, ply, plieClock = 1, whiteCastling = setOf(CastleType.KingSide)
        )

        underTest.update(ply, CheckCount.None)

        assertEquals(expected, underTest)
    }

    @Test
    fun `WHEN white h1 rook moved THEN white can no longer castle kingSide`() {
        underTest = MutableGameInfo()
        val ply = StandardPly(Side.WHITE, Piece.Rook, Locus.h1, Locus.h2)
        val expected = MutableGameInfo(
            Side.BLACK, ply, plieClock = 1, whiteCastling = setOf(CastleType.QueenSide)
        )

        underTest.update(ply, CheckCount.None)

        assertEquals(expected, underTest)
    }

    @Test
    fun `WHEN black king moved THEN black can no longer castle`() {
        underTest = MutableGameInfo(turn = Side.BLACK)
        val ply = StandardPly(Side.BLACK, Piece.King, Locus.e8, Locus.e7)
        val expected =
            MutableGameInfo(Side.WHITE, ply, plieClock = 1, moveIndex = 2, blackCastling = setOf())

        underTest.update(ply, CheckCount.None)

        assertEquals(expected, underTest)
    }

    @Test
    fun `WHEN black a8 rook moved THEN black can no longer castle queenSide`() {
        underTest = MutableGameInfo(turn = Side.BLACK)
        val ply = StandardPly(Side.BLACK, Piece.Rook, Locus.a8, Locus.a7)
        val expected =
            MutableGameInfo(
                Side.WHITE,
                ply,
                plieClock = 1,
                moveIndex = 2,
                blackCastling = setOf(CastleType.KingSide)
            )

        underTest.update(ply, CheckCount.None)

        assertEquals(expected, underTest)
    }

    @Test
    fun `WHEN black h8 rook moved THEN black can no longer castle kingSide`() {
        underTest = MutableGameInfo(turn = Side.BLACK)
        val ply = StandardPly(Side.BLACK, Piece.Rook, Locus.h8, Locus.h7)
        val expected =
            MutableGameInfo(
                Side.WHITE,
                ply,
                plieClock = 1,
                moveIndex = 2,
                blackCastling = setOf(CastleType.QueenSide)
            )

        underTest.update(ply, CheckCount.None)

        assertEquals(expected, underTest)
    }

    @Test
    fun `WHEN white a1 rook is captured THEN white can no longer castle queenSide`() {
        underTest = MutableGameInfo(turn = Side.BLACK)
        val ply =
            StandardPly(Side.BLACK, Piece.Queen, Locus.e5, Locus.a1, captured = Piece.Rook)
        val expected =
            MutableGameInfo(
                Side.WHITE,
                ply,
                moveIndex = 2,
                whiteCastling = setOf(CastleType.KingSide)
            )

        underTest.update(ply, CheckCount.None)

        assertEquals(expected, underTest)
    }

    @Test
    fun `WHEN black h8 rook is captured THEN black can no longer castle kingSide`() {
        underTest = MutableGameInfo()
        val ply =
            StandardPly(Side.WHITE, Piece.Queen, Locus.e5, Locus.h8, captured = Piece.Rook)
        val expected =
            MutableGameInfo(Side.BLACK, ply, blackCastling = setOf(CastleType.QueenSide))

        underTest.update(ply, CheckCount.None)

        assertEquals(expected, underTest)
    }
}
