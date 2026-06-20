package com.paulcraciunas.screens.data

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.SidedPiece
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.logic.builders.Builders
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class BoardViewDataTest {
    private val gameFactory = RealGameFactory()

    @Nested
    internal inner class Empty {
        @Test
        fun `GIVEN empty board WHEN created THEN has 64 squares`() {
            val result = BoardViewData.empty()

            assertEquals(64, result.squares.size)
        }

        @Test
        fun `GIVEN empty board WHEN created THEN all squares have null piece`() {
            val result = BoardViewData.empty()

            result.squares.forEach { square ->
                assertNull(square?.piece)
            }
        }

        @Test
        fun `GIVEN empty board WHEN created THEN has no selection`() {
            val result = BoardViewData.empty()

            assertNull(result.selection)
        }

        @Test
        fun `GIVEN empty board WHEN created THEN has no available moves`() {
            val result = BoardViewData.empty()

            assertTrue(result.availableMoves.isEmpty())
        }

        @Test
        fun `GIVEN empty board WHEN created THEN has no animating piece`() {
            val result = BoardViewData.empty()

            assertNull(result.animatingPiece)
        }
    }

    @Nested
    internal inner class Default {
        @Test
        fun `GIVEN default board WHEN created THEN has 64 squares`() {
            val result = BoardViewData.default()

            assertEquals(64, result.squares.size)
        }

        @Test
        fun `GIVEN default board WHEN created THEN white pawns are on rank 2`() {
            val result = BoardViewData.default()
            val whitePawn = SidedPiece.of(Side.WHITE, Piece.Pawn)

            listOf(Locus.a2, Locus.b2, Locus.c2, Locus.d2, Locus.e2, Locus.f2, Locus.g2, Locus.h2)
                .forEach { locus ->
                    assertEquals(whitePawn, result.at(locus))
                }
        }

        @Test
        fun `GIVEN default board WHEN created THEN black pawns are on rank 7`() {
            val result = BoardViewData.default()
            val blackPawn = SidedPiece.of(Side.BLACK, Piece.Pawn)

            listOf(Locus.a7, Locus.b7, Locus.c7, Locus.d7, Locus.e7, Locus.f7, Locus.g7, Locus.h7)
                .forEach { locus ->
                    assertEquals(blackPawn, result.at(locus))
                }
        }

        @Test
        fun `GIVEN default board WHEN created THEN white rooks are in corners`() {
            val result = BoardViewData.default()
            val whiteRook = SidedPiece.of(Side.WHITE, Piece.Rook)

            assertEquals(whiteRook, result.at(Locus.a1))
            assertEquals(whiteRook, result.at(Locus.h1))
        }

        @Test
        fun `GIVEN default board WHEN created THEN black rooks are in corners`() {
            val result = BoardViewData.default()
            val blackRook = SidedPiece.of(Side.BLACK, Piece.Rook)

            assertEquals(blackRook, result.at(Locus.a8))
            assertEquals(blackRook, result.at(Locus.h8))
        }

        @Test
        fun `GIVEN default board WHEN created THEN white king is on e1`() {
            val result = BoardViewData.default()

            assertEquals(SidedPiece.of(Side.WHITE, Piece.King), result.at(Locus.e1))
        }

        @Test
        fun `GIVEN default board WHEN created THEN black king is on e8`() {
            val result = BoardViewData.default()

            assertEquals(SidedPiece.of(Side.BLACK, Piece.King), result.at(Locus.e8))
        }

        @Test
        fun `GIVEN default board WHEN created THEN white queen is on d1`() {
            val result = BoardViewData.default()

            assertEquals(SidedPiece.of(Side.WHITE, Piece.Queen), result.at(Locus.d1))
        }

        @Test
        fun `GIVEN default board WHEN created THEN black queen is on d8`() {
            val result = BoardViewData.default()

            assertEquals(SidedPiece.of(Side.BLACK, Piece.Queen), result.at(Locus.d8))
        }

        @Test
        fun `GIVEN default board WHEN created THEN white knights are on b1 and g1`() {
            val result = BoardViewData.default()
            val whiteKnight = SidedPiece.of(Side.WHITE, Piece.Knight)

            assertEquals(whiteKnight, result.at(Locus.b1))
            assertEquals(whiteKnight, result.at(Locus.g1))
        }

        @Test
        fun `GIVEN default board WHEN created THEN white bishops are on c1 and f1`() {
            val result = BoardViewData.default()
            val whiteBishop = SidedPiece.of(Side.WHITE, Piece.Bishop)

            assertEquals(whiteBishop, result.at(Locus.c1))
            assertEquals(whiteBishop, result.at(Locus.f1))
        }

        @Test
        fun `GIVEN default board WHEN created THEN ranks 3 through 6 are empty`() {
            val result = BoardViewData.default()
            val emptyRankLoci = listOf(
                Locus.a3, Locus.b3, Locus.c3, Locus.d3, Locus.e3, Locus.f3, Locus.g3, Locus.h3,
                Locus.a4, Locus.b4, Locus.c4, Locus.d4, Locus.e4, Locus.f4, Locus.g4, Locus.h4,
                Locus.a5, Locus.b5, Locus.c5, Locus.d5, Locus.e5, Locus.f5, Locus.g5, Locus.h5,
                Locus.a6, Locus.b6, Locus.c6, Locus.d6, Locus.e6, Locus.f6, Locus.g6, Locus.h6,
            )

            emptyRankLoci.forEach { locus ->
                assertNull(result.at(locus))
            }
        }
    }

    @Nested
    internal inner class FromBoard {
        @Test
        fun `GIVEN default board WHEN from is called THEN pieces are loaded correctly`() {
            val board = defaultBoard()

            val result = BoardViewData.from(board = board)

            assertEquals(SidedPiece.of(Side.WHITE, Piece.King), result.at(Locus.e1))
            assertEquals(SidedPiece.of(Side.BLACK, Piece.King), result.at(Locus.e8))
            assertNull(result.at(Locus.e4))
        }

        @Test
        fun `GIVEN board after e2e4 WHEN from is called THEN pawn is on e4`() {
            val board = boardAfterMoves(Locus.e2 to Locus.e4)

            val result = BoardViewData.from(board = board)

            assertEquals(SidedPiece.of(Side.WHITE, Piece.Pawn), result.at(Locus.e4))
            assertNull(result.at(Locus.e2))
        }

        @Test
        fun `GIVEN board WHEN from is called without lastMove THEN no animating piece`() {
            val board = defaultBoard()

            val result = BoardViewData.from(board = board)

            assertNull(result.animatingPiece)
        }
    }

    @Nested
    internal inner class FromBoardWithLastMove {
        @Test
        fun `GIVEN board after move WHEN from with lastMove without animation THEN no animating piece`() {
            val board = boardAfterMoves(Locus.e2 to Locus.e4)
            val lastMove = Locus.e2 to Locus.e4

            val result = BoardViewData.from(
                board = board,
                lastMove = lastMove,
                withAnimation = false,
            )

            assertNull(result.animatingPiece)
        }
    }

    @Nested
    internal inner class FromBoardWithAnimation {
        @Test
        fun `GIVEN board after move WHEN from with animation THEN animating piece is set`() {
            val board = boardAfterMoves(Locus.e2 to Locus.e4)
            val lastMove = Locus.e2 to Locus.e4

            val result = BoardViewData.from(
                board = board,
                lastMove = lastMove,
                withAnimation = true,
            )

            assertNotNull(result.animatingPiece)
        }

        @Test
        fun `GIVEN board after move WHEN from with animation THEN animating piece has correct from`() {
            val board = boardAfterMoves(Locus.e2 to Locus.e4)
            val lastMove = Locus.e2 to Locus.e4

            val result = BoardViewData.from(
                board = board,
                lastMove = lastMove,
                withAnimation = true,
            )

            assertEquals(Locus.e2, result.lastMove?.first)
        }

        @Test
        fun `GIVEN board after move WHEN from with animation THEN animating piece has correct to`() {
            val board = boardAfterMoves(Locus.e2 to Locus.e4)
            val lastMove = Locus.e2 to Locus.e4

            val result = BoardViewData.from(
                board = board,
                lastMove = lastMove,
                withAnimation = true,
            )

            assertEquals(Locus.e4, result.lastMove?.second)
        }

        @Test
        fun `GIVEN board after move WHEN from with animation THEN animating piece has moved piece`() {
            val board = boardAfterMoves(Locus.e2 to Locus.e4)
            val lastMove = Locus.e2 to Locus.e4

            val result = BoardViewData.from(
                board = board,
                lastMove = lastMove,
                withAnimation = true,
            )

            assertEquals(SidedPiece.of(Side.WHITE, Piece.Pawn), result.animatingPiece)
        }

        @Test
        fun `GIVEN board after move WHEN from without animation THEN no animating piece`() {
            val board = boardAfterMoves(Locus.e2 to Locus.e4)
            val lastMove = Locus.e2 to Locus.e4

            val result = BoardViewData.from(
                board = board,
                lastMove = lastMove,
                withAnimation = false,
            )

            assertNull(result.animatingPiece)
        }
    }

    @Nested
    internal inner class Select {
        @Test
        fun `GIVEN default board WHEN select piece THEN selection is set`() {
            val board = BoardViewData.default()
            val moves = listOf(Locus.e3, Locus.e4)

            val result = board.select(at = Locus.e2, moves = moves)

            assertEquals(Locus.e2, result.selection)
        }

        @Test
        fun `GIVEN default board WHEN select piece THEN available moves are set`() {
            val board = BoardViewData.default()
            val moves = listOf(Locus.e3, Locus.e4)

            val result = board.select(at = Locus.e2, moves = moves)

            assertEquals(moves, result.availableMoves)
        }
    }

    @Nested
    internal inner class SelectEmptySquare {
        @Test
        fun `GIVEN empty board WHEN select empty square THEN returns same instance`() {
            val board = BoardViewData.empty()

            val result = board.select(at = Locus.e4, moves = listOf(Locus.e5))

            assertSame(board, result)
        }

        @Test
        fun `GIVEN default board WHEN select empty square THEN returns same instance`() {
            val board = BoardViewData.default()

            val result = board.select(at = Locus.e4, moves = listOf(Locus.e5))

            assertSame(board, result)
        }

        @Test
        fun `GIVEN default board WHEN select empty square THEN selection stays null`() {
            val board = BoardViewData.default()

            val result = board.select(at = Locus.e4, moves = listOf(Locus.e5))

            assertNull(result.selection)
        }
    }

    @Nested
    internal inner class ClearSelection {
        @Test
        fun `GIVEN selected piece WHEN clearSelection THEN selection is null`() {
            val board = BoardViewData.default()
                .select(at = Locus.e2, moves = listOf(Locus.e3, Locus.e4))

            val result = board.clearSelection()

            assertNull(result.selection)
        }

        @Test
        fun `GIVEN selected piece WHEN clearSelection THEN available moves are empty`() {
            val board = BoardViewData.default()
                .select(at = Locus.e2, moves = listOf(Locus.e3, Locus.e4))

            val result = board.clearSelection()

            assertTrue(result.availableMoves.isEmpty())
        }
    }

    @Nested
    internal inner class ClearSelectionWhenNothingSelected {
        @Test
        fun `GIVEN no selection WHEN clearSelection THEN returns same instance`() {
            val board = BoardViewData.default()

            val result = board.clearSelection()

            assertSame(board, result)
        }

        @Test
        fun `GIVEN empty board WHEN clearSelection THEN returns same instance`() {
            val board = BoardViewData.empty()

            val result = board.clearSelection()

            assertSame(board, result)
        }
    }

    @Nested
    internal inner class At {
        @Test
        fun `GIVEN default board WHEN at a1 THEN returns white rook`() {
            val board = BoardViewData.default()

            val result = board.at(Locus.a1)

            assertEquals(SidedPiece.of(Side.WHITE, Piece.Rook), result)
        }

        @Test
        fun `GIVEN default board WHEN at h8 THEN returns black rook`() {
            val board = BoardViewData.default()

            val result = board.at(Locus.h8)

            assertEquals(SidedPiece.of(Side.BLACK, Piece.Rook), result)
        }

        @Test
        fun `GIVEN empty board WHEN at e4 THEN returns square with null piece`() {
            val board = BoardViewData.empty()

            val result = board.at(Locus.e4)

            assertNull(result?.piece)
        }
    }

    private fun defaultBoard() = Builders.boardFactory().defaultBoard()

    private fun boardAfterMoves(vararg moves: Pair<Locus, Locus>) =
        gameFactory.builder().withDefaultBoard().buildGame().run {
            start()
            moves.forEach { (from, to) -> play(from, to) }
            board
        }
}
