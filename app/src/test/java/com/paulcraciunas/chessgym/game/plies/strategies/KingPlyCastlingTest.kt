package com.paulcraciunas.chessgym.game.plies.strategies

import com.paulcraciunas.chessgym.game.CheckCount
import com.paulcraciunas.chessgym.game.GameState
import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.board.Board
import com.paulcraciunas.chessgym.game.board.File
import com.paulcraciunas.chessgym.game.board.File.a
import com.paulcraciunas.chessgym.game.board.File.b
import com.paulcraciunas.chessgym.game.board.File.c
import com.paulcraciunas.chessgym.game.board.File.d
import com.paulcraciunas.chessgym.game.board.File.e
import com.paulcraciunas.chessgym.game.board.File.f
import com.paulcraciunas.chessgym.game.board.File.g
import com.paulcraciunas.chessgym.game.board.File.h
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece
import com.paulcraciunas.chessgym.game.board.Rank
import com.paulcraciunas.chessgym.game.board.Rank.`1`
import com.paulcraciunas.chessgym.game.board.Rank.`7`
import com.paulcraciunas.chessgym.game.board.Rank.`8`
import com.paulcraciunas.chessgym.game.plies.CastlePly
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class KingPlyCastlingTest {
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
        kingHome: Pair<File, Rank>,
        rookHome: Pair<File, Rank>,
        kingDest: Pair<File, Rank>,
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
                location = kingDest
            )
    }

    @Test
    fun `WHEN castling is not available THEN do not return castling moves`() {
        on.add(piece = Piece.Rook, side = Side.BLACK, at = Locus(h, `8`))
        val withOut = GameState(turn = Side.BLACK, castling = emptySet())

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
        kingHome: Pair<File, Rank>,
        rookHome: Pair<File, Rank>,
        occupied: Pair<File, Rank>,
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
            mutableListOf<Arguments>().apply {
                // Order is: King, Rook, Occupier, turn, name (for readability)
                add(Arguments.of(e to `8`, h to `8`, g to `8`, Side.BLACK, "KingSide"))
                add(Arguments.of(e to `8`, h to `8`, f to `8`, Side.BLACK, "KingSide"))
                add(Arguments.of(e to `8`, a to `8`, b to `8`, Side.BLACK, "QueenSide"))
                add(Arguments.of(e to `8`, a to `8`, c to `8`, Side.BLACK, "QueenSide"))
                add(Arguments.of(e to `8`, a to `8`, d to `8`, Side.BLACK, "QueenSide"))
                add(Arguments.of(e to `1`, h to `1`, g to `1`, Side.WHITE, "KingSide"))
                add(Arguments.of(e to `1`, h to `1`, f to `1`, Side.WHITE, "KingSide"))
                add(Arguments.of(e to `1`, a to `1`, b to `1`, Side.WHITE, "QueenSide"))
                add(Arguments.of(e to `1`, a to `1`, c to `1`, Side.WHITE, "QueenSide"))
                add(Arguments.of(e to `1`, a to `1`, d to `1`, Side.WHITE, "QueenSide"))
            }

        @JvmStatic
        fun permittedCastles(): List<Arguments> =
            mutableListOf<Arguments>().apply {
                // Order is: King, Rook, King destination, turn, name (for readability)
                add(Arguments.of(e to `8`, h to `8`, g to `8`, Side.BLACK, "KingSide"))
                add(Arguments.of(e to `8`, a to `8`, c to `8`, Side.BLACK, "QueenSide"))
                add(Arguments.of(e to `1`, h to `1`, g to `1`, Side.WHITE, "KingSide"))
                add(Arguments.of(e to `1`, a to `1`, c to `1`, Side.WHITE, "QueenSide"))
            }
    }
}
