package com.paulcraciunas.game.plies.strategies

import com.paulcraciunas.game.CheckCount
import com.paulcraciunas.game.GameState
import com.paulcraciunas.game.Side
import com.paulcraciunas.game.assertHas
import com.paulcraciunas.game.assertNoMovesOf
import com.paulcraciunas.game.board.Board
import com.paulcraciunas.game.board.File.e
import com.paulcraciunas.game.board.File.g
import com.paulcraciunas.game.board.File.h
import com.paulcraciunas.game.board.Locus
import com.paulcraciunas.game.board.Piece
import com.paulcraciunas.game.board.Rank.`1`
import com.paulcraciunas.game.board.Rank.`7`
import com.paulcraciunas.game.board.Rank.`8`
import com.paulcraciunas.game.loc
import com.paulcraciunas.game.plies.CastlePly
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class KingPlyCastlingTest {
    private val home = Locus(e, `8`)
    private val on = Board().apply {
        add(piece = Piece.King, side = Side.BLACK, at = home)
    }
    private val with = GameState(turn = Side.BLACK)

    private val underTest = KingPlyStrategy()

    @Test
    fun `WHEN king is not on correct square THEN do not return castling moves`() {
        on.remove(at = home)
        on.add(piece = Piece.King, side = Side.BLACK, at = Locus(e, `7`))

        underTest.plies(from = Locus(e, `7`), on = on, with = with).assertNoMovesOf<CastlePly>()
    }

    @Test
    fun `WHEN rook is not on correct square THEN do not return castling moves`() {
        on.add(piece = Piece.Rook, side = Side.BLACK, at = Locus(g, `8`))

        underTest.plies(from = home, on = on, with = with).assertNoMovesOf<CastlePly>()
    }

    @Test
    fun `GIVEN black pieces on white positions WHEN castling is available THEN do not return castling moves`() {
        on.remove(at = home)
        on.add(piece = Piece.King, side = Side.BLACK, at = Locus(e, `1`))
        on.add(piece = Piece.Rook, side = Side.BLACK, at = Locus(h, `1`))

        underTest.plies(from = Locus(e, `1`), on = on, with = with).assertNoMovesOf<CastlePly>()
    }

    @ParameterizedTest(name = "{4} castling is allowed for {3} when king is on {0} and rook on {1}")
    @MethodSource("permittedCastles")
    fun `WHEN castling is possible THEN return castling`(
        kingHome: String,
        rookHome: String,
        kingDest: String,
        turn: Side,
        side: String, // This isn't needed for the test, but for pretty printing and readability
    ) {
        on.remove(at = home)
        on.add(piece = Piece.Rook, side = turn, at = rookHome.loc())
        on.add(piece = Piece.King, side = turn, at = kingHome.loc())

        underTest.plies(from = kingHome.loc(), on = on, with = with.copy(turn = turn))
            .assertHas<CastlePly>(
                turn = turn,
                piece = Piece.King,
                home = kingHome.loc(),
                location = kingDest.loc()
            )
    }

    @Test
    fun `WHEN castling is not available THEN do not return castling moves`() {
        on.add(piece = Piece.Rook, side = Side.BLACK, at = Locus(h, `8`))
        val withOut = GameState(turn = Side.BLACK, blackCastling = emptySet())

        underTest.plies(from = home, on = on, with = withOut).assertNoMovesOf<CastlePly>()
    }

    @Test
    fun `GIVEN castling is available WHEN is in check THEN do not return castling moves`() {
        on.add(piece = Piece.Rook, side = Side.BLACK, at = Locus(h, `8`))
        val inCheck = with.copy(inCheckCount = CheckCount.One)

        underTest.plies(from = home, on = on, with = inCheck).assertNoMovesOf<CastlePly>()
    }

    @ParameterizedTest(name = "{4} castling forbidden for {3} when {2} is occupied")
    @MethodSource("forbiddenCastles")
    fun `GIVEN castling is available WHEN square is occupied THEN do not allow castle`(
        kingHome: String,
        rookHome: String,
        occupied: String,
        turn: Side,
        side: String, // This isn't needed for the test, but for pretty printing and readability
    ) {
        on.remove(at = home)
        on.add(piece = Piece.Rook, side = turn, at = rookHome.loc())
        on.add(piece = Piece.King, side = turn, at = kingHome.loc())
        on.add(piece = Piece.Knight, side = turn.other(), at = occupied.loc())

        underTest.plies(from = kingHome.loc(), on = on, with = with.copy(turn = turn))
            .assertNoMovesOf<CastlePly>()
    }

    companion object {
        @JvmStatic
        fun forbiddenCastles(): List<Arguments> =
            listOf<Arguments>(
                // Order is: King, Rook, Occupier, turn, name (for readability)
                Arguments.of("e8", "h8", "g8", Side.BLACK, "KingSide"),
                Arguments.of("e8", "h8", "f8", Side.BLACK, "KingSide"),
                Arguments.of("e8", "a8", "b8", Side.BLACK, "QueenSide"),
                Arguments.of("e8", "a8", "c8", Side.BLACK, "QueenSide"),
                Arguments.of("e8", "a8", "d8", Side.BLACK, "QueenSide"),
                Arguments.of("e1", "h1", "g1", Side.WHITE, "KingSide"),
                Arguments.of("e1", "h1", "f1", Side.WHITE, "KingSide"),
                Arguments.of("e1", "a1", "b1", Side.WHITE, "QueenSide"),
                Arguments.of("e1", "a1", "c1", Side.WHITE, "QueenSide"),
                Arguments.of("e1", "a1", "d1", Side.WHITE, "QueenSide"),
            )

        @JvmStatic
        fun permittedCastles(): List<Arguments> =
            listOf<Arguments>(
                // Order is: King, Rook, King destination, turn, name (for readability)
                Arguments.of("e8", "h8", "g8", Side.BLACK, "KingSide"),
                Arguments.of("e8", "a8", "c8", Side.BLACK, "QueenSide"),
                Arguments.of("e1", "h1", "g1", Side.WHITE, "KingSide"),
                Arguments.of("e1", "a1", "c1", Side.WHITE, "QueenSide"),
            )
    }
}
