package com.paulcraciunas.domain.impl

import com.paulcraciunas.domain.api.RandomFactory
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.RealGameFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class GenerateMoveThePieceBoardImplTest {
    private val randomFactory = SequentialRandomFactory()
    private val gameFactory = RealGameFactory()

    private val underTest = GenerateMoveThePieceBoardImpl(GenerateRandomLociImpl(randomFactory), randomFactory, gameFactory)

    @Test
    fun `GIVEN valid parameters WHEN invoke THEN returns board with player piece`() {
        // When
        val result = underTest(
            piece = Piece.Rook,
            requiredMoves = 1,
            opposingPieceCount = 2
        )

        // Then
        assertNotNull(result.playerPieceLocus)
    }

    @Test
    fun `GIVEN valid parameters WHEN invoke THEN returns board with correct number of opposing pieces`() {
        // Given
        val expectedOpposingCount = 4

        // When
        val result = underTest(
            piece = Piece.Bishop,
            requiredMoves = 1,
            opposingPieceCount = expectedOpposingCount
        )

        // Then
        assertEquals(expectedOpposingCount, result.opposingPieces.size)
    }

    @Test
    fun `GIVEN valid parameters WHEN invoke THEN player position is not same as any opposing piece`() {
        // When
        val result = underTest(
            piece = Piece.Knight,
            requiredMoves = 2,
            opposingPieceCount = 4
        )

        // Then
        assertTrue(result.playerPieceLocus !in result.opposingPieces.keys)
    }

    @Test
    fun `GIVEN Rook piece WHEN invoke THEN generates valid board`() {
        // When
        val result = underTest(
            piece = Piece.Rook,
            requiredMoves = 1,
            opposingPieceCount = 4
        )

        // Then
        assertNotNull(result)
        assertTrue(result.opposingPieces.isNotEmpty())
    }

    @Test
    fun `GIVEN Bishop piece WHEN invoke THEN generates valid board`() {
        // When
        val result = underTest(
            piece = Piece.Bishop,
            requiredMoves = 1,
            opposingPieceCount = 4
        )

        // Then
        assertNotNull(result)
        assertTrue(result.opposingPieces.isNotEmpty())
    }

    @Test
    fun `GIVEN Knight piece WHEN invoke THEN generates valid board`() {
        // When
        val result = underTest(
            piece = Piece.Knight,
            requiredMoves = 1,
            opposingPieceCount = 4
        )

        // Then
        assertNotNull(result)
        assertTrue(result.opposingPieces.isNotEmpty())
    }

    @Test
    fun `GIVEN Queen piece WHEN invoke THEN generates valid board`() {
        // When
        val result = underTest(
            piece = Piece.Queen,
            requiredMoves = 1,
            opposingPieceCount = 4
        )

        // Then
        assertNotNull(result)
        assertTrue(result.opposingPieces.isNotEmpty())
    }

    @Test
    fun `GIVEN multiple required moves WHEN invoke THEN generates valid board`() {
        // When
        val result = underTest(
            piece = Piece.Rook,
            requiredMoves = 3,
            opposingPieceCount = 4
        )

        // Then
        assertNotNull(result)
    }

    @Test
    fun `GIVEN many opposing pieces WHEN invoke THEN generates valid board`() {
        // When
        val result = underTest(
            piece = Piece.Rook,
            requiredMoves = 1,
            opposingPieceCount = 6
        )

        // Then
        assertNotNull(result)
        assertTrue(result.opposingPieces.size <= 10)
    }

    @Test
    fun `GIVEN invalid parameters WHEN invoke THEN throws`() {
        // When
        assertThrows<IllegalStateException> {
            underTest(piece = Piece.Rook, requiredMoves = 1, opposingPieceCount = 64)
        }
    }

    /**
     * A simple sequential random factory for testing.
     * Returns predictable values for reproducible tests.
     */
    private class SequentialRandomFactory : RandomFactory {
        private var counter = 0

        override fun nextInt(from: Int, to: Int): Int {
            val range = to - from
            if (range <= 0) return from
            val result = from + (counter % range)
            counter++
            return result
        }
    }
}
