package com.paulcraciunas.game.logic.plies

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.game.logic.api.board.pawnStart
import com.paulcraciunas.game.logic.api.state.CheckCount
import com.paulcraciunas.game.logic.assertHas
import com.paulcraciunas.game.logic.assertNoMoves
import com.paulcraciunas.game.logic.assertNoMovesOf
import com.paulcraciunas.game.logic.impl.MutableGameInfo
import com.paulcraciunas.game.logic.impl.board.Board
import com.paulcraciunas.game.logic.impl.board.BoardFactory
import com.paulcraciunas.game.logic.impl.plies.CastlePly
import com.paulcraciunas.game.logic.impl.plies.PlyFactory
import com.paulcraciunas.game.logic.impl.plies.StandardPly
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class PlyFactoryTest {
    private val on = Board()
    private val with = MutableGameInfo()

    private val underTest = PlyFactory()

    @Test
    fun `GIVEN default starting board WHEN getting plies for white THEN return all correct plies`() {
        on.from(BoardFactory.defaultBoard())
        val expected = mutableListOf<StandardPly>()
            .addAllPawnMoves(Side.WHITE, Rank.`3`)
            .addAllPawnMoves(Side.WHITE, Rank.`4`)
            .apply {
                add(StandardPly(Side.WHITE, Piece.Knight, Locus.b1, Locus.a3))
                add(StandardPly(Side.WHITE, Piece.Knight, Locus.b1, Locus.c3))
                add(StandardPly(Side.WHITE, Piece.Knight, Locus.g1, Locus.h3))
                add(StandardPly(Side.WHITE, Piece.Knight, Locus.g1, Locus.f3))
            }.map { ExpectedPly(it) }

        val actual = underTest.allLegalPlies(on = on, with = with)
            .map { ExpectedPly(it) }

        assertEquals(expected.size, actual.size)
        assertTrue(expected.containsAll(actual))
        assertTrue(actual.containsAll(expected))
    }

    @Test
    fun `WHEN getting plies THEN the board does not change`() {
        on.from(BoardFactory.defaultBoard())
        val temp = Board().from(on)

        underTest.allLegalPlies(on = on, with = with)

        assertEquals(temp, on)
    }

    @Test
    fun `GIVEN default starting board WHEN white moves e4 THEN return all correct plies for black`() {
        on.from(BoardFactory.defaultBoard())
        on.move(Locus.e2, Locus.e4, Side.WHITE)
        val expected = mutableListOf<StandardPly>()
            .addAllPawnMoves(Side.BLACK, Rank.`6`)
            .addAllPawnMoves(Side.BLACK, Rank.`5`)
            .apply {
                add(StandardPly(Side.BLACK, Piece.Knight, Locus.b8, Locus.a6))
                add(StandardPly(Side.BLACK, Piece.Knight, Locus.b8, Locus.c6))
                add(StandardPly(Side.BLACK, Piece.Knight, Locus.g8, Locus.h6))
                add(StandardPly(Side.BLACK, Piece.Knight, Locus.g8, Locus.f6))
            }.map { ExpectedPly(it) }

        val actual = underTest.allLegalPlies(
            on, with.copy(turn = Side.BLACK, inCheckCount = CheckCount.One)
        ).map { ExpectedPly(it) }

        assertEquals(expected.size, actual.size)
        assertTrue(expected.containsAll(actual))
        assertTrue(actual.containsAll(expected))
    }

    @Test
    fun `GIVEN white is checkmated WHEN computing legal plies THEN return an empty list`() {
        on.from(BoardFactory.defaultBoard())
            .playAlternatingMoves(
                listOf(
                    //Fool's Mate
                    Locus.f2 to Locus.f3, // f3
                    Locus.e7 to Locus.e6, // e6
                    Locus.g2 to Locus.g4, // g4??
                    Locus.d8 to Locus.h4, // Qh4#
                )
            )

        underTest.allLegalPlies(
            on = on,
            with = with.copy(turn = Side.WHITE, inCheckCount = CheckCount.One)
        ).assertNoMoves()
    }

    @Test
    fun `GIVEN black is checkmated WHEN computing legal plies THEN return an empty list`() {
        on.from(BoardFactory.defaultBoard())
            .playAlternatingMoves(
                listOf(
                    //Scholar's Mate
                    Locus.e2 to Locus.e4, // e4
                    Locus.e7 to Locus.e5, // e5
                    Locus.d1 to Locus.h5, // Qh5
                    Locus.b8 to Locus.c6, // Kc6
                    Locus.f1 to Locus.c4, // Bc4
                    Locus.g8 to Locus.f6, // Kf6??
                    Locus.h5 to Locus.f7, // Qxf7#
                )
            )

        underTest.allLegalPlies(on = on, with = with.copy(turn = Side.BLACK)).assertNoMoves()
    }

    @Test
    fun `GIVEN black is stalemated WHEN computing legal plies THEN return an empty list`() {
        on.add(piece = Piece.King, side = Side.BLACK, at = Locus.h7)
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = Locus.a4)
        on.add(piece = Piece.King, side = Side.WHITE, at = Locus.f7)
        on.add(piece = Piece.Bishop, side = Side.WHITE, at = Locus.g7)
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = Locus.a3)

        underTest.allLegalPlies(on = on, with = with.copy(turn = Side.BLACK)).assertNoMoves()
    }

    @Test
    fun `GIVEN white is stalemated WHEN computing legal plies THEN return an empty list`() {
        on.add(piece = Piece.King, side = Side.BLACK, at = Locus.f3)
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = Locus.f2)
        on.add(piece = Piece.King, side = Side.WHITE, at = Locus.f1)

        underTest.allLegalPlies(on = on, with = with.copy(turn = Side.WHITE)).assertNoMoves()
    }

    @Test
    fun `WHEN in double check THEN return only king moves`() {
        // Use a bishop in front of the king
        on.add(piece = Piece.King, side = Side.BLACK, at = Locus.e8)
        on.add(piece = Piece.Knight, side = Side.BLACK, at = Locus.d6)
        on.add(piece = Piece.Knight, side = Side.BLACK, at = Locus.c5)
        on.add(piece = Piece.King, side = Side.WHITE, at = Locus.e1)
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus.e3)
        on.add(piece = Piece.Queen, side = Side.WHITE, at = Locus.a4)

        val expected = mutableListOf<StandardPly>()
            .apply {
                add(StandardPly(Side.BLACK, Piece.King, Locus.e8, Locus.d8))
                add(StandardPly(Side.BLACK, Piece.King, Locus.e8, Locus.f8))
                add(StandardPly(Side.BLACK, Piece.King, Locus.e8, Locus.f7))
            }.map { ExpectedPly(it) }

        val actual = underTest.allLegalPlies(
            on, with.copy(turn = Side.BLACK)
        ).map { ExpectedPly(it) }

        assertEquals(expected.size, actual.size)
        assertTrue(expected.containsAll(actual))
        assertTrue(actual.containsAll(expected))
    }

    @Test
    fun `GIVEN in double check WHEN computing check count THEN return two`() {
        // Use a bishop in front of the king
        on.add(piece = Piece.King, side = Side.BLACK, at = Locus.e8)
        on.add(piece = Piece.Knight, side = Side.BLACK, at = Locus.d6)
        on.add(piece = Piece.Knight, side = Side.BLACK, at = Locus.c5)
        on.add(piece = Piece.King, side = Side.WHITE, at = Locus.e1)
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus.e3)
        on.add(piece = Piece.Queen, side = Side.WHITE, at = Locus.a4)

        assertEquals(CheckCount.Two, underTest.checkCount(Locus.e8, on, turn = Side.WHITE))
    }

    @Test
    fun `WHEN piece is pinned THEN it cannot move`() {
        // Use a bishop in front of the king
        on.add(piece = Piece.Bishop, side = Side.BLACK, at = Locus.a7)
        on.add(piece = Piece.King, side = Side.BLACK, at = Locus.a8)
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus.a1)

        val expected = mutableListOf<StandardPly>()
            .apply {
                add(StandardPly(Side.BLACK, Piece.King, Locus.a8, Locus.b8))
                add(StandardPly(Side.BLACK, Piece.King, Locus.a8, Locus.b7))
            }.map { ExpectedPly(it) }

        val actual = underTest.allLegalPlies(
            on, with.copy(turn = Side.BLACK)
        ).map { ExpectedPly(it) }

        assertEquals(expected.size, actual.size)
        assertTrue(expected.containsAll(actual))
        assertTrue(actual.containsAll(expected))
    }

    @Test
    fun `WHEN castling is available THEN return it`() {
        on.add(piece = Piece.King, side = Side.BLACK, at = Locus.e8)
        on.add(piece = Piece.Rook, side = Side.BLACK, at = Locus.a8)
        on.add(piece = Piece.Rook, side = Side.BLACK, at = Locus.h8)
        on.add(piece = Piece.King, side = Side.WHITE, at = Locus.e1)
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus.a1)
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus.h1)

        underTest.allLegalPlies(on, with.copy(turn = Side.WHITE))
            .assertHas<CastlePly>(
                turn = Side.WHITE,
                piece = Piece.King,
                home = Locus.e1,
                location = Locus.c1
            ).assertHas<CastlePly>(
                turn = Side.WHITE,
                piece = Piece.King,
                home = Locus.e1,
                location = Locus.g1
            )
        underTest.allLegalPlies(on, with.copy(turn = Side.BLACK))
            .assertHas<CastlePly>(
                turn = Side.BLACK,
                piece = Piece.King,
                home = Locus.e8,
                location = Locus.c8
            ).assertHas<CastlePly>(
                turn = Side.BLACK,
                piece = Piece.King,
                home = Locus.e8,
                location = Locus.g8
            )
    }

    @ParameterizedTest(name = "{4} castling is not possible for {3} when enemy rook is on {2}")
    @MethodSource("forbiddenCastles")
    fun `WHEN castling intermediate square is in check THEN do not allow castling`(
        kingHome: Locus,
        rookHome: Locus,
        enemyRook: Locus,
        turn: Side,
        side: String, // This isn't needed for the test, but for pretty printing and readability
    ) {
        on.add(piece = Piece.King, side = turn, at = kingHome)
        on.add(piece = Piece.Rook, side = turn, at = rookHome)
        on.add(piece = Piece.Rook, side = turn.other(), at = enemyRook)

        underTest.allLegalPlies(on, with.copy(turn = turn))
            .assertNoMovesOf<CastlePly>()
    }

    @Test
    fun `WHEN computing checks on a default board THEN return no checks`() {
        on.from(BoardFactory.defaultBoard())

        assertEquals(CheckCount.None, underTest.checkCount(Locus.e8, on = on, with.turn))
        assertEquals(CheckCount.None, underTest.checkCount(Locus.e1, on = on, with.turn.other()))
    }

    @Test
    fun `WHEN computing checks THEN the board does not change`() {
        on.from(BoardFactory.defaultBoard())
        val temp = Board().from(on)

        underTest.checkCount(Locus.e8, on = on, with.turn)

        assertEquals(temp, on)
    }

    @Test
    fun `WHEN about to be smother mated THEN king cannot move`() {
        on.add(piece = Piece.King, side = Side.BLACK, at = Locus.a8)
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = Locus.a7)
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = Locus.b7)
        on.add(piece = Piece.Rook, side = Side.BLACK, at = Locus.h8)
        on.add(piece = Piece.King, side = Side.WHITE, at = Locus.g1)
        on.add(piece = Piece.Queen, side = Side.WHITE, at = Locus.b8)
        on.add(piece = Piece.Knight, side = Side.WHITE, at = Locus.a6)
        val expectedPly = ExpectedPly(Side.BLACK, Piece.Rook, Locus.h8, Locus.b8)

        val actual = underTest.allLegalPlies(
            on, with.copy(turn = Side.BLACK)
        ).map { ExpectedPly(it) }

        assertEquals(1, actual.size)
        assertEquals(expectedPly, actual.first())
    }

    private fun MutableList<StandardPly>.addAllPawnMoves(side: Side, toRank: Rank) = apply {
        File.entries.forEach {
            add(StandardPly(side, Piece.Pawn, Locus.from(it, pawnStart(side)), to = Locus.from(it, toRank)))
        }
    }

    private fun IBoard.playAlternatingMoves(moves: List<Pair<Locus, Locus>>) {
        var turn = Side.WHITE
        moves.forEach {
            move(it.first, it.second, turn)
            turn = turn.other()
        }
    }

    companion object {
        @JvmStatic
        fun forbiddenCastles(): List<Arguments> =
            listOf<Arguments>(
                // Order is: King, Rook, Enemy Rook, turn, name (for readability)
                Arguments.of(Locus.e8, Locus.h8, Locus.g1, Side.BLACK, "KingSide"),
                Arguments.of(Locus.e8, Locus.h8, Locus.f1, Side.BLACK, "KingSide"),
                Arguments.of(Locus.e8, Locus.a8, Locus.c1, Side.BLACK, "QueenSide"),
                Arguments.of(Locus.e8, Locus.a8, Locus.d1, Side.BLACK, "QueenSide"),
                Arguments.of(Locus.e1, Locus.h1, Locus.g8, Side.WHITE, "KingSide"),
                Arguments.of(Locus.e1, Locus.h1, Locus.f8, Side.WHITE, "KingSide"),
                Arguments.of(Locus.e1, Locus.a1, Locus.c8, Side.WHITE, "QueenSide"),
                Arguments.of(Locus.e1, Locus.a1, Locus.d8, Side.WHITE, "QueenSide"),
            )
    }
}
