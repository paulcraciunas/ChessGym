package com.paulcraciunas.game.logic.gameover

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Result
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.game.logic.impl.gameover.DrawByInsufficientMaterialStrategy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class DrawByInsufficientMaterialStrategyTest {
    private val helper = GameTestHelper()

    private val underTest = DrawByInsufficientMaterialStrategy()

    @Test
    fun `GIVEN kings and two knights WHEN strategy is applied THEN game state unchanged`() {
        // Given
        val pieces = listOf(
            Triple(Piece.King, Side.WHITE, Locus(File.e, Rank.`1`)),
            Triple(Piece.King, Side.BLACK, Locus(File.e, Rank.`8`)),
            Triple(Piece.Knight, Side.WHITE, Locus(File.b, Rank.`1`)),
            Triple(Piece.Knight, Side.WHITE, Locus(File.g, Rank.`1`))
        )
        val game = helper.createGameWithPieces(pieces)
        game.start()

        // When
        val result = underTest(game)

        // Then
        assertEquals(Game.GameState.InProgress, result)
    }

    @Test
    fun `GIVEN only two kings on board WHEN strategy is applied THEN game state is draw by insufficient material`() {
        // Given
        val game = helper.createGameWithKingsOnly()
        game.start()

        // When
        val result = underTest(game)

        // Then
        assertTrue(result is Game.GameState.Finished)
        assertEquals(Result.DrawByInsufficientMaterial, (result as Game.GameState.Finished).result)
    }

    @Test
    fun `GIVEN king and knight vs king WHEN strategy is applied THEN game state is draw by insufficient material`() {
        // Given
        val pieces = listOf(
            Triple(Piece.King, Side.WHITE, Locus(File.e, Rank.`1`)),
            Triple(Piece.King, Side.BLACK, Locus(File.e, Rank.`8`)),
            Triple(Piece.Knight, Side.WHITE, Locus(File.b, Rank.`1`))
        )
        val game = helper.createGameWithPieces(pieces)
        game.start()

        // When
        val result = underTest(game)

        // Then
        assertTrue(result is Game.GameState.Finished)
        assertEquals(Result.DrawByInsufficientMaterial, (result as Game.GameState.Finished).result)
    }

    @Test
    fun `GIVEN king vs king and knight WHEN strategy is applied THEN game state is draw by insufficient material`() {
        // Given
        val pieces = listOf(
            Triple(Piece.King, Side.WHITE, Locus(File.e, Rank.`1`)),
            Triple(Piece.King, Side.BLACK, Locus(File.e, Rank.`8`)),
            Triple(Piece.Knight, Side.BLACK, Locus(File.b, Rank.`8`))
        )
        val game = helper.createGameWithPieces(pieces)
        game.start()

        // When
        val result = underTest(game)

        // Then
        assertTrue(result is Game.GameState.Finished)
        assertEquals(Result.DrawByInsufficientMaterial, (result as Game.GameState.Finished).result)
    }

    @Test
    fun `GIVEN king and bishop vs king WHEN strategy is applied THEN game state is draw by insufficient material`() {
        // Given
        val pieces = listOf(
            Triple(Piece.King, Side.WHITE, Locus(File.e, Rank.`1`)),
            Triple(Piece.King, Side.BLACK, Locus(File.e, Rank.`8`)),
            Triple(Piece.Bishop, Side.WHITE, Locus(File.c, Rank.`1`))
        )
        val game = helper.createGameWithPieces(pieces)
        game.start()

        // When
        val result = underTest(game)

        // Then
        assertTrue(result is Game.GameState.Finished)
        assertEquals(Result.DrawByInsufficientMaterial, (result as Game.GameState.Finished).result)
    }

    @Test
    fun `GIVEN king vs king and bishop WHEN strategy is applied THEN game state is draw by insufficient material`() {
        // Given
        val pieces = listOf(
            Triple(Piece.King, Side.WHITE, Locus(File.e, Rank.`1`)),
            Triple(Piece.King, Side.BLACK, Locus(File.e, Rank.`8`)),
            Triple(Piece.Bishop, Side.BLACK, Locus(File.f, Rank.`8`))
        )
        val game = helper.createGameWithPieces(pieces)
        game.start()

        // When
        val result = underTest(game)

        // Then
        assertTrue(result is Game.GameState.Finished)
        assertEquals(Result.DrawByInsufficientMaterial, (result as Game.GameState.Finished).result)
    }

    @Test
    fun `GIVEN king and two knights vs king WHEN strategy is applied THEN game state unchanged`() {
        // Given - Two knights can theoretically deliver checkmate
        val pieces = listOf(
            Triple(Piece.King, Side.WHITE, Locus(File.e, Rank.`1`)),
            Triple(Piece.King, Side.BLACK, Locus(File.e, Rank.`8`)),
            Triple(Piece.Knight, Side.WHITE, Locus(File.b, Rank.`1`)),
            Triple(Piece.Knight, Side.WHITE, Locus(File.g, Rank.`1`))
        )
        val game = helper.createGameWithPieces(pieces)
        game.start()

        // When
        val result = underTest(game)

        // Then
        assertEquals(Game.GameState.InProgress, result)
    }

    @Test
    fun `GIVEN king and queen vs king WHEN strategy is applied THEN game state unchanged`() {
        // Given - Queen can deliver checkmate
        val pieces = listOf(
            Triple(Piece.King, Side.WHITE, Locus(File.e, Rank.`1`)),
            Triple(Piece.King, Side.BLACK, Locus(File.e, Rank.`8`)),
            Triple(Piece.Queen, Side.WHITE, Locus(File.d, Rank.`1`))
        )
        val game = helper.createGameWithPieces(pieces)
        game.start()

        // When
        val result = underTest(game)

        // Then
        assertEquals(Game.GameState.InProgress, result)
    }

    @Test
    fun `GIVEN king and rook vs king WHEN strategy is applied THEN game state unchanged`() {
        // Given - Rook can deliver checkmate
        val pieces = listOf(
            Triple(Piece.King, Side.WHITE, Locus(File.e, Rank.`1`)),
            Triple(Piece.King, Side.BLACK, Locus(File.e, Rank.`8`)),
            Triple(Piece.Rook, Side.WHITE, Locus(File.a, Rank.`1`))
        )
        val game = helper.createGameWithPieces(pieces)
        game.start()

        // When
        val result = underTest(game)

        // Then
        assertEquals(Game.GameState.InProgress, result)
    }

    @Test
    fun `GIVEN king and pawn vs king WHEN strategy is applied THEN game state unchanged`() {
        // Given - Pawn can promote and deliver checkmate
        val pieces = listOf(
            Triple(Piece.King, Side.WHITE, Locus(File.e, Rank.`1`)),
            Triple(Piece.King, Side.BLACK, Locus(File.e, Rank.`8`)),
            Triple(Piece.Pawn, Side.WHITE, Locus(File.e, Rank.`2`))
        )
        val game = helper.createGameWithPieces(pieces)
        game.start()

        // When
        val result = underTest(game)

        // Then
        assertEquals(Game.GameState.InProgress, result)
    }

    @Test
    fun `GIVEN missing one king WHEN strategy is applied THEN game state unchanged`() {
        // Given - Custom puzzle scenario with only one king
        val pieces = listOf(
            Triple(Piece.King, Side.WHITE, Locus(File.e, Rank.`1`))
            // No black king
        )
        val game = helper.createGameWithPieces(pieces)
        game.start()

        // When
        val result = underTest(game)

        // Then
        assertEquals(Game.GameState.InProgress, result)
    }

    @Test
    fun `GIVEN both sides have sufficient material WHEN strategy is applied THEN game state unchanged`() {
        // Given - Both sides have queens
        val pieces = listOf(
            Triple(Piece.King, Side.WHITE, Locus(File.e, Rank.`1`)),
            Triple(Piece.King, Side.BLACK, Locus(File.e, Rank.`8`)),
            Triple(Piece.Queen, Side.WHITE, Locus(File.d, Rank.`1`)),
            Triple(Piece.Queen, Side.BLACK, Locus(File.d, Rank.`8`))
        )
        val game = helper.createGameWithPieces(pieces)
        game.start()

        // When
        val result = underTest(game)

        // Then
        assertEquals(Game.GameState.InProgress, result)
    }

    @Test
    fun `GIVEN one side has sufficient material and other has insufficient WHEN strategy is applied THEN game state unchanged`() {
        // Given - White has queen (sufficient), Black has only king (insufficient)
        val pieces = listOf(
            Triple(Piece.King, Side.WHITE, Locus(File.e, Rank.`1`)),
            Triple(Piece.King, Side.BLACK, Locus(File.e, Rank.`8`)),
            Triple(Piece.Queen, Side.WHITE, Locus(File.d, Rank.`1`))
            // Black has only king
        )
        val game = helper.createGameWithPieces(pieces)
        game.start()

        // When
        val result = underTest(game)

        // Then
        assertEquals(Game.GameState.InProgress, result)
    }

    @Test
    fun `GIVEN king and two bishops vs king WHEN strategy is applied THEN game state unchanged`() {
        // Given - Two bishops can deliver checkmate
        val pieces = listOf(
            Triple(Piece.King, Side.WHITE, Locus(File.e, Rank.`1`)),
            Triple(Piece.King, Side.BLACK, Locus(File.e, Rank.`8`)),
            Triple(Piece.Bishop, Side.WHITE, Locus(File.c, Rank.`1`)),
            Triple(Piece.Bishop, Side.WHITE, Locus(File.f, Rank.`1`))
        )
        val game = helper.createGameWithPieces(pieces)
        game.start()

        // When
        val result = underTest(game)

        // Then
        assertEquals(Game.GameState.InProgress, result)
    }

    @Test
    fun `GIVEN king and bishop vs king and bishop same color WHEN strategy is applied THEN game state is draw by insufficient material`() {
        // Given - Both bishops on light squares, neither side can mate
        val pieces = listOf(
            Triple(Piece.King, Side.WHITE, Locus(File.e, Rank.`1`)),
            Triple(Piece.King, Side.BLACK, Locus(File.e, Rank.`8`)),
            Triple(Piece.Bishop, Side.WHITE, Locus(File.f, Rank.`1`)), // Light square
            Triple(Piece.Bishop, Side.BLACK, Locus(File.h, Rank.`1`))  // Light square
        )
        val game = helper.createGameWithPieces(pieces)
        game.start()

        // When
        val result = underTest(game)

        // Then
        assertTrue(result is Game.GameState.Finished)
        assertEquals(Result.DrawByInsufficientMaterial, (result as Game.GameState.Finished).result)
    }

    @Test
    fun `GIVEN king and bishop vs king and bishop different colors WHEN strategy is applied THEN game state unchanged`() {
        // Given - Bishops on different colored squares, checkmate is possible
        val pieces = listOf(
            Triple(Piece.King, Side.WHITE, Locus(File.e, Rank.`1`)),
            Triple(Piece.King, Side.BLACK, Locus(File.e, Rank.`8`)),
            Triple(Piece.Bishop, Side.WHITE, Locus(File.c, Rank.`1`)), // Dark square
            Triple(Piece.Bishop, Side.BLACK, Locus(File.f, Rank.`1`))  // Light square
        )
        val game = helper.createGameWithPieces(pieces)
        game.start()

        // When
        val result = underTest(game)

        // Then
        assertEquals(Game.GameState.InProgress, result)
    }
}
