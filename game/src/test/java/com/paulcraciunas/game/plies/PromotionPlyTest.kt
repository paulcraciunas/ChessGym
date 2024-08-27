package com.paulcraciunas.game.plies

import com.paulcraciunas.game.Side
import com.paulcraciunas.game.board.Board
import com.paulcraciunas.game.board.Piece
import com.paulcraciunas.game.loc
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PromotionPlyTest {
    private val on = Board()

    @Test
    fun `WHEN executing a promotion THEN the pawn is removed and desired piece is added`() {
        val ply = PromotionPly(
            turn = Side.WHITE,
            from = "c7".loc(),
            to = "c8".loc(),
            captured = null,
            resultingPiece = Piece.Queen
        )
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = ply.from)

        ply.exec(on)

        assertTrue(on.has(Piece.Queen, Side.WHITE, ply.to))
        assertTrue(on.isEmpty(ply.from))
        assertTrue(ply.isPawnMoveOrCapture())
        assertNull(ply.captured())
    }

    @Test
    fun `WHEN executing a promotion for black THEN the pawn is removed and desired piece is added`() {
        val ply = PromotionPly(
            turn = Side.BLACK,
            from = "c2".loc(),
            to = "c1".loc(),
            captured = null,
            resultingPiece = Piece.Queen
        )
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = ply.from)

        ply.exec(on)

        assertTrue(on.has(Piece.Queen, Side.BLACK, ply.to))
        assertTrue(on.isEmpty(ply.from))
        assertTrue(ply.isPawnMoveOrCapture())
    }

    @Test
    fun `GIVEN a piece to capture WHEN executing THEN captured piece is removed`() {
        val ply = PromotionPly(
            turn = Side.WHITE,
            from = "c7".loc(),
            to = "b8".loc(),
            captured = Piece.Bishop,
            resultingPiece = Piece.Queen
        )
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = ply.from)
        on.add(piece = Piece.Bishop, side = Side.BLACK, at = ply.to)

        ply.exec(on)

        assertTrue(on.has(Piece.Queen, Side.WHITE, ply.to))
        assertTrue(on.isEmpty(ply.from))
        assertTrue(ply.isPawnMoveOrCapture())
    }

    @Test
    fun `GIVEN a piece to capture for black WHEN executing THEN captured piece is removed`() {
        val ply = PromotionPly(
            turn = Side.BLACK,
            from = "c2".loc(),
            to = "b1".loc(),
            captured = Piece.Bishop,
            resultingPiece = Piece.Queen
        )
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = ply.from)
        on.add(piece = Piece.Bishop, side = Side.WHITE, at = ply.to)

        ply.exec(on)

        assertTrue(on.has(Piece.Queen, Side.BLACK, ply.to))
        assertTrue(on.isEmpty(ply.from))
        assertTrue(ply.isPawnMoveOrCapture())
    }

    @Test
    fun `GIVEN no promotion selected WHEN executing a promotion THEN assume a Queen`() {
        val ply = PromotionPly(
            turn = Side.BLACK,
            from = "c2".loc(),
            to = "b1".loc(),
            captured = Piece.Bishop,
        )
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = ply.from)
        on.add(piece = Piece.Bishop, side = Side.WHITE, at = ply.to)

        ply.exec(on)

        assertTrue(on.has(Piece.Queen, Side.BLACK, ply.to))
        assertTrue(on.isEmpty(ply.from))
        assertTrue(ply.isPawnMoveOrCapture())
        assertNotNull(ply.captured())
    }

    @Test
    fun `WHEN pawn is not near promotion THEN exec throws`() {
        val ply = PromotionPly(
            turn = Side.WHITE,
            from = "c6".loc(),
            to = "c8".loc(),
            captured = null,
            resultingPiece = Piece.Queen
        )
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = ply.from)

        assertThrows<AssertionError> { ply.exec(on) }
    }

    @Test
    fun `WHEN black pawn is not near promotion THEN exec throws`() {
        val ply = PromotionPly(
            turn = Side.BLACK,
            from = "c3".loc(),
            to = "c1".loc(),
            captured = null,
            resultingPiece = Piece.Queen
        )
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = ply.from)

        assertThrows<AssertionError> { ply.exec(on) }
    }

    @Test
    fun `WHEN pawn is not promoting THEN exec throws`() {
        val ply = PromotionPly(
            turn = Side.WHITE,
            from = "c6".loc(),
            to = "c6".loc(),
            captured = null,
            resultingPiece = Piece.Queen
        )
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = ply.from)

        assertThrows<AssertionError> { ply.exec(on) }
    }

    @Test
    fun `GIVEN no promotion selected WHEN accepting THEN ply executes correctly`() {
        val ply = PromotionPly(
            turn = Side.WHITE,
            from = "c7".loc(),
            to = "c8".loc(),
            captured = null,
        )
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = ply.from)

        ply.accept(Piece.Queen)
        ply.exec(on)

        assertTrue(on.has(Piece.Queen, Side.WHITE, ply.to))
        assertTrue(on.isEmpty(ply.from))
    }

    @Test
    fun `WHEN undoing a promotion THEN the desired piece is removed and the pawn is added back`() {
        val ply = PromotionPly(
            turn = Side.WHITE,
            from = "c7".loc(),
            to = "c8".loc(),
            captured = null,
            resultingPiece = Piece.Queen
        )
        on.add(piece = Piece.Queen, side = Side.WHITE, at = ply.to)

        ply.undo(on)

        assertTrue(on.has(Piece.Pawn, Side.WHITE, ply.from))
        assertTrue(on.isEmpty(ply.to))
    }

    @Test
    fun `WHEN undoing a capture promotion THEN the captured piece is added back`() {
        val ply = PromotionPly(
            turn = Side.WHITE,
            from = "c7".loc(),
            to = "b8".loc(),
            captured = Piece.Bishop,
            resultingPiece = Piece.Queen
        )
        on.add(piece = Piece.Queen, side = Side.WHITE, at = ply.to)

        ply.undo(on)

        assertTrue(on.has(Piece.Pawn, Side.WHITE, ply.from))
        assertTrue(on.has(Piece.Bishop, Side.BLACK, ply.to))
    }

    @Test
    fun `WHEN promoting to a Pawn or King THEN throw`() {
        val ply = PromotionPly(
            turn = Side.WHITE,
            from = "c7".loc(),
            to = "b8".loc(),
            captured = Piece.Bishop,
        )

        assertThrows<AssertionError> {
            ply.accept(Piece.Pawn)
        }
        assertThrows<AssertionError> {
            ply.accept(Piece.King)
        }
    }

    @Test
    fun `WHEN serializing to algebraic notation THEN return correct string`() {
        assertEquals(
            "c8=Q",
            PromotionPly(
                Side.WHITE,
                "c7".loc(),
                "c8".loc(),
                resultingPiece = Piece.Queen
            ).algebraic()
        )
        assertEquals(
            "cxb1=B",
            PromotionPly(
                turn = Side.BLACK,
                from = "c2".loc(),
                to = "b1".loc(),
                captured = Piece.Rook,
                resultingPiece = Piece.Bishop
            ).algebraic()
        )
    }
}
