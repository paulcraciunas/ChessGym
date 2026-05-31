package com.paulcraciunas.game.logic.plies.strategies

import com.paulcraciunas.game.logic.allLocationsExcept
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.assertHas
import com.paulcraciunas.game.logic.assertMoves
import com.paulcraciunas.game.logic.assertMovesOf
import com.paulcraciunas.game.logic.assertNoMoves
import com.paulcraciunas.game.logic.impl.MutableGameInfo
import com.paulcraciunas.game.logic.impl.board.Board
import com.paulcraciunas.game.logic.impl.plies.PromotionPly
import com.paulcraciunas.game.logic.impl.plies.StandardPly
import com.paulcraciunas.game.logic.impl.plies.strategies.PawnPlyStrategy
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class PawnPlyStrategyTest {
    private val home = Locus.e7
    private val on = Board()
    private val with = MutableGameInfo(turn = Side.BLACK)

    private val underTest = PawnPlyStrategy()

    @Test
    fun `GIVEN non-pawn WHEN getting plies THEN throw`() {
        on.add(piece = Piece.Bishop, side = Side.BLACK, at = home)

        assertThrows<AssertionError> {
            underTest.plies(from = home, on = on, with = with)
        }
    }

    @ParameterizedTest(name = "{0} pawn from {1} can move to {2}")
    @MethodSource("standardMoves")
    fun `GIVEN pawn on a certain square WHEN getting plies THEN return forward moves`(
        turn: Side,
        home: Locus,
        positions: List<Locus>,
    ) {
        on.add(piece = Piece.Pawn, side = turn, at = home)

        underTest.plies(from = home, on = on, with = with.copy(turn = turn))
            .assertMoves(
                turn = turn,
                piece = Piece.Pawn,
                home = home,
                locations = positions.map { it }
            )
    }

    @ParameterizedTest(name = "{0} pawn from {1} can capture on {3}")
    @MethodSource("captures")
    fun `GIVEN captures are possible WHEN getting plies THEN return captures`(
        turn: Side,
        home: Locus,
        block: Locus,
        positions: List<Locus>,
    ) {
        on.add(piece = Piece.Pawn, side = turn, at = home)
        on.add(piece = Piece.Rook, side = turn, at = block) // Block movement
        positions.forEach {
            on.add(piece = Piece.Knight, side = turn.other(), at = it)
        }

        underTest.plies(from = home, on = on, with = with.copy(turn = turn))
            .assertMoves(
                turn = turn,
                piece = Piece.Pawn,
                home = home,
                locations = positions.map { it }
            )
    }

    @ParameterizedTest(name = "{0} pawn from {1} can capture on {2} and move to {3}")
    @MethodSource("movesAndCaptures")
    fun `GIVEN captures and moves are possible WHEN getting plies THEN return all`(
        turn: Side,
        home: Locus,
        captures: List<Locus>,
        allMoves: List<Locus>,
    ) {
        on.add(piece = Piece.Pawn, side = turn, at = home)
        captures.forEach {
            on.add(piece = Piece.Knight, side = turn.other(), at = it)
        }

        underTest.plies(from = home, on = on, with = with.copy(turn = turn))
            .assertMoves(
                turn = turn,
                piece = Piece.Pawn,
                home = home,
                locations = (captures + allMoves).map { it }
            )
    }

    @ParameterizedTest(name = "BLACK pawn from {0} can capture en-passent {1} and move to {2}")
    @MethodSource("enPassentCaptures")
    fun `GIVEN en-passent possible WHEN getting plies THEN return en-passent`(
        home: Locus,
        enemy: Locus,
        destination: Locus,
        lastPly: Pair<Locus, Locus>, // from, to
    ) {
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = home)
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = enemy)
        val whitePly = StandardPly(
            turn = Side.WHITE,
            piece = Piece.Pawn,
            from = lastPly.first,
            to = lastPly.second
        )

        underTest.plies(from = home, on = on, with = with.copy(lastPly = whitePly))
            .assertHas<StandardPly>(
                turn = Side.BLACK,
                piece = Piece.Pawn,
                home = home,
                location = destination
            )
    }

    @Test
    fun `GIVEN enemy rook moved in en-passent way WHEN getting plies THEN do not return en-passent`() {
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = Locus.e4)
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus.d4)
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = Locus.e3)
        val whitePly = StandardPly(
            turn = Side.WHITE,
            piece = Piece.Rook,
            from = Locus.d2,
            to = Locus.d4
        )

        underTest.plies(from = Locus.e4, on = on, with = with.copy(lastPly = whitePly))
            .assertNoMoves()
    }

    @Test
    fun `GIVEN promotion possible WHEN getting plies THEN return promotion`() {
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = Locus.e2)

        underTest.plies(from = Locus.e2, on = on, with = with)
            .assertMovesOf<PromotionPly>(
                turn = Side.BLACK,
                piece = Piece.Pawn,
                home = Locus.e2,
                locations = listOf(Locus.e1)
            )
    }

    @Test
    fun `GIVEN promotion blocked WHEN getting plies THEN return nothing`() {
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = Locus.e2)
        on.add(piece = Piece.Rook, side = Side.BLACK, at = Locus.e1)

        underTest.plies(from = Locus.e2, on = on, with = with).assertNoMoves()
    }

    @Test
    fun `GIVEN a capture promotion possible WHEN getting plies THEN return capture promotion`() {
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = Locus.e2)
        on.add(piece = Piece.Knight, side = Side.WHITE, at = Locus.d1)
        on.add(piece = Piece.Queen, side = Side.WHITE, at = Locus.f1)

        underTest.plies(from = Locus.e2, on = on, with = with)
            .assertMovesOf<PromotionPly>(
                turn = Side.BLACK,
                piece = Piece.Pawn,
                home = Locus.e2,
                locations = listOf(Locus.e1, Locus.d1, Locus.f1)
            )
    }

    @Test
    fun `GIVEN non-pawn WHEN checking attacks THEN throw`() {
        on.add(piece = Piece.Bishop, side = Side.BLACK, at = home)

        assertThrows<AssertionError> {
            Locus.all {
                underTest.canAttack(from = home, to = it, on = on, turn = with.turn)
            }
        }
    }

    @ParameterizedTest(name = "{0} pawn from {1} can attack {3}")
    @MethodSource("captures")
    fun `WHEN checking attacks THEN return capture positions`(
        turn: Side,
        home: Locus,
        block: Locus,
        positions: List<Locus>,
    ) {
        on.add(piece = Piece.Pawn, side = turn, at = home)
        on.add(piece = Piece.Rook, side = turn, at = block) // Block movement
        positions.forEach {
            on.add(piece = Piece.Knight, side = turn.other(), at = it)
        }

        positions.forEach {
            assertTrue(
                underTest.canAttack(from = home, to = it, on = on, turn = turn)
            )
        }
        allLocationsExcept(home, positions.map { it }).forEach {
            assertFalse(
                underTest.canAttack(from = home, to = it, on = on, turn = turn)
            )
        }
    }

    companion object {
        @JvmStatic
        fun standardMoves(): List<Arguments> =
            listOf<Arguments>(
                // Order is: turn, from, moves,
                Arguments.of(Side.BLACK, Locus.e7, listOf(Locus.e6, Locus.e5)),
                Arguments.of(Side.BLACK, Locus.e6, listOf(Locus.e5)),
                Arguments.of(Side.BLACK, Locus.e3, listOf(Locus.e2)),
                Arguments.of(Side.WHITE, Locus.e2, listOf(Locus.e3, Locus.e4)),
                Arguments.of(Side.WHITE, Locus.e3, listOf(Locus.e4)),
                Arguments.of(Side.WHITE, Locus.e6, listOf(Locus.e7)),
            )

        @JvmStatic
        fun captures(): List<Arguments> =
            listOf<Arguments>(
                // Order is: turn, from, block, captures
                Arguments.of(Side.BLACK, Locus.e7, Locus.e6, listOf(Locus.d6, Locus.f6)),
                Arguments.of(Side.BLACK, Locus.a6, Locus.a5, listOf(Locus.b5)),
                Arguments.of(Side.BLACK, Locus.h5, Locus.h4, listOf(Locus.g4)),
                Arguments.of(Side.WHITE, Locus.e2, Locus.e3, listOf(Locus.d3, Locus.f3)),
                Arguments.of(Side.WHITE, Locus.a3, Locus.a4, listOf(Locus.b4)),
                Arguments.of(Side.WHITE, Locus.h4, Locus.h5, listOf(Locus.g5)),
            )

        @JvmStatic
        fun movesAndCaptures(): List<Arguments> =
            listOf<Arguments>(
                // Order is: turn, from, captures, moves
                Arguments.of(Side.BLACK, Locus.e7, listOf(Locus.d6, Locus.f6), listOf(Locus.e6, Locus.e5)),
                Arguments.of(Side.BLACK, Locus.e6, listOf(Locus.d5, Locus.f5), listOf(Locus.e5)),
                Arguments.of(Side.BLACK, Locus.e3, listOf(Locus.d2, Locus.f2), listOf(Locus.e2)),
                Arguments.of(Side.BLACK, Locus.a6, listOf(Locus.b5), listOf(Locus.a5)),
                Arguments.of(Side.BLACK, Locus.h5, listOf(Locus.g4), listOf(Locus.h4)),
                Arguments.of(Side.WHITE, Locus.e2, listOf(Locus.d3, Locus.f3), listOf(Locus.e3, Locus.e4)),
                Arguments.of(Side.WHITE, Locus.e3, listOf(Locus.d4, Locus.f4), listOf(Locus.e4)),
                Arguments.of(Side.WHITE, Locus.e6, listOf(Locus.d7, Locus.f7), listOf(Locus.e7)),
                Arguments.of(Side.WHITE, Locus.a3, listOf(Locus.b4), listOf(Locus.a4)),
                Arguments.of(Side.WHITE, Locus.h4, listOf(Locus.g5), listOf(Locus.h5)),
            )

        @JvmStatic
        fun enPassentCaptures(): List<Arguments> =
            listOf<Arguments>(
                // Order is: from, capture, to, lastPly
                Arguments.of(Locus.e4, Locus.d4, Locus.d3, Pair(Locus.d2, Locus.d4)),
                Arguments.of(Locus.e4, Locus.f4, Locus.f3, Pair(Locus.f2, Locus.f4)),
                Arguments.of(Locus.a4, Locus.b4, Locus.b3, Pair(Locus.b2, Locus.b4)),
                Arguments.of(Locus.h4, Locus.g4, Locus.g3, Pair(Locus.g2, Locus.g4)),
            )
    }
}
