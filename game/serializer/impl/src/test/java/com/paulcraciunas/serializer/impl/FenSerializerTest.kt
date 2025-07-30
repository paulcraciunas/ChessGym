package com.paulcraciunas.serializer.impl

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.assertDefaultBoard
import com.paulcraciunas.game.logic.impl.MutableGame
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.game.logic.impl.plies.PlyFactory
import com.paulcraciunas.game.logic.impl.plies.StandardPly
import com.paulcraciunas.game.logic.loc
import com.paulcraciunas.game.logic.plies.ExpectedPly
import com.paulcraciunas.serializer.api.SerializeException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class FenSerializerTest {
    private val underTest = FenSerializer(RealGameFactory(PlyFactory()))

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

        val game = underTest.from(fen).apply { start() }
        val board = game.board

        val expectedPlies = mutableListOf<StandardPly>()
            .apply {
                add(StandardPly(Side.WHITE, Piece.Pawn, "e2".loc(), "e3".loc()))
                add(StandardPly(Side.WHITE, Piece.Pawn, "e2".loc(), "e4".loc()))
            }.map { ExpectedPly(it) }

        val actualPlies = game.info.plies("e2".loc())
            .map { ExpectedPly(it) }

        assertDefaultBoard(board)
        assertEquals(Side.WHITE, game.info.turn)
        assertEquals(Game.GameState.InProgress, game.state)
        assertEquals(expectedPlies.size, actualPlies.size)
        assertTrue(expectedPlies.containsAll(actualPlies))
        assertTrue(actualPlies.containsAll(expectedPlies))
    }

    @Test
    fun `WHEN loading position without castling THEN game state is correct`() {
        val fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 1"

        val game = underTest.from(fen)

        assertDefaultBoard(game.board)
        assertTrue(game.info.whiteCastling.isEmpty())
        assertTrue(game.info.blackCastling.isEmpty())
    }

    @Test
    fun `WHEN serializing position without castling THEN fen string is correct`() {
        val fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 1"

        assertEquals(fen, underTest.of(underTest.from(fen)))
    }

    @Test
    fun `WHEN serializing default starting position THEN fen string is correct`() {
        val fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

        assertEquals(fen, underTest.of(MutableGame()))
    }

    @Test
    fun `WHEN serializing after pawn jump THEN fen string contains en-passent`() {
        val fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"
        val game = MutableGame().apply { start() }
        game.play(game.info.plies("e2".loc()).first { it.to == "e4".loc() })

        assertEquals(fen, underTest.of(game))
    }

    @ParameterizedTest
    @MethodSource("validPositions")
    fun `WHEN deserializing valid positions THEN fen strings are accepted`(fen: String) {
        assertDoesNotThrow {
            val game = underTest.from(fen)
            assertEquals(fen, underTest.of(game))
        }
    }

    companion object {
        @JvmStatic
        fun invalidRows(): List<Arguments> = listOf<Arguments>(
            // Order is: FEN, malformed row
            Arguments.of("znbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "znbqkbnr"),
            Arguments.of("rnbqkbnrr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "rnbqkbnrr"),
            Arguments.of("rnbqkbn/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "rnbqkbn"),
            Arguments.of("rnbqkbn2/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "rnbqkbn2"),
            Arguments.of("r8/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "r8"),
            Arguments.of("znbqkbnr/9/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "9"),
            Arguments.of("znbqkbnr/9/8/8/8/8/PPPPPPPP/ w KQkq - 0 1", ""),
        )

        @JvmStatic
        fun validPositions(): List<Arguments> = listOf<Arguments>(
            // These are the ending positions from the 1987 World Chess Championship
            Arguments.of("r2qkb1r/ppp2ppp/3p4/4p3/4n3/1PP2N2/PB1P1PPP/RN1QR1K1 w kq - 0 12"),
            Arguments.of("r3kb1r/pp1npppp/2p5/8/1q6/2N1P3/PPPQBPPP/R3K2R w KQkq - 0 15"),
            Arguments.of("rnbq1rk1/pp3ppp/4b3/1B1pN3/3Pp3/2P5/PP3PPP/R1BQ1RK1 w - - 0 13"),
            Arguments.of("r1bq1rk1/ppp2ppp/2n1p3/8/1b2P3/5N2/PPPB1PPP/RN1Q1RK1 w - - 0 11"),
            Arguments.of("r1bqk2r/ppp2ppp/2nbp3/8/1PP2B2/2N2N2/P2P1PPP/R2Q1RK1 w kq - 0 10"),
            Arguments.of("r1bq1rk1/pp2bppp/3p4/2p5/2PnP3/2N1B3/PPQ2PPP/R3K2R w KQ - 0 12"),
            Arguments.of("rnbq1rk1/pp3ppp/4p3/8/1b1P4/2N2N2/PP2BPPP/R1BQ1RK1 w - - 0 9"),
            Arguments.of("r1bqk2r/ppp2ppp/3bpn2/8/2B5/2N5/PPP2PPP/R1BQK2R w KQkq - 0 9"),
            Arguments.of("rnbq1rk1/ppp2ppp/4pn2/8/2P1P3/2N5/PP2BPPP/R1BQ1RK1 w - - 0 9"),
            Arguments.of("r1bq1rk1/ppp2ppp/3bpn2/4P3/2B5/2N5/PPP2PPP/R1BQ1RK1 w - - 0 11"),
            Arguments.of("r1bq1rk1/ppp2ppp/4p3/4P3/2n1n3/2N5/PP3PPP/R1BQR1K1 w - - 0 12"),
            Arguments.of("r1bq1rk1/ppp2ppp/4p3/4P3/2n1n3/2N5/PP3PPP/R1BQR1K1 w - - 0 12"),
            Arguments.of("rnbq1rk1/ppp2ppp/4p3/8/2BPn3/2N5/PP3PPP/R1BQ1RK1 w - - 0 9"),
            Arguments.of("rnbq1rk1/pp3ppp/4p3/8/2BpP3/2N5/PP3PPP/R1BQ1RK1 w - - 0 11"),
            Arguments.of("r1bq1rk1/ppp2ppp/2n1p3/8/2B1P3/2N2N2/PP3PPP/R1BQ1RK1 w - - 0 12"),
            Arguments.of("r1bq1rk1/ppp2ppp/2n1p3/8/2B1P3/2N2N2/PP3PPP/R1BQ1RK1 w - - 0 12"),
            Arguments.of("r1bq1rk1/ppp2ppp/2n1p3/8/2B1P3/2N2N2/PP3PPP/R1BQ1RK1 w - - 0 12"),
            Arguments.of("r1bq1rk1/ppp2ppp/2n1p3/8/2B1P3/2N2N2/PP3PPP/R1BQ1RK1 w - - 0 12"),
            Arguments.of("rnbq1rk1/ppp2ppp/4p3/8/2BPn3/2N5/PP3PPP/R1BQ1RK1 w - - 0 9"),
            Arguments.of("r1bq1rk1/ppp2ppp/4p3/8/2BPn3/2N5/PP3PPP/R1BQ1RK1 w - - 0 10"),
            Arguments.of("rnbq1rk1/pp3ppp/4p3/8/2BPn3/2N5/PP3PPP/R1BQ1RK1 w - - 0 11"),
            Arguments.of("rnbq1rk1/ppp2ppp/4p3/8/2BPn3/2N5/PP3PPP/R1BQ1RK1 w - - 0 9"),
            Arguments.of("rnbq1rk1/pp3ppp/4p3/8/2BPn3/2N5/PP3PPP/R1BQ1RK1 w - - 0 11"),
            Arguments.of("r1bq1rk1/pp3ppp/2n1p3/8/2BP4/2N5/PP3PPP/R1BQR1K1 w - - 0 11"),
        )
    }
}