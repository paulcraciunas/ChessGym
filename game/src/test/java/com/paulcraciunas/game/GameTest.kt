package com.paulcraciunas.game

import com.paulcraciunas.game.board.Board
import com.paulcraciunas.game.board.BoardFactory
import com.paulcraciunas.game.board.Locus
import com.paulcraciunas.game.board.Piece
import com.paulcraciunas.game.plies.ExpectedPly
import com.paulcraciunas.game.plies.StandardPly
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class GameTest {
    private val on = BoardFactory.defaultBoard()

    private lateinit var underTest: Game

    @Test
    fun `GIVEN default starting board WHEN getting plies for white THEN return all correct plies`() {
        underTest = Game(board = on)
        val expected = mutableListOf<StandardPly>()
            .apply {
                add(StandardPly(Side.WHITE, Piece.Pawn, "e2".loc(), "e3".loc()))
                add(StandardPly(Side.WHITE, Piece.Pawn, "e2".loc(), "e4".loc()))
            }.map { ExpectedPly(it) }

        val actual = underTest.playablePlies("e2".loc())
            .map { ExpectedPly(it) }

        assertEquals(Side.WHITE, underTest.turn())
        assertNull(underTest.isOver())
        assertEquals(expected.size, actual.size)
        assertTrue(expected.containsAll(actual))
        assertTrue(actual.containsAll(expected))
    }

    @Test
    fun `GIVEN default starting board WHEN getting plies for black THEN return all correct plies`() {
        underTest = Game(board = on)
        underTest.play(
            underTest.playablePlies("e2".loc()).first { it.to == "e4".loc() }
        )
        val expected = mutableListOf<StandardPly>()
            .apply {
                add(StandardPly(Side.BLACK, Piece.Pawn, "e7".loc(), "e6".loc()))
                add(StandardPly(Side.BLACK, Piece.Pawn, "e7".loc(), "e5".loc()))
            }.map { ExpectedPly(it) }

        val actual = underTest.playablePlies("e7".loc())
            .map { ExpectedPly(it) }

        assertEquals(Side.BLACK, underTest.turn())
        assertNull(underTest.isOver())
        assertEquals(expected.size, actual.size)
        assertTrue(expected.containsAll(actual))
        assertTrue(actual.containsAll(expected))
    }

    @Test
    fun `WHEN playing a Fools Mate game THEN game ends in checkmate`() {
        underTest = Game(board = on)
        underTest.play("f2", "f3")
            .play("e7", "e6")
            .play("g2", "g4")
            .play("d8", "h4")

        Locus.all { // No move from anywhere
            assertTrue(underTest.playablePlies(it).isEmpty())
        }
        assertEquals(Side.WHITE, underTest.turn())
        assertEquals(Result.CheckMate, underTest.isOver())
    }

    @Test
    fun `WHEN playing a Scholars Mate game THEN game ends in checkmate`() {
        underTest = Game(board = on)
        underTest.play("e2", "e4")
            .play("e7", "e5")
            .play("d1", "h5")
            .play("b8", "c6")
            .play("f1", "c4")
            .play("g8", "f6")
            .play("h5", "f7")

        Locus.all { // No move from anywhere
            assertTrue(underTest.playablePlies(it).isEmpty())
        }
        assertEquals(Side.BLACK, underTest.turn())
        assertEquals(Result.CheckMate, underTest.isOver())
    }

    @Test
    fun `WHEN repeating position 3 times THEN game ends in draw`() {
        underTest = Game(board = on)
        underTest // Start with an inconsequential move
            .play("e2", "e4").play("e7", "e5")
            // Move the king forward and back
            .play("e1", "e2").play("e8", "e7")
            .play("e2", "e1").play("e7", "e8")
            // Now move the knights forward and back
            .play("g1", "f3").play("b8", "c6")
            .play("f3", "g1").play("c6", "b8")

        Locus.all { // No move from anywhere
            assertTrue(underTest.playablePlies(it).isEmpty())
        }
        assertEquals(Side.WHITE, underTest.turn())
        assertEquals(Result.DrawByRepetition, underTest.isOver())
    }

    @Test
    fun `WHEN loading a stalemate position for black THEN game is over`() {
        val board = Board().apply {
            add(Piece.King, Side.WHITE, "a1".loc())
            add(Piece.Queen, Side.WHITE, "g6".loc())
            add(Piece.King, Side.BLACK, "h8".loc())
        }
        underTest = Game(board = board, turn = Side.BLACK)

        Locus.all { // No move from anywhere
            assertTrue(underTest.playablePlies(it).isEmpty())
        }
        assertEquals(Side.BLACK, underTest.turn())
        assertEquals(Result.StaleMate, underTest.isOver())
    }

    @Test
    fun `WHEN loading a stalemate position for white THEN game is over`() {
        val board = Board().apply {
            add(Piece.King, Side.WHITE, "f1".loc())
            add(Piece.Pawn, Side.BLACK, "f2".loc())
            add(Piece.King, Side.BLACK, "f3".loc())
        }
        underTest = Game(board = board)

        Locus.all { // No move from anywhere
            assertTrue(underTest.playablePlies(it).isEmpty())
        }
        assertEquals(Side.WHITE, underTest.turn())
        assertEquals(Result.StaleMate, underTest.isOver())
    }

    @Test
    fun `WHEN loading a position with just 2 kings THEN draw by insufficient material`() {
        verifyDrawByInsufficientMaterial(
            Triple(Piece.King, Side.WHITE, "f1"),
            Triple(Piece.King, Side.BLACK, "f3"),
        )
    }

    @Test
    fun `WHEN loading a position with an extra Bishop THEN draw by insufficient material`() {
        verifyDrawByInsufficientMaterial(
            Triple(Piece.King, Side.WHITE, "f1"),
            Triple(Piece.Bishop, Side.WHITE, "a7"),
            Triple(Piece.King, Side.BLACK, "f3"),
        )
    }

    @Test
    fun `WHEN loading a position with an extra Knight THEN draw by insufficient material`() {
        verifyDrawByInsufficientMaterial(
            Triple(Piece.King, Side.WHITE, "f1"),
            Triple(Piece.Knight, Side.WHITE, "a7"),
            Triple(Piece.King, Side.BLACK, "f3"),
        )
    }

    @Test
    fun `WHEN loading a position with a same colour Bishop each THEN draw by insufficient material`() {
        verifyDrawByInsufficientMaterial(
            Triple(Piece.King, Side.WHITE, "f1"),
            Triple(Piece.Bishop, Side.WHITE, "a7"),
            Triple(Piece.Bishop, Side.BLACK, "h4"),
            Triple(Piece.King, Side.BLACK, "f3"),
        )
    }

    private fun verifyDrawByInsufficientMaterial(vararg pieces: Triple<Piece, Side, String>) {
        val board = Board()
        pieces.forEach {
            board.add(it.first, it.second, it.third.loc())
        }
        underTest = Game(board = board)

        Locus.all { // No move from anywhere
            assertTrue(underTest.playablePlies(it).isEmpty())
        }
        assertEquals(Side.WHITE, underTest.turn())
        assertEquals(
            Result.DrawByInsufficientMaterial,
            underTest.isOver()
        )
    }

    private fun Game.play(from: String, to: String): Game = apply {
        play(
            playablePlies(from.loc()).firstOrNull { it.to == to.loc() }
                ?: throw AssertionError("Wrong move")
        )
    }
}
