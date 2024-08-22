package com.paulcraciunas.chessgym.game.plies

import com.paulcraciunas.chessgym.game.CheckCount
import com.paulcraciunas.chessgym.game.GameState
import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.board.Board
import com.paulcraciunas.chessgym.game.board.BoardFactory
import com.paulcraciunas.chessgym.game.board.File
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece
import com.paulcraciunas.chessgym.game.board.Rank
import com.paulcraciunas.chessgym.game.assertHas
import com.paulcraciunas.chessgym.game.assertNoMoves
import com.paulcraciunas.chessgym.game.assertNoMovesOf
import com.paulcraciunas.chessgym.game.loc
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class PlyFactoryTest {
    private val on = Board()
    private val with = GameState()

    private val underTest = PlyFactory()

    @Test
    fun `GIVEN default starting board WHEN getting plies for white THEN return all correct plies`() {
        on.from(BoardFactory.defaultBoard())
        val expected = mutableListOf<StandardPly>()
            .addAllPawnMoves(Side.WHITE, Rank.`3`)
            .addAllPawnMoves(Side.WHITE, Rank.`4`)
            .apply {
                add(StandardPly(Side.WHITE, Piece.Knight, "b1".loc(), "a3".loc()))
                add(StandardPly(Side.WHITE, Piece.Knight, "b1".loc(), "c3".loc()))
                add(StandardPly(Side.WHITE, Piece.Knight, "g1".loc(), "h3".loc()))
                add(StandardPly(Side.WHITE, Piece.Knight, "g1".loc(), "f3".loc()))
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
        on.move("e2".loc(), "e4".loc(), Side.WHITE)
        val expected = mutableListOf<StandardPly>()
            .addAllPawnMoves(Side.BLACK, Rank.`6`)
            .addAllPawnMoves(Side.BLACK, Rank.`5`)
            .apply {
                add(StandardPly(Side.BLACK, Piece.Knight, "b8".loc(), "a6".loc()))
                add(StandardPly(Side.BLACK, Piece.Knight, "b8".loc(), "c6".loc()))
                add(StandardPly(Side.BLACK, Piece.Knight, "g8".loc(), "h6".loc()))
                add(StandardPly(Side.BLACK, Piece.Knight, "g8".loc(), "f6".loc()))
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
                    "f2" to "f3", // f3
                    "e7" to "e6", // e6
                    "g2" to "g4", // g4??
                    "d8" to "h4", // Qh4#
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
                    "e2" to "e4", // e4
                    "e7" to "e5", // e5
                    "d1" to "h5", // Qh5
                    "b8" to "c6", // Kc6
                    "f1" to "c4", // Bc4
                    "g8" to "f6", // Kf6??
                    "h5" to "f7", // Qxf7#
                )
            )

        underTest.allLegalPlies(on = on, with = with.copy(turn = Side.BLACK)).assertNoMoves()
    }

    @Test
    fun `GIVEN black is stalemated WHEN computing legal plies THEN return an empty list`() {
        on.add(piece = Piece.King, side = Side.BLACK, at = "h7".loc())
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = "a4".loc())
        on.add(piece = Piece.King, side = Side.WHITE, at = "f7".loc())
        on.add(piece = Piece.Bishop, side = Side.WHITE, at = "g7".loc())
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = "a3".loc())

        underTest.allLegalPlies(on = on, with = with.copy(turn = Side.BLACK)).assertNoMoves()
    }

    @Test
    fun `GIVEN white is stalemated WHEN computing legal plies THEN return an empty list`() {
        on.add(piece = Piece.King, side = Side.BLACK, at = "f3".loc())
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = "f2".loc())
        on.add(piece = Piece.King, side = Side.WHITE, at = "f1".loc())

        underTest.allLegalPlies(on = on, with = with.copy(turn = Side.WHITE)).assertNoMoves()
    }

    @Test
    fun `WHEN in double check THEN return only king moves`() {
        // Use a bishop in front of the king
        on.add(piece = Piece.King, side = Side.BLACK, at = "e8".loc())
        on.add(piece = Piece.Knight, side = Side.BLACK, at = "d6".loc())
        on.add(piece = Piece.Knight, side = Side.BLACK, at = "c5".loc())
        on.add(piece = Piece.King, side = Side.WHITE, at = "e1".loc())
        on.add(piece = Piece.Rook, side = Side.WHITE, at = "e3".loc())
        on.add(piece = Piece.Queen, side = Side.WHITE, at = "a4".loc())

        val expected = mutableListOf<StandardPly>()
            .apply {
                add(StandardPly(Side.BLACK, Piece.King, "e8".loc(), "d8".loc()))
                add(StandardPly(Side.BLACK, Piece.King, "e8".loc(), "f8".loc()))
                add(StandardPly(Side.BLACK, Piece.King, "e8".loc(), "f7".loc()))
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
        on.add(piece = Piece.King, side = Side.BLACK, at = "e8".loc())
        on.add(piece = Piece.Knight, side = Side.BLACK, at = "d6".loc())
        on.add(piece = Piece.Knight, side = Side.BLACK, at = "c5".loc())
        on.add(piece = Piece.King, side = Side.WHITE, at = "e1".loc())
        on.add(piece = Piece.Rook, side = Side.WHITE, at = "e3".loc())
        on.add(piece = Piece.Queen, side = Side.WHITE, at = "a4".loc())

        assertEquals(CheckCount.Two, underTest.canCheck("e8".loc(), on, turn = Side.WHITE))
    }

    @Test
    fun `WHEN piece is pinned THEN it cannot move`() {
        // Use a bishop in front of the king
        on.add(piece = Piece.Bishop, side = Side.BLACK, at = "a7".loc())
        on.add(piece = Piece.King, side = Side.BLACK, at = "a8".loc())
        on.add(piece = Piece.Rook, side = Side.WHITE, at = "a1".loc())

        val expected = mutableListOf<StandardPly>()
            .apply {
                add(StandardPly(Side.BLACK, Piece.King, "a8".loc(), "b8".loc()))
                add(StandardPly(Side.BLACK, Piece.King, "a8".loc(), "b7".loc()))
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
        on.add(piece = Piece.King, side = Side.BLACK, at = "e8".loc())
        on.add(piece = Piece.Rook, side = Side.BLACK, at = "a8".loc())
        on.add(piece = Piece.Rook, side = Side.BLACK, at = "h8".loc())
        on.add(piece = Piece.King, side = Side.WHITE, at = "e1".loc())
        on.add(piece = Piece.Rook, side = Side.WHITE, at = "a1".loc())
        on.add(piece = Piece.Rook, side = Side.WHITE, at = "h1".loc())

        underTest.allLegalPlies(on, with.copy(turn = Side.WHITE))
            .assertHas<CastlePly>(
                turn = Side.WHITE,
                piece = Piece.King,
                home = "e1".loc(),
                location = "c1".loc()
            ).assertHas<CastlePly>(
                turn = Side.WHITE,
                piece = Piece.King,
                home = "e1".loc(),
                location = "g1".loc()
            )
        underTest.allLegalPlies(on, with.copy(turn = Side.BLACK))
            .assertHas<CastlePly>(
                turn = Side.BLACK,
                piece = Piece.King,
                home = "e8".loc(),
                location = "c8".loc()
            ).assertHas<CastlePly>(
                turn = Side.BLACK,
                piece = Piece.King,
                home = "e8".loc(),
                location = "g8".loc()
            )
    }

    @ParameterizedTest(name = "{4} castling is not possible for {3} when enemy rook is on {2}")
    @MethodSource("forbiddenCastles")
    fun `WHEN castling intermediate square is in check THEN do not allow castling`(
        kingHome: String,
        rookHome: String,
        enemyRook: String,
        turn: Side,
        side: String, // This isn't needed for the test, but for pretty printing and readability
    ) {
        on.add(piece = Piece.King, side = turn, at = kingHome.loc())
        on.add(piece = Piece.Rook, side = turn, at = rookHome.loc())
        on.add(piece = Piece.Rook, side = turn.other(), at = enemyRook.loc())

        underTest.allLegalPlies(on, with.copy(turn = turn))
            .assertNoMovesOf<CastlePly>()
    }

    @Test
    fun `WHEN computing checks on a default board THEN return no checks`() {
        on.from(BoardFactory.defaultBoard())

        assertEquals(CheckCount.None, underTest.canCheck("e8".loc(), on = on, with.turn))
        assertEquals(CheckCount.None, underTest.canCheck("e1".loc(), on = on, with.turn.other()))
    }

    @Test
    fun `WHEN computing checks THEN the board does not change`() {
        on.from(BoardFactory.defaultBoard())
        val temp = Board().from(on)

        underTest.canCheck("e8".loc(), on = on, with.turn)

        assertEquals(temp, on)
    }

    @Test
    fun `WHEN about to be smother mated THEN king cannot move`() {
        on.add(piece = Piece.King, side = Side.BLACK, at = "a8".loc())
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = "a7".loc())
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = "b7".loc())
        on.add(piece = Piece.Rook, side = Side.BLACK, at = "h8".loc())
        on.add(piece = Piece.King, side = Side.WHITE, at = "g1".loc())
        on.add(piece = Piece.Queen, side = Side.WHITE, at = "b8".loc())
        on.add(piece = Piece.Knight, side = Side.WHITE, at = "a6".loc())
        val expectedPly = ExpectedPly(Side.BLACK, Piece.Rook, "h8".loc(), "b8".loc())

        val actual = underTest.allLegalPlies(
            on, with.copy(turn = Side.BLACK)
        ).map { ExpectedPly(it) }

        assertEquals(1, actual.size)
        assertEquals(expectedPly, actual.first())
    }

    private fun MutableList<StandardPly>.addAllPawnMoves(side: Side, toRank: Rank) = apply {
        File.entries.forEach {
            add(StandardPly(side, Piece.Pawn, Locus(it, side.pawnStart()), to = Locus(it, toRank)))
        }
    }

    private fun Board.playAlternatingMoves(moves: List<Pair<String, String>>) {
        var turn = Side.WHITE
        moves.forEach {
            move(it.first.loc(), it.second.loc(), turn)
            turn = turn.other()
        }
    }

    companion object {
        @JvmStatic
        fun forbiddenCastles(): List<Arguments> =
            mutableListOf<Arguments>(
                // Order is: King, Rook, Enemy Rook, turn, name (for readability)
                Arguments.of("e8", "h8", "g1", Side.BLACK, "KingSide"),
                Arguments.of("e8", "h8", "f1", Side.BLACK, "KingSide"),
                Arguments.of("e8", "a8", "b1", Side.BLACK, "QueenSide"),
                Arguments.of("e8", "a8", "c1", Side.BLACK, "QueenSide"),
                Arguments.of("e8", "a8", "d1", Side.BLACK, "QueenSide"),
                Arguments.of("e1", "h1", "g8", Side.WHITE, "KingSide"),
                Arguments.of("e1", "h1", "f8", Side.WHITE, "KingSide"),
                Arguments.of("e1", "a1", "b8", Side.WHITE, "QueenSide"),
                Arguments.of("e1", "a1", "c8", Side.WHITE, "QueenSide"),
                Arguments.of("e1", "a1", "d8", Side.WHITE, "QueenSide"),
            )
    }
}
