package com.paulcraciunas.chessgym.game.io

import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.assertDefaultBoard
import com.paulcraciunas.chessgym.game.board.Piece
import com.paulcraciunas.chessgym.game.loc
import com.paulcraciunas.chessgym.game.plies.ExpectedPly
import com.paulcraciunas.chessgym.game.plies.StandardPly
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class FenSerializerTest {
    private val underTest = FenSerializer

    @Test
    fun `WHEN fen parts are missing THEN throw`() {
        val fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR KQkq - 0 1"

        assertThrows<SerializeException>("Expected 6 FEN parts, found 5") {
            underTest.from(fen)
        }
    }

    @Test
    fun `WHEN we have too many parts THEN throw`() {
        val fen = "r/p/8/8/8/8/8/8 w KQkq - 0 1 1"

        assertThrows<SerializeException>("Expected 6 FEN parts, found 7") {
            underTest.from(fen)
        }
    }

    @Test
    fun `WHEN rows are missing THEN throw`() {
        val fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP w KQkq - 0 1"

        assertThrows<SerializeException>("Expected 6 FEN parts, found 5") {
            underTest.from(fen)
        }
    }

    @ParameterizedTest(name = "{0} is invalid because of row {1}")
    @MethodSource("invalidRows")
    fun `WHEN row is malformed THEN throw`(
        fen: String,
        row: String,
    ) {
        assertThrows<SerializeException>("Illegal row found: $row") {
            underTest.from(fen)
        }
    }

    @Test
    fun `WHEN side is invalid THEN throw`() {
        val fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR g KQkq - 0 1"

        assertThrows<SerializeException>("Expecting 'w'/'b' as side, found g") {
            underTest.from(fen)
        }
    }

    @Test
    fun `WHEN side is missing THEN throw`() {
        val fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR  KQkq - 0 1"

        assertThrows<SerializeException>("Missing 'w'/'b' as side") {
            underTest.from(fen)
        }
    }

    @Test
    fun `WHEN plie clock is invalid THEN throw`() {
        val fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - a 1"

        assertThrows<SerializeException>("Expecting number, found: a") {
            underTest.from(fen)
        }
    }

    @Test
    fun `WHEN move index is invalid THEN throw`() {
        val fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 K"

        assertThrows<SerializeException>("Expecting number, found: K") {
            underTest.from(fen)
        }
    }

    @Test
    fun `WHEN castling is invalid THEN throw`() {
        val fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQbq - 0 K"

        assertThrows<SerializeException>("Unexpected castling symbol found: b") {
            underTest.from(fen)
        }
    }

    @Test
    fun `WHEN en-passent is invalid THEN throw`() {
        val fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq e2 0 K"

        assertThrows<SerializeException>("Invalid en-passent location: e2") {
            underTest.from(fen)
        }
    }

    @Test
    fun `WHEN loading default starting position THEN board is correct`() {
        val fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

        val game = underTest.from(fen)
        val board = game.currentBoard()

        val expectedPlies = mutableListOf<StandardPly>()
            .apply {
                add(StandardPly(Side.WHITE, Piece.Pawn, "e2".loc(), "e3".loc()))
                add(StandardPly(Side.WHITE, Piece.Pawn, "e2".loc(), "e4".loc()))
            }.map { ExpectedPly(it) }

        val actualPlies = game.playablePlies("e2".loc())
            .map { ExpectedPly(it) }

        assertDefaultBoard(board)
        assertEquals(Side.WHITE, game.turn())
        assertNull(game.ending())
        assertEquals(expectedPlies.size, actualPlies.size)
        assertTrue(expectedPlies.containsAll(actualPlies))
        assertTrue(actualPlies.containsAll(expectedPlies))
    }

    companion object {
        @JvmStatic
        fun invalidRows(): List<Arguments> = mutableListOf<Arguments>(
            // Order is: FEN, malformed row
            Arguments.of("znbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "znbqkbnr"),
            Arguments.of("rnbqkbnrr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "rnbqkbnrr"),
            Arguments.of("rnbqkbn/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "rnbqkbn"),
            Arguments.of("rnbqkbn2/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "rnbqkbn2"),
            Arguments.of("r8/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "r8"),
            Arguments.of("znbqkbnr/9/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "9"),
            Arguments.of("znbqkbnr/9/8/8/8/8/PPPPPPPP/ w KQkq - 0 1", ""),
        )
    }
}
