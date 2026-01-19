package com.paulcraciunas.game.logic.gameover

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Result
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.game.logic.api.state.CheckCount
import com.paulcraciunas.game.logic.impl.gameover.StaleMateStrategy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class StaleMateStrategyTest {
    private val helper = GameTestHelper()

    private val underTest = StaleMateStrategy()

    @Test
    fun `GIVEN player not in check with no legal moves WHEN strategy is applied THEN game state is stalemate`() {
        // Given - Classic stalemate position
        val pieces = listOf(
            Triple(Piece.King, Side.WHITE, Locus(File.a, Rank.`1`)),
            Triple(Piece.King, Side.BLACK, Locus(File.c, Rank.`3`)),
            Triple(Piece.Queen, Side.BLACK, Locus(File.b, Rank.`3`))
        )
        val game = helper.createGameWithPieces(pieces, Side.WHITE)
        game.start()

        // Set stalemate conditions
        game.info.inCheckCount = CheckCount.None
        game.plies.clear()

        // When
        val result = underTest(game)

        // Then
        assertTrue(result is Game.GameState.Finished)
        assertEquals(Result.StaleMate, (result as Game.GameState.Finished).result)
    }

    @Test
    fun `GIVEN player in check with no legal moves WHEN strategy is applied THEN game state unchanged`() {
        // Given - Checkmate scenario (not stalemate)
        val game = helper.createGameWithKingsOnly()
        game.start()

        // Set checkmate conditions
        game.info.inCheckCount = CheckCount.One
        game.plies.clear()
        val originalState = game.state

        // When
        val result = underTest(game)

        // Then
        assertEquals(originalState, result)
    }

    @Test
    fun `GIVEN player not in check with legal moves available WHEN strategy is applied THEN game state unchanged`() {
        // Given - Position where white has moves available (not stalemate)
        val pieces = listOf(
            Triple(Piece.King, Side.WHITE, Locus(File.a, Rank.`1`)),
            Triple(Piece.King, Side.BLACK, Locus(File.c, Rank.`3`)),
            Triple(Piece.Queen, Side.BLACK, Locus(File.b, Rank.`3`)),
            Triple(Piece.Pawn, Side.WHITE, Locus(File.h, Rank.`2`)) // White pawn that can move
        )
        val game = helper.createGameWithPieces(pieces, Side.WHITE)
        game.start()

        // Verify conditions: not in check, has legal moves
        game.info.inCheckCount = CheckCount.None
        val originalState = game.state

        // When
        val result = underTest(game)

        // Then
        assertEquals(originalState, result)
    }

    @Test
    fun `GIVEN player in double check with no legal moves WHEN strategy is applied THEN game state unchanged`() {
        // Given - Double check scenario
        val game = helper.createGameWithKingsOnly()
        game.start()

        // Set double check conditions
        game.info.inCheckCount = CheckCount.Two
        game.plies.clear()
        val originalState = game.state

        // When
        val result = underTest(game)

        // Then
        assertEquals(originalState, result)
    }
}
