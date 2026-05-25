package com.paulcraciunas.game.logic.gameover

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Result
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.gameover.DrawByRepetitionStrategy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class DrawByRepetitionStrategyTest {
    private val helper = GameTestHelper()

    private val underTest = DrawByRepetitionStrategy()

    @Test
    fun `GIVEN position repeated only 2 times WHEN strategy is applied THEN game state unchanged`() {
        // Given
        val pieces = listOf(
            Triple(Piece.King, Side.WHITE, Locus.e1),
            Triple(Piece.King, Side.BLACK, Locus.e8),
            Triple(Piece.Knight, Side.WHITE, Locus.g1),
            Triple(Piece.Knight, Side.BLACK, Locus.b8)
        )
        val game = helper.createGameWithPieces(pieces)
        game.start()

        // Create moves for only 2 repetitions
        // Play first cycle
        game.play(Locus.g1, Locus.f3)
        game.play(Locus.b8, Locus.c6)
        game.play(Locus.f3, Locus.g1)
        game.play(Locus.c6, Locus.b8)

        // Play second cycle
        game.play(Locus.g1, Locus.f3)
        game.play(Locus.b8, Locus.c6)
        game.play(Locus.f3, Locus.g1)

        val originalState = game.state

        // When
        val result = underTest(game)

        // Then
        assertEquals(originalState, result)
    }

    @Test
    fun `GIVEN insufficient history length WHEN strategy is applied THEN game state unchanged`() {
        // Given
        val pieces = listOf(
            Triple(Piece.King, Side.WHITE, Locus.e1),
            Triple(Piece.King, Side.BLACK, Locus.e8),
            Triple(Piece.Knight, Side.WHITE, Locus.g1),
            Triple(Piece.Queen, Side.WHITE, Locus.d1), // Add material to avoid insufficient material
            Triple(Piece.Queen, Side.BLACK, Locus.d8)
        )
        val game = helper.createGameWithPieces(pieces)
        game.start()

        // Play only a few moves (less than required for repetition check)
        game.play(Locus.g1,Locus.f3)
        game.play(Locus.e8,Locus.f8)

        val originalState = game.state

        // When
        val result = underTest(game)

        // Then
        assertEquals(originalState, result)
    }

    @Test
    fun `GIVEN no history WHEN strategy is applied THEN game state unchanged`() {
        // Given
        val game = helper.createGameWithKingsOnly()
        game.start()

        val originalState = game.state

        // When
        val result = underTest(game)

        // Then
        assertEquals(originalState, result)
    }

    @Test
    fun `GIVEN different positions in history WHEN strategy is applied THEN game state unchanged`() {
        // Given - Position where moves don't create repetition
        val pieces = listOf(
            Triple(Piece.King, Side.WHITE, Locus.e1),
            Triple(Piece.King, Side.BLACK, Locus.e8),
            Triple(Piece.Knight, Side.WHITE, Locus.g1),
            Triple(Piece.Knight, Side.BLACK, Locus.b8),
            Triple(Piece.Pawn, Side.WHITE, Locus.h2),
            Triple(Piece.Pawn, Side.BLACK, Locus.a7)
        )
        val game = helper.createGameWithPieces(pieces)
        game.start()

        // Play different moves that don't create repetition
        game.play(Locus.g1, Locus.f3)
        game.play(Locus.b8, Locus.c6)
        game.play(Locus.f3, Locus.e5)
        game.play(Locus.c6, Locus.d4)
        game.play(Locus.h2, Locus.h3)
        game.play(Locus.a7, Locus.a6)

        val originalState = game.state

        // When
        val result = underTest(game)

        // Then
        assertEquals(originalState, result)
    }

    @Test
    fun `GIVEN exact repetition pattern with king moves WHEN strategy is applied THEN game state is draw by repetition`() {
        // Given - Simple king repetition pattern
        val pieces = listOf(
            Triple(Piece.King, Side.WHITE, Locus.e1),
            Triple(Piece.King, Side.BLACK, Locus.e8),
            Triple(Piece.Queen, Side.WHITE, Locus.d1), // Add material to avoid insufficient material
            Triple(Piece.Queen, Side.BLACK, Locus.d8)
        )
        val game = helper.createGameWithPieces(pieces)
        game.start()

        // Create king repetition: Ke1-f1-e1-f1-e1-f1 and Ke8-f8-e8-f8-e8-f8
        // Play two complete cycles to trigger repetition
        repeat(2) {
            game.play(Locus.e1, Locus.f1)
            game.play(Locus.e8, Locus.f8)
            game.play(Locus.f1, Locus.e1)
            game.play(Locus.f8, Locus.e8)
        }

        // When
        val result = underTest(game)

        // Then
        assertTrue(result is Game.GameState.Finished)
        assertEquals(Result.DrawByRepetition, (result as Game.GameState.Finished).result)
    }
}