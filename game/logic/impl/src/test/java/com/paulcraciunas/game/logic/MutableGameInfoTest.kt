package com.paulcraciunas.game.logic

import com.paulcraciunas.game.logic.api.CastleType
import com.paulcraciunas.game.logic.api.Side
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
        val ply = StandardPly(Side.WHITE, Piece.Pawn, "e2".loc(), "e4".loc())
        val expected = MutableGameInfo(Side.BLACK, ply)

        underTest.update(ply, CheckCount.None)

        assertEquals(expected, underTest)
    }

    @Test
    fun `WHEN move is a capture THEN next resets the ply clock`() {
        underTest = MutableGameInfo()
        val ply =
            StandardPly(Side.WHITE, Piece.Knight, "g1".loc(), "f3".loc(), captured = Piece.Bishop)
        val expected = MutableGameInfo(Side.BLACK, ply)

        underTest.update(ply, CheckCount.None)

        assertEquals(expected, underTest)
    }

    @Test
    fun `WHEN move is neither a pawn move nor a capture THEN next increments the ply clock`() {
        underTest = MutableGameInfo()
        val ply = StandardPly(Side.WHITE, Piece.Knight, "g1".loc(), "f3".loc())
        val expected = MutableGameInfo(Side.BLACK, ply, plieClock = 1)

        underTest.update(ply, CheckCount.None)

        assertEquals(expected, underTest)
    }

    @Test
    fun `WHEN move from BLACK THEN next increments move count`() {
        underTest = MutableGameInfo(turn = Side.BLACK)
        val ply = StandardPly(Side.BLACK, Piece.Knight, "g8".loc(), "f6".loc())
        val expected = MutableGameInfo(Side.WHITE, ply, plieClock = 1, moveIndex = 2)

        underTest.update(ply, CheckCount.None)

        assertEquals(expected, underTest)
    }

    @Test
    fun `WHEN white king moved THEN white can no longer castle`() {
        underTest = MutableGameInfo()
        val ply = StandardPly(Side.WHITE, Piece.King, "e1".loc(), "e2".loc())
        val expected = MutableGameInfo(Side.BLACK, ply, plieClock = 1, whiteCastling = setOf())

        underTest.update(ply, CheckCount.None)

        assertEquals(expected, underTest)
    }

    @Test
    fun `WHEN white a1 rook moved THEN white can no longer castle queenSide`() {
        underTest = MutableGameInfo()
        val ply = StandardPly(Side.WHITE, Piece.Rook, "a1".loc(), "a2".loc())
        val expected = MutableGameInfo(
            Side.BLACK, ply, plieClock = 1, whiteCastling = setOf(CastleType.KingSide)
        )

        underTest.update(ply, CheckCount.None)

        assertEquals(expected, underTest)
    }

    @Test
    fun `WHEN white h1 rook moved THEN white can no longer castle kingSide`() {
        underTest = MutableGameInfo()
        val ply = StandardPly(Side.WHITE, Piece.Rook, "h1".loc(), "h2".loc())
        val expected = MutableGameInfo(
            Side.BLACK, ply, plieClock = 1, whiteCastling = setOf(CastleType.QueenSide)
        )

        underTest.update(ply, CheckCount.None)

        assertEquals(expected, underTest)
    }

    @Test
    fun `WHEN black king moved THEN black can no longer castle`() {
        underTest = MutableGameInfo(turn = Side.BLACK)
        val ply = StandardPly(Side.BLACK, Piece.King, "e8".loc(), "e7".loc())
        val expected =
            MutableGameInfo(Side.WHITE, ply, plieClock = 1, moveIndex = 2, blackCastling = setOf())

        underTest.update(ply, CheckCount.None)

        assertEquals(expected, underTest)
    }

    @Test
    fun `WHEN black a8 rook moved THEN black can no longer castle queenSide`() {
        underTest = MutableGameInfo(turn = Side.BLACK)
        val ply = StandardPly(Side.BLACK, Piece.Rook, "a8".loc(), "a7".loc())
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
        val ply = StandardPly(Side.BLACK, Piece.Rook, "h8".loc(), "h7".loc())
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
            StandardPly(Side.BLACK, Piece.Queen, "e5".loc(), "a1".loc(), captured = Piece.Rook)
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
            StandardPly(Side.WHITE, Piece.Queen, "e5".loc(), "h8".loc(), captured = Piece.Rook)
        val expected =
            MutableGameInfo(Side.BLACK, ply, blackCastling = setOf(CastleType.QueenSide))

        underTest.update(ply, CheckCount.None)

        assertEquals(expected, underTest)
    }
}
