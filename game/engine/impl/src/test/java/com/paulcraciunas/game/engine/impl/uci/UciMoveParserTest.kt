package com.paulcraciunas.game.engine.impl.uci

import com.paulcraciunas.game.engine.api.EngineMove
import com.paulcraciunas.game.engine.api.UciMoveParser
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class UciMoveParserTest {

    @Nested
    internal inner class Parse {
        @Test
        fun `GIVEN standard 4-char move WHEN parse THEN returns EngineMove`() {
            val result = UciMoveParser.parse("e2e4")

            assertNotNull(result)
            assertEquals(Locus.e2, result!!.from)
            assertEquals(Locus.e4, result.to)
            assertNull(result.promotion)
        }

        @Test
        fun `GIVEN promotion move WHEN parse THEN returns EngineMove with promotion`() {
            val result = UciMoveParser.parse("a7a8q")

            assertNotNull(result)
            assertEquals(Locus.a7, result!!.from)
            assertEquals(Locus.a8, result.to)
            assertEquals(Piece.Queen, result.promotion)
        }

        @Test
        fun `GIVEN knight promotion WHEN parse THEN returns Knight promotion`() {
            val result = UciMoveParser.parse("b7b8n")

            assertNotNull(result)
            assertEquals(Piece.Knight, result!!.promotion)
        }

        @Test
        fun `GIVEN rook promotion WHEN parse THEN returns Rook promotion`() {
            val result = UciMoveParser.parse("c7c8r")

            assertNotNull(result)
            assertEquals(Piece.Rook, result!!.promotion)
        }

        @Test
        fun `GIVEN bishop promotion WHEN parse THEN returns Bishop promotion`() {
            val result = UciMoveParser.parse("d7d8b")

            assertNotNull(result)
            assertEquals(Piece.Bishop, result!!.promotion)
        }

        @Test
        fun `GIVEN too short string WHEN parse THEN returns null`() {
            assertNull(UciMoveParser.parse("e2"))
        }

        @Test
        fun `GIVEN too long string WHEN parse THEN returns null`() {
            assertNull(UciMoveParser.parse("e2e4qx"))
        }

        @Test
        fun `GIVEN invalid file WHEN parse THEN returns null`() {
            assertNull(UciMoveParser.parse("z2e4"))
        }

        @Test
        fun `GIVEN invalid rank WHEN parse THEN returns null`() {
            assertNull(UciMoveParser.parse("e0e4"))
        }
    }

    @Nested
    internal inner class Format {
        @Test
        fun `GIVEN standard move WHEN format THEN returns UCI string`() {
            val move = EngineMove(
                from = Locus.e2,
                to = Locus.e4,
            )

            assertEquals("e2e4", UciMoveParser.format(move))
        }

        @Test
        fun `GIVEN promotion move WHEN format THEN includes promotion piece`() {
            val move = EngineMove(
                from = Locus.a7,
                to = Locus.a8,
                promotion = Piece.Queen,
            )

            assertEquals("a7a8q", UciMoveParser.format(move))
        }

        @Test
        fun `GIVEN knight promotion WHEN format THEN appends n`() {
            val move = EngineMove(
                from = Locus.b7,
                to = Locus.b8,
                promotion = Piece.Knight,
            )

            assertEquals("b7b8n", UciMoveParser.format(move))
        }
    }

    @Nested
    internal inner class RoundTrip {
        @Test
        fun `GIVEN parsed move WHEN formatted THEN matches original string`() {
            val original = "g1f3"
            val parsed = UciMoveParser.parse(original)
            assertNotNull(parsed)
            assertEquals(original, UciMoveParser.format(parsed!!))
        }

        @Test
        fun `GIVEN promotion move WHEN round-tripped THEN matches original`() {
            val original = "a7a8q"
            val parsed = UciMoveParser.parse(original)
            assertNotNull(parsed)
            assertEquals(original, UciMoveParser.format(parsed!!))
        }
    }
}
