package com.paulcraciunas.chessgym.game.plies.strategies

import com.paulcraciunas.chessgym.game.GameState
import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.allLocationsExcept
import com.paulcraciunas.chessgym.game.assertHas
import com.paulcraciunas.chessgym.game.assertMoves
import com.paulcraciunas.chessgym.game.assertMovesOf
import com.paulcraciunas.chessgym.game.assertNoMoves
import com.paulcraciunas.chessgym.game.board.Board
import com.paulcraciunas.chessgym.game.board.File.e
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece
import com.paulcraciunas.chessgym.game.board.Rank.`7`
import com.paulcraciunas.chessgym.game.loc
import com.paulcraciunas.chessgym.game.plies.PromotionPly
import com.paulcraciunas.chessgym.game.plies.StandardPly
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class PawnPlyStrategyTest {
    private val home = Locus(e, `7`)
    private val on = Board()
    private val with = GameState(turn = Side.BLACK)

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
        home: String,
        positions: List<String>,
    ) {
        on.add(piece = Piece.Pawn, side = turn, at = home.loc())

        underTest.plies(from = home.loc(), on = on, with = with.copy(turn = turn))
            .assertMoves(
                turn = turn,
                piece = Piece.Pawn,
                home = home.loc(),
                locations = positions.map { it.loc() }
            )
    }

    @ParameterizedTest(name = "{0} pawn from {1} can capture on {3}")
    @MethodSource("captures")
    fun `GIVEN captures are possible WHEN getting plies THEN return captures`(
        turn: Side,
        home: String,
        block: String,
        positions: List<String>,
    ) {
        on.add(piece = Piece.Pawn, side = turn, at = home.loc())
        on.add(piece = Piece.Rook, side = turn, at = block.loc()) // Block movement
        positions.forEach {
            on.add(piece = Piece.Knight, side = turn.other(), at = it.loc())
        }

        underTest.plies(from = home.loc(), on = on, with = with.copy(turn = turn))
            .assertMoves(
                turn = turn,
                piece = Piece.Pawn,
                home = home.loc(),
                locations = positions.map { it.loc() }
            )
    }

    @ParameterizedTest(name = "{0} pawn from {1} can capture on {2} and move to {3}")
    @MethodSource("movesAndCaptures")
    fun `GIVEN captures and moves are possible WHEN getting plies THEN return all`(
        turn: Side,
        home: String,
        captures: List<String>,
        allMoves: List<String>,
    ) {
        on.add(piece = Piece.Pawn, side = turn, at = home.loc())
        captures.forEach {
            on.add(piece = Piece.Knight, side = turn.other(), at = it.loc())
        }

        underTest.plies(from = home.loc(), on = on, with = with.copy(turn = turn))
            .assertMoves(
                turn = turn,
                piece = Piece.Pawn,
                home = home.loc(),
                locations = (captures + allMoves).map { it.loc() }
            )
    }

    @ParameterizedTest(name = "BLACK pawn from {0} can capture en-passent {1} and move to {2}")
    @MethodSource("enPassentCaptures")
    fun `GIVEN en-passent possible WHEN getting plies THEN return en-passent`(
        home: String,
        enemy: String,
        destination: String,
        lastPly: Pair<String, String>, // from, to
    ) {
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = home.loc())
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = enemy.loc())
        val whitePly = StandardPly(
            turn = Side.WHITE,
            piece = Piece.Pawn,
            from = lastPly.first.loc(),
            to = lastPly.second.loc()
        )

        underTest.plies(from = home.loc(), on = on, with = with.copy(lastPly = whitePly))
            .assertHas<StandardPly>(
                turn = Side.BLACK,
                piece = Piece.Pawn,
                home = home.loc(),
                location = destination.loc()
            )
    }

    @Test
    fun `GIVEN enemy rook moved in en-passent way WHEN getting plies THEN do not return en-passent`() {
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = "e4".loc())
        on.add(piece = Piece.Rook, side = Side.WHITE, at = "d4".loc())
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = "e3".loc())
        val whitePly = StandardPly(
            turn = Side.WHITE,
            piece = Piece.Rook,
            from = "d2".loc(),
            to = "d4".loc()
        )

        underTest.plies(from = "e4".loc(), on = on, with = with.copy(lastPly = whitePly))
            .assertNoMoves()
    }

    @Test
    fun `GIVEN promotion possible WHEN getting plies THEN return promotion`() {
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = "e2".loc())

        underTest.plies(from = "e2".loc(), on = on, with = with)
            .assertMovesOf<PromotionPly>(
                turn = Side.BLACK,
                piece = Piece.Pawn,
                home = "e2".loc(),
                locations = listOf("e1").map { it.loc() }
            )
    }

    @Test
    fun `GIVEN promotion blocked WHEN getting plies THEN return nothing`() {
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = "e2".loc())
        on.add(piece = Piece.Rook, side = Side.BLACK, at = "e1".loc())

        underTest.plies(from = "e2".loc(), on = on, with = with).assertNoMoves()
    }

    @Test
    fun `GIVEN a capture promotion possible WHEN getting plies THEN return capture promotion`() {
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = "e2".loc())
        on.add(piece = Piece.Knight, side = Side.WHITE, at = "d1".loc())
        on.add(piece = Piece.Queen, side = Side.WHITE, at = "f1".loc())

        underTest.plies(from = "e2".loc(), on = on, with = with)
            .assertMovesOf<PromotionPly>(
                turn = Side.BLACK,
                piece = Piece.Pawn,
                home = "e2".loc(),
                locations = listOf("e1", "d1", "f1").map { it.loc() }
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
        home: String,
        block: String,
        positions: List<String>,
    ) {
        on.add(piece = Piece.Pawn, side = turn, at = home.loc())
        on.add(piece = Piece.Rook, side = turn, at = block.loc()) // Block movement
        positions.forEach {
            on.add(piece = Piece.Knight, side = turn.other(), at = it.loc())
        }

        positions.forEach {
            assertTrue(
                underTest.canAttack(from = home.loc(), to = it.loc(), on = on, turn = turn)
            )
        }
        allLocationsExcept(home.loc(), positions.map { it.loc() }).forEach {
            assertFalse(
                underTest.canAttack(from = home.loc(), to = it, on = on, turn = turn)
            )
        }
    }

    companion object {
        @JvmStatic
        fun standardMoves(): List<Arguments> =
            mutableListOf<Arguments>(
                // Order is: turn, from, moves,
                Arguments.of(Side.BLACK, "e7", listOf("e6", "e5")),
                Arguments.of(Side.BLACK, "e6", listOf("e5")),
                Arguments.of(Side.BLACK, "e3", listOf("e2")),
                Arguments.of(Side.WHITE, "e2", listOf("e3", "e4")),
                Arguments.of(Side.WHITE, "e3", listOf("e4")),
                Arguments.of(Side.WHITE, "e6", listOf("e7")),
            )

        @JvmStatic
        fun captures(): List<Arguments> =
            mutableListOf<Arguments>(
                // Order is: turn, from, block, captures
                Arguments.of(Side.BLACK, "e7", "e6", listOf("d6", "f6")),
                Arguments.of(Side.BLACK, "a6", "a5", listOf("b5")),
                Arguments.of(Side.BLACK, "h5", "h4", listOf("g4")),
                Arguments.of(Side.WHITE, "e2", "e3", listOf("d3", "f3")),
                Arguments.of(Side.WHITE, "a3", "a4", listOf("b4")),
                Arguments.of(Side.WHITE, "h4", "h5", listOf("g5")),
            )

        @JvmStatic
        fun movesAndCaptures(): List<Arguments> =
            mutableListOf<Arguments>(
                // Order is: turn, from, captures, moves
                Arguments.of(Side.BLACK, "e7", listOf("d6", "f6"), listOf("e6", "e5")),
                Arguments.of(Side.BLACK, "e6", listOf("d5", "f5"), listOf("e5")),
                Arguments.of(Side.BLACK, "e3", listOf("d2", "f2"), listOf("e2")),
                Arguments.of(Side.BLACK, "a6", listOf("b5"), listOf("a5")),
                Arguments.of(Side.BLACK, "h5", listOf("g4"), listOf("h4")),
                Arguments.of(Side.WHITE, "e2", listOf("d3", "f3"), listOf("e3", "e4")),
                Arguments.of(Side.WHITE, "e3", listOf("d4", "f4"), listOf("e4")),
                Arguments.of(Side.WHITE, "e6", listOf("d7", "f7"), listOf("e7")),
                Arguments.of(Side.WHITE, "a3", listOf("b4"), listOf("a4")),
                Arguments.of(Side.WHITE, "h4", listOf("g5"), listOf("h5")),
            )

        @JvmStatic
        fun enPassentCaptures(): List<Arguments> =
            mutableListOf<Arguments>(
                // Order is: from, capture, to, lastPly
                Arguments.of("e4", "d4", "d3", Pair("d2", "d4")),
                Arguments.of("e4", "f4", "f3", Pair("f2", "f4")),
                Arguments.of("a4", "b4", "b3", Pair("b2", "b4")),
                Arguments.of("h4", "g4", "g3", Pair("g2", "g4")),
            )
    }
}
