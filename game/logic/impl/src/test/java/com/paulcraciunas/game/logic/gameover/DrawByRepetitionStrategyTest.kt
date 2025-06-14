package com.paulcraciunas.game.logic.gameover

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Result
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.game.logic.impl.gameover.DrawByRepetitionStrategy
import com.paulcraciunas.game.logic.loc
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
            Triple(Piece.King, Side.WHITE, Locus(File.e, Rank.`1`)),
            Triple(Piece.King, Side.BLACK, Locus(File.e, Rank.`8`)),
            Triple(Piece.Knight, Side.WHITE, Locus(File.g, Rank.`1`)),
            Triple(Piece.Knight, Side.BLACK, Locus(File.b, Rank.`8`))
        )
        val game = helper.createGameWithPieces(pieces)
        game.start()

        // Create moves for only 2 repetitions
        // Play first cycle
        game.play("g1".loc(), "f3".loc())
        game.play("b8".loc(), "c6".loc())
        game.play("f3".loc(), "g1".loc())
        game.play("c6".loc(), "b8".loc())

        // Play second cycle
        game.play("g1".loc(), "f3".loc())
        game.play("b8".loc(), "c6".loc())
        game.play("f3".loc(), "g1".loc())

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
            Triple(Piece.King, Side.WHITE, Locus(File.e, Rank.`1`)),
            Triple(Piece.King, Side.BLACK, Locus(File.e, Rank.`8`)),
            Triple(Piece.Knight, Side.WHITE, Locus(File.g, Rank.`1`)),
            Triple(Piece.Queen, Side.WHITE, Locus(File.d, Rank.`1`)), // Add material to avoid insufficient material
            Triple(Piece.Queen, Side.BLACK, Locus(File.d, Rank.`8`))
        )
        val game = helper.createGameWithPieces(pieces)
        game.start()

        // Play only a few moves (less than required for repetition check)
        game.play("g1".loc(),"f3".loc())
        game.play("e8".loc(),"f8".loc())

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
            Triple(Piece.King, Side.WHITE, Locus(File.e, Rank.`1`)),
            Triple(Piece.King, Side.BLACK, Locus(File.e, Rank.`8`)),
            Triple(Piece.Knight, Side.WHITE, Locus(File.g, Rank.`1`)),
            Triple(Piece.Knight, Side.BLACK, Locus(File.b, Rank.`8`)),
            Triple(Piece.Pawn, Side.WHITE, Locus(File.h, Rank.`2`)),
            Triple(Piece.Pawn, Side.BLACK, Locus(File.a, Rank.`7`))
        )
        val game = helper.createGameWithPieces(pieces)
        game.start()

        // Play different moves that don't create repetition
        game.play("g1".loc(), "f3".loc())
        game.play("b8".loc(), "c6".loc())
        game.play("f3".loc(), "e5".loc())
        game.play("c6".loc(), "d4".loc())
        game.play("h2".loc(), "h3".loc())
        game.play("a7".loc(), "a6".loc())

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
            Triple(Piece.King, Side.WHITE, Locus(File.e, Rank.`1`)),
            Triple(Piece.King, Side.BLACK, Locus(File.e, Rank.`8`)),
            Triple(Piece.Queen, Side.WHITE, Locus(File.d, Rank.`1`)), // Add material to avoid insufficient material
            Triple(Piece.Queen, Side.BLACK, Locus(File.d, Rank.`8`))
        )
        val game = helper.createGameWithPieces(pieces)
        game.start()

        // Create king repetition: Ke1-f1-e1-f1-e1-f1 and Ke8-f8-e8-f8-e8-f8
        // Play two complete cycles to trigger repetition
        repeat(2) {
            game.play("e1".loc(), "f1".loc())
            game.play("e8".loc(), "f8".loc())
            game.play("f1".loc(), "e1".loc())
            game.play("f8".loc(), "e8".loc())
        }

        // When
        val result = underTest(game)

        // Then
        assertTrue(result is Game.GameState.Finished)
        assertEquals(Result.DrawByRepetition, (result as Game.GameState.Finished).result)
    }
}