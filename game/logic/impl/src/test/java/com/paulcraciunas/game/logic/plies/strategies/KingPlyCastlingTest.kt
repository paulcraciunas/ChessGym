package com.paulcraciunas.game.logic.plies.strategies

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.state.CheckCount
import com.paulcraciunas.game.logic.assertHas
import com.paulcraciunas.game.logic.assertNoMovesOf
import com.paulcraciunas.game.logic.impl.MutableGameInfo
import com.paulcraciunas.game.logic.impl.board.Board
import com.paulcraciunas.game.logic.impl.plies.CastlePly
import com.paulcraciunas.game.logic.impl.plies.strategies.KingPlyStrategy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class KingPlyCastlingTest {
    private val home = Locus.e8
    private val on = Board().apply {
        add(piece = Piece.King, side = Side.BLACK, at = home)
    }
    private val with = MutableGameInfo(turn = Side.BLACK)

    private val underTest = KingPlyStrategy()

    @Test
    fun `WHEN king is not on correct square THEN do not return castling moves`() {
        on.remove(at = home)
        on.add(piece = Piece.King, side = Side.BLACK, at = Locus.e7)

        underTest.plies(from = Locus.e7, on = on, with = with).assertNoMovesOf<CastlePly>()
    }

    @Test
    fun `WHEN rook is not on correct square THEN do not return castling moves`() {
        on.add(piece = Piece.Rook, side = Side.BLACK, at = Locus.g8)

        underTest.plies(from = home, on = on, with = with).assertNoMovesOf<CastlePly>()
    }

    @Test
    fun `GIVEN black pieces on white positions WHEN castling is available THEN do not return castling moves`() {
        on.remove(at = home)
        on.add(piece = Piece.King, side = Side.BLACK, at = Locus.e1)
        on.add(piece = Piece.Rook, side = Side.BLACK, at = Locus.h1)

        underTest.plies(from = Locus.e1, on = on, with = with).assertNoMovesOf<CastlePly>()
    }

    @ParameterizedTest(name = "{4} castling is allowed for {3} when king is on {0} and rook on {1}")
    @MethodSource("permittedCastles")
    fun `WHEN castling is possible THEN return castling`(
        kingHome: Locus,
        rookHome: Locus,
        kingDest: Locus,
        turn: Side,
        side: String, // This isn't needed for the test, but for pretty printing and readability
    ) {
        on.remove(at = home)
        on.add(piece = Piece.Rook, side = turn, at = rookHome)
        on.add(piece = Piece.King, side = turn, at = kingHome)

        underTest.plies(from = kingHome, on = on, with = with.copy(turn = turn))
            .assertHas<CastlePly>(
                turn = turn,
                piece = Piece.King,
                home = kingHome,
                location = kingDest
            )
    }

    @Test
    fun `WHEN castling is not available THEN do not return castling moves`() {
        on.add(piece = Piece.Rook, side = Side.BLACK, at = Locus.h8)
        val withOut = MutableGameInfo(turn = Side.BLACK, blackCastling = emptySet())

        underTest.plies(from = home, on = on, with = withOut).assertNoMovesOf<CastlePly>()
    }

    @Test
    fun `GIVEN castling is available WHEN is in check THEN do not return castling moves`() {
        on.add(piece = Piece.Rook, side = Side.BLACK, at = Locus.h8)
        val inCheck = with.copy(inCheckCount = CheckCount.One)

        underTest.plies(from = home, on = on, with = inCheck).assertNoMovesOf<CastlePly>()
    }

    @ParameterizedTest(name = "{4} castling forbidden for {3} when {2} is occupied")
    @MethodSource("forbiddenCastles")
    fun `GIVEN castling is available WHEN square is occupied THEN do not allow castle`(
        kingHome: Locus,
        rookHome: Locus,
        occupied: Locus,
        turn: Side,
        side: String, // This isn't needed for the test, but for pretty printing and readability
    ) {
        on.remove(at = home)
        on.add(piece = Piece.Rook, side = turn, at = rookHome)
        on.add(piece = Piece.King, side = turn, at = kingHome)
        on.add(piece = Piece.Knight, side = turn.other(), at = occupied)

        underTest.plies(from = kingHome, on = on, with = with.copy(turn = turn))
            .assertNoMovesOf<CastlePly>()
    }

    companion object {
        @JvmStatic
        fun forbiddenCastles(): List<Arguments> =
            listOf<Arguments>(
                // Order is: King, Rook, Occupier, turn, name (for readability)
                Arguments.of(Locus.e8, Locus.h8, Locus.g8, Side.BLACK, "KingSide"),
                Arguments.of(Locus.e8, Locus.h8, Locus.f8, Side.BLACK, "KingSide"),
                Arguments.of(Locus.e8, Locus.a8, Locus.b8, Side.BLACK, "QueenSide"),
                Arguments.of(Locus.e8, Locus.a8, Locus.c8, Side.BLACK, "QueenSide"),
                Arguments.of(Locus.e8, Locus.a8, Locus.d8, Side.BLACK, "QueenSide"),
                Arguments.of(Locus.e1, Locus.h1, Locus.g1, Side.WHITE, "KingSide"),
                Arguments.of(Locus.e1, Locus.h1, Locus.f1, Side.WHITE, "KingSide"),
                Arguments.of(Locus.e1, Locus.a1, Locus.b1, Side.WHITE, "QueenSide"),
                Arguments.of(Locus.e1, Locus.a1, Locus.c1, Side.WHITE, "QueenSide"),
                Arguments.of(Locus.e1, Locus.a1, Locus.d1, Side.WHITE, "QueenSide"),
            )

        @JvmStatic
        fun permittedCastles(): List<Arguments> =
            listOf<Arguments>(
                // Order is: King, Rook, King destination, turn, name (for readability)
                Arguments.of(Locus.e8, Locus.h8, Locus.g8, Side.BLACK, "KingSide"),
                Arguments.of(Locus.e8, Locus.a8, Locus.c8, Side.BLACK, "QueenSide"),
                Arguments.of(Locus.e1, Locus.h1, Locus.g1, Side.WHITE, "KingSide"),
                Arguments.of(Locus.e1, Locus.a1, Locus.c1, Side.WHITE, "QueenSide"),
            )
    }
}
