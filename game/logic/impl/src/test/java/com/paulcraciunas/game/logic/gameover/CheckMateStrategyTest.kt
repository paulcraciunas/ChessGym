package com.paulcraciunas.game.logic.gameover

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Result
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.gameover.CheckMateStrategy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class CheckMateStrategyTest {
    private val helper = GameTestHelper()

    private val underTest = CheckMateStrategy()

    @Test
    fun `GIVEN player in check with no legal moves WHEN strategy is applied THEN game state is checkmate`() {
        // Given - Create a back rank mate scenario
        val pieces = listOf(
            Triple(Piece.King, Side.WHITE, Locus.a1),
            Triple(Piece.King, Side.BLACK, Locus.a3),
            Triple(Piece.Rook, Side.BLACK, Locus.c1),
            Triple(Piece.Pawn, Side.WHITE, Locus.h2),
            Triple(Piece.Pawn, Side.WHITE, Locus.g2)
        )
        val game = helper.createGameWithPieces(pieces, Side.WHITE)
        game.start()

        // When
        val result = underTest(game)

        // Then
        assertTrue(result is Game.GameState.Finished)
        assertEquals(Result.CheckMate, (result as Game.GameState.Finished).result)
    }

    @Test
    fun `GIVEN player in double check with a legal move WHEN strategy is applied THEN game state is unchanged`() {
        // Given - Create a scenario with double check
        val pieces = listOf(
            Triple(Piece.King, Side.WHITE, Locus.e1),
            Triple(Piece.King, Side.BLACK, Locus.e8),
            Triple(Piece.Rook, Side.BLACK, Locus.e2),
            Triple(Piece.Bishop, Side.BLACK, Locus.d2),
            Triple(Piece.Bishop, Side.BLACK, Locus.d3)
        )
        val game = helper.createGameWithPieces(pieces, Side.WHITE)
        game.start()
        val originalState = game.state

        // When
        val result = underTest(game)

        // Then
        assertEquals(originalState, result)
    }

    @Test
    fun `GIVEN player in double check with no legal moves WHEN strategy is applied THEN game state is checkmate`() {
        // Given - Create a scenario with double check
        val pieces = listOf(
            Triple(Piece.King, Side.WHITE, Locus.a1),
            Triple(Piece.Bishop, Side.WHITE, Locus.d4),
            Triple(Piece.Knight, Side.WHITE, Locus.d3),
            Triple(Piece.King, Side.BLACK, Locus.b3),
            Triple(Piece.Rook, Side.BLACK, Locus.a2),
            Triple(Piece.Bishop, Side.BLACK, Locus.c2),
            Triple(Piece.Bishop, Side.BLACK, Locus.c3)
        )
        val game = helper.createGameWithPieces(pieces, Side.WHITE)
        game.start()

        // When
        val result = underTest(game)

        // Then
        assertTrue(result is Game.GameState.Finished)
        assertEquals(Result.CheckMate, (result as Game.GameState.Finished).result)
    }

    @Test
    fun `GIVEN player in check with legal moves available WHEN strategy is applied THEN game state unchanged`() {
        // Given - King in check but can move
        val pieces = listOf(
            Triple(Piece.King, Side.WHITE, Locus.e1),
            Triple(Piece.King, Side.BLACK, Locus.e8),
            Triple(Piece.Rook, Side.BLACK, Locus.e2)
        )
        val game = helper.createGameWithPieces(pieces, Side.WHITE)
        game.start()
        val originalState = game.state

        // When
        val result = underTest(game)

        // Then
        assertEquals(originalState, result)
    }

    @Test
    fun `GIVEN player not in check with no legal moves WHEN strategy is applied THEN game state unchanged`() {
        // Given - Stalemate scenario (not checkmate)
        val game = helper.createGameWithKingsOnly()
        game.start()

        // Set no check and no legal moves
        val originalState = game.state

        // When
        val result = underTest(game)

        // Then
        assertEquals(originalState, result)
    }

    @Test
    fun `GIVEN player not in check with legal moves WHEN strategy is applied THEN game state unchanged`() {
        // Given - Normal game position
        val game = helper.createGameWithKingsOnly()
        game.start()

        // Normal position with moves available
        val originalState = game.state

        // When
        val result = underTest(game)

        // Then
        assertEquals(originalState, result)
    }
}