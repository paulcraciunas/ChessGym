package com.paulcraciunas.chessgym.game

import com.paulcraciunas.chessgym.game.board.Piece
import com.paulcraciunas.chessgym.game.plies.CastlePly
import com.paulcraciunas.chessgym.game.plies.StandardPly
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GameStateTest {
    private lateinit var underTest: GameState

    @Test
    fun `WHEN move is pawn move THEN next resets the ply clock`() {
        underTest = GameState()
        val ply = StandardPly(Side.WHITE, Piece.Pawn, "e2".loc(), "e4".loc())
        val expected = GameState(Side.BLACK, ply)

        assertEquals(expected, underTest.next(ply, CheckCount.None))
    }

    @Test
    fun `WHEN move is a capture THEN next resets the ply clock`() {
        underTest = GameState()
        val ply =
            StandardPly(Side.WHITE, Piece.Knight, "g1".loc(), "f3".loc(), captured = Piece.Bishop)
        val expected = GameState(Side.BLACK, ply)

        assertEquals(expected, underTest.next(ply, CheckCount.None))
    }

    @Test
    fun `WHEN move is neither a pawn move nor a capture THEN next increments the ply clock`() {
        underTest = GameState()
        val ply = StandardPly(Side.WHITE, Piece.Knight, "g1".loc(), "f3".loc())
        val expected = GameState(Side.BLACK, ply, plieClock = 1)

        assertEquals(expected, underTest.next(ply, CheckCount.None))
    }

    @Test
    fun `WHEN move from BLACK THEN next increments move count`() {
        underTest = GameState(turn = Side.BLACK)
        val ply = StandardPly(Side.BLACK, Piece.Knight, "g8".loc(), "f6".loc())
        val expected = GameState(Side.WHITE, ply, plieClock = 1, moveIndex = 2)

        assertEquals(expected, underTest.next(ply, CheckCount.None))
    }

    @Test
    fun `WHEN white king moved THEN white can no longer castle`() {
        underTest = GameState()
        val ply = StandardPly(Side.WHITE, Piece.King, "e1".loc(), "e2".loc())
        val expected = GameState(Side.BLACK, ply, plieClock = 1, whiteCastling = setOf())

        assertEquals(expected, underTest.next(ply, CheckCount.None))
    }

    @Test
    fun `WHEN white a1 rook moved THEN white can no longer castle queenSide`() {
        underTest = GameState()
        val ply = StandardPly(Side.WHITE, Piece.Rook, "a1".loc(), "a2".loc())
        val expected = GameState(
            Side.BLACK, ply, plieClock = 1, whiteCastling = setOf(CastlePly.Type.KingSide)
        )

        assertEquals(expected, underTest.next(ply, CheckCount.None))
    }

    @Test
    fun `WHEN white h1 rook moved THEN white can no longer castle kingSide`() {
        underTest = GameState()
        val ply = StandardPly(Side.WHITE, Piece.Rook, "h1".loc(), "h2".loc())
        val expected = GameState(
            Side.BLACK, ply, plieClock = 1, whiteCastling = setOf(CastlePly.Type.QueenSide)
        )

        assertEquals(expected, underTest.next(ply, CheckCount.None))
    }

    @Test
    fun `WHEN black king moved THEN black can no longer castle`() {
        underTest = GameState(turn = Side.BLACK)
        val ply = StandardPly(Side.BLACK, Piece.King, "e8".loc(), "e7".loc())
        val expected =
            GameState(Side.WHITE, ply, plieClock = 1, moveIndex = 2, blackCastling = setOf())

        assertEquals(expected, underTest.next(ply, CheckCount.None))
    }

    @Test
    fun `WHEN black a8 rook moved THEN black can no longer castle queenSide`() {
        underTest = GameState(turn = Side.BLACK)
        val ply = StandardPly(Side.BLACK, Piece.Rook, "a8".loc(), "a7".loc())
        val expected =
            GameState(
                Side.WHITE,
                ply,
                plieClock = 1,
                moveIndex = 2,
                blackCastling = setOf(CastlePly.Type.KingSide)
            )

        assertEquals(expected, underTest.next(ply, CheckCount.None))
    }

    @Test
    fun `WHEN black h8 rook moved THEN black can no longer castle kingSide`() {
        underTest = GameState(turn = Side.BLACK)
        val ply = StandardPly(Side.BLACK, Piece.Rook, "h8".loc(), "h7".loc())
        val expected =
            GameState(
                Side.WHITE,
                ply,
                plieClock = 1,
                moveIndex = 2,
                blackCastling = setOf(CastlePly.Type.QueenSide)
            )

        assertEquals(expected, underTest.next(ply, CheckCount.None))
    }

    @Test
    fun `WHEN white a1 rook is captured THEN white can no longer castle queenSide`() {
        underTest = GameState(turn = Side.BLACK)
        val ply =
            StandardPly(Side.BLACK, Piece.Queen, "e5".loc(), "a1".loc(), captured = Piece.Rook)
        val expected =
            GameState(
                Side.WHITE,
                ply,
                moveIndex = 2,
                whiteCastling = setOf(CastlePly.Type.KingSide)
            )

        assertEquals(expected, underTest.next(ply, CheckCount.None))
    }

    @Test
    fun `WHEN black h8 rook is captured THEN black can no longer castle kingSide`() {
        underTest = GameState()
        val ply =
            StandardPly(Side.WHITE, Piece.Queen, "e5".loc(), "h8".loc(), captured = Piece.Rook)
        val expected =
            GameState(Side.BLACK, ply, blackCastling = setOf(CastlePly.Type.QueenSide))

        assertEquals(expected, underTest.next(ply, CheckCount.None))
    }
}
