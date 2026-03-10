package com.paulcraciunas.game.logic

import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.MoveAdapter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class MoveAdapterTest {
    private val underTest = MoveAdapter()

    @Test
    fun `GIVEN standard move WHEN parsing THEN returns correct move`() {
        // Given
        val moveString = "e2e4"

        // When
        val result = underTest.from(moveString)

        // Then
        assertEquals(Locus.from("e2")!!, result.from)
        assertEquals(Locus.from("e4")!!, result.to)
        assertNull(result.promotion)
    }

    @Test
    fun `GIVEN move with promotion WHEN parsing THEN returns correct move with promotion`() {
        // Given
        val moveString = "e7e8Q"

        // When
        val result = underTest.from(moveString)

        // Then
        assertEquals(Locus.from("e7")!!, result.from)
        assertEquals(Locus.from("e8")!!, result.to)
        assertEquals(Piece.Queen, result.promotion)
    }

    @Test
    fun `GIVEN move with knight promotion WHEN parsing THEN returns correct move with knight promotion`() {
        // Given
        val moveString = "e7e8N"

        // When
        val result = underTest.from(moveString)

        // Then
        assertEquals(Locus.from("e7")!!, result.from)
        assertEquals(Locus.from("e8")!!, result.to)
        assertEquals(Piece.Knight, result.promotion)
    }

    @Test
    fun `GIVEN move with rook promotion WHEN parsing THEN returns correct move with rook promotion`() {
        // Given
        val moveString = "e7e8R"

        // When
        val result = underTest.from(moveString)

        // Then
        assertEquals(Locus.from("e7")!!, result.from)
        assertEquals(Locus.from("e8")!!, result.to)
        assertEquals(Piece.Rook, result.promotion)
    }

    @Test
    fun `GIVEN move with bishop promotion WHEN parsing THEN returns correct move with bishop promotion`() {
        // Given
        val moveString = "e7e8B"

        // When
        val result = underTest.from(moveString)

        // Then
        assertEquals(Locus.from("e7")!!, result.from)
        assertEquals(Locus.from("e8")!!, result.to)
        assertEquals(Piece.Bishop, result.promotion)
    }

    @Test
    fun `GIVEN move with uppercase promotion WHEN parsing THEN returns correct move with promotion`() {
        // Given
        val moveString = "e7e8Q"

        // When
        val result = underTest.from(moveString)

        // Then
        assertEquals(Locus.from("e7")!!, result.from)
        assertEquals(Locus.from("e8")!!, result.to)
        assertEquals(Piece.Queen, result.promotion)
    }

    @Test
    fun `GIVEN move with lowercase promotion WHEN parsing THEN throws exception`() {
        // Given
        val moveString = "e7e8q"

        // When & Then
        assertThrows<IllegalArgumentException> {
            underTest.from(moveString)
        }
    }

    @Test
    fun `GIVEN move with lowercase knight promotion WHEN parsing THEN throws exception`() {
        // Given
        val moveString = "e7e8n"

        // When & Then
        assertThrows<IllegalArgumentException> {
            underTest.from(moveString)
        }
    }

    @Test
    fun `GIVEN complex move WHEN parsing THEN returns correct move`() {
        // Given
        val moveString = "a1h8"

        // When
        val result = underTest.from(moveString)

        // Then
        assertEquals(Locus.from("a1")!!, result.from)
        assertEquals(Locus.from("h8")!!, result.to)
        assertNull(result.promotion)
    }

    @Test
    fun `GIVEN move with short promotion WHEN parsing THEN returns correct move with promotion`() {
        // Given
        val moveString = "a7a8Q"

        // When
        val result = underTest.from(moveString)

        // Then
        assertEquals(Locus.from("a7")!!, result.from)
        assertEquals(Locus.from("a8")!!, result.to)
        assertEquals(Piece.Queen, result.promotion)
    }

    @Test
    fun `GIVEN move with long promotion WHEN parsing THEN returns correct move with promotion`() {
        // Given
        val moveString = "h2h1R"

        // When
        val result = underTest.from(moveString)

        // Then
        assertEquals(Locus.from("h2")!!, result.from)
        assertEquals(Locus.from("h1")!!, result.to)
        assertEquals(Piece.Rook, result.promotion)
    }

    @Test
    fun `GIVEN move too short WHEN parsing THEN throws assertion error`() {
        // Given
        val moveString = "e2e"

        // When & Then
        assertThrows<AssertionError> {
            underTest.from(moveString)
        }
    }

    @Test
    fun `GIVEN move too long WHEN adapting THEN throws assertion error`() {
        // Given
        val moveString = "e2e4xx" // 6 characters

        // When & Then
        assertThrows<AssertionError> {
            underTest.from(moveString)
        }
    }

    @Test
    fun `GIVEN move with invalid promotion piece WHEN parsing THEN throws exception`() {
        // Given
        val moveString = "e2e4x" // 5 characters with invalid promotion

        // When & Then
        assertThrows<IllegalArgumentException> {
            underTest.from(moveString)
        }
    }

    @Test
    fun `GIVEN empty string WHEN parsing THEN throws assertion error`() {
        // Given
        val moveString = ""

        // When & Then
        assertThrows<AssertionError> {
            underTest.from(moveString)
        }
    }

    @Test
    fun `GIVEN single character WHEN parsing THEN throws assertion error`() {
        // Given
        val moveString = "e"

        // When & Then
        assertThrows<AssertionError> {
            underTest.from(moveString)
        }
    }

    @Test
    fun `GIVEN three characters WHEN parsing THEN throws assertion error`() {
        // Given
        val moveString = "e2e"

        // When & Then
        assertThrows<AssertionError> {
            underTest.from(moveString)
        }
    }

    @Test
    fun `GIVEN move with number promotion WHEN parsing THEN throws exception`() {
        // Given
        val moveString = "e7e81" // '1' is not a valid promotion piece

        // When & Then
        assertThrows<IllegalArgumentException> {
            underTest.from(moveString)
        }
    }

    @Test
    fun `GIVEN move with special character promotion WHEN parsing THEN throws exception`() {
        // Given
        val moveString = "e7e8!" // '!' is not a valid promotion piece

        // When & Then
        assertThrows<IllegalArgumentException> {
            underTest.from(moveString)
        }
    }

    @Test
    fun `GIVEN various valid moves WHEN parsing THEN all return correct results`() {
        // Given
        val testCases = listOf(
            "a1a2" to Pair("a1", "a2"),
            "h8h7" to Pair("h8", "h7"),
            "e4e5" to Pair("e4", "e5"),
            "d3d4" to Pair("d3", "d4"),
            "f6f7" to Pair("f6", "f7"),
            "g1g2" to Pair("g1", "g2"),
            "c8c7" to Pair("c8", "c7"),
            "b5b6" to Pair("b5", "b6")
        )

        // When & Then
        testCases.forEach { (moveString, expected) ->
            val result = underTest.from(moveString)
            assertEquals(Locus.from(expected.first)!!, result.from)
            assertEquals(Locus.from(expected.second)!!, result.to)
            assertNull(result.promotion)
        }
    }

    @Test
    fun `GIVEN various valid promotion moves WHEN parsing THEN all return correct results`() {
        // Given
        val testCases = listOf(
            "a7a8Q" to Piece.Queen,
            "h2h1R" to Piece.Rook,
            "e7e8N" to Piece.Knight,
            "d7d8B" to Piece.Bishop,
            "f7f8Q" to Piece.Queen,
            "g2g1R" to Piece.Rook,
            "c7c8N" to Piece.Knight,
            "b7b8B" to Piece.Bishop
        )

        // When & Then
        testCases.forEach { (moveString, expectedPromotion) ->
            val result = underTest.from(moveString)
            assertEquals(expectedPromotion, result.promotion)
        }
    }
}
