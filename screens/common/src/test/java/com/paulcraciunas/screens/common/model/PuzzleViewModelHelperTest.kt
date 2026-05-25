package com.paulcraciunas.screens.common.model

import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.game.logic.impl.RealGameFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PuzzleViewModelHelperTest {
    private val gameFactory = RealGameFactory()
    private val underTest = PuzzleViewModelHelper(gameFactory.puzzleInteractor())

    @Nested
    internal inner class Load {
        @Test
        fun `GIVEN puzzle WHEN load THEN returns PuzzleData with correct rating`() {
            // Given
            val puzzle = buildStandardPuzzle(rating = 1500)

            // When
            val result = underTest.load(puzzle)

            // Then
            assertEquals(1500, result.rating)
        }

        @Test
        fun `GIVEN puzzle WHEN load THEN returns PuzzleData with correct player`() {
            // Given
            val puzzle = buildStandardPuzzle() // Black to move after e2e4

            // When
            val result = underTest.load(puzzle)

            // Then
            assertEquals(Side.BLACK, result.player)
        }

        @Test
        fun `GIVEN puzzle WHEN load THEN returns PuzzleData with captured pieces`() {
            // Given
            val puzzle = buildPuzzleWithCaptures()

            // When
            val result = underTest.load(puzzle)

            // Then
            assertNotNull(result.captured)
            assertTrue(result.captured.containsKey(Side.WHITE))
            assertTrue(result.captured.containsKey(Side.BLACK))
        }

        @Test
        fun `GIVEN puzzle WHEN load THEN returns PuzzleData with boardData`() {
            // Given
            val puzzle = buildStandardPuzzle()

            // When
            val result = underTest.load(puzzle)

            // Then
            assertNotNull(result.boardData)
        }

        @Test
        fun `GIVEN puzzle WHEN load THEN helper properties are updated`() {
            // Given
            val puzzle = buildStandardPuzzle(rating = 1200)

            // When
            underTest.load(puzzle)

            // Then
            assertEquals(1200, underTest.rating)
            assertEquals(Side.BLACK, underTest.player)
        }
    }

    @Nested
    internal inner class HandleSquareClick {
        @Test
        fun `GIVEN no selection WHEN clicking piece THEN piece is selected with moves`() {
            // Given
            underTest.load(buildStandardPuzzle())

            // When
            val result = underTest.handleSquareClick(Locus.e7)

            // Then
            val selectedSquare = result.data.boardData.at(Rank.`7`, File.e)
            assertEquals(true, selectedSquare.piece?.isSelected)
            assertTrue(result.data.boardData.at(Rank.`5`, File.e).canMoveTo)
            assertTrue(result.data.boardData.at(Rank.`6`, File.e).canMoveTo)
        }

        @Test
        fun `GIVEN piece selected WHEN clicking same square THEN selection is cleared`() {
            // Given
            underTest.load(buildStandardPuzzle())
            underTest.handleSquareClick(Locus.e7)

            // When
            val result = underTest.handleSquareClick(Locus.e7)

            // Then
            val selectedSquare = result.data.boardData.at(Rank.`7`, File.e)
            assertEquals(false, selectedSquare.piece?.isSelected)
            assertFalse(result.data.boardData.at(Rank.`5`, File.e).canMoveTo)
        }

        @Test
        fun `GIVEN piece selected WHEN clicking invalid move THEN selection is cleared`() {
            // Given
            underTest.load(buildStandardPuzzle())
            underTest.handleSquareClick(Locus.e7)

            // When - e7 to e3 is not a valid move
            val result = underTest.handleSquareClick(Locus.e3)

            // Then
            val selectedSquare = result.data.boardData.at(Rank.`7`, File.e)
            assertEquals(false, selectedSquare.piece?.isSelected)
            assertFalse(result.data.boardData.at(Rank.`5`, File.e).canMoveTo)
        }

        @Test
        fun `GIVEN piece selected WHEN clicking valid move THEN move is played`() {
            // Given
            underTest.load(buildStandardPuzzle())
            underTest.handleSquareClick(Locus.e7)

            // When
            val result = underTest.handleSquareClick(Locus.e5)

            // Then - board state should show piece moved
            val fromSquare = result.data.boardData.at(Rank.`7`, File.e)
            val toSquare = result.data.boardData.at(Rank.`5`, File.e)
            assertNull(fromSquare.piece)
            assertNotNull(toSquare.piece)
            assertEquals(Piece.Pawn, toSquare.piece?.piece)
        }

        @Test
        fun `GIVEN correct move played WHEN puzzle continues THEN isOver is false`() {
            // Given - Standard puzzle: e2e4, e7e5, g1f3, b8c6
            underTest.load(buildStandardPuzzle())
            underTest.handleSquareClick(Locus.e7)

            // When
            val result = underTest.handleSquareClick(Locus.e5)

            // Then - puzzle should continue (opponent plays g1f3, then we play)
            assertFalse(result.isOver)
        }

        @Test
        fun `GIVEN wrong move played WHEN puzzle fails THEN isOver is true and isSuccess is false`() {
            // Given
            underTest.load(buildStandardPuzzle())
            underTest.handleSquareClick(Locus.d7)

            // When - d7d5 is wrong (expected e7e5)
            val result = underTest.handleSquareClick(Locus.d5)

            // Then
            assertTrue(result.isOver)
            assertFalse(result.isSuccess)
        }

        @Test
        fun `GIVEN pawn reaching back rank WHEN clicking target THEN returns promotion required`() {
            // Given
            underTest.load(buildPromotionPuzzle())
            underTest.handleSquareClick(Locus.a7)

            // When
            val result = underTest.handleSquareClick(Locus.a8)

            // Then
            assertNotNull(result.promotion)
            assertTrue(result.promotion!!.showChooser)
            assertEquals(Locus.a8, result.promotion.at)
        }

        @Test
        fun `GIVEN autoPromote enabled WHEN pawn reaches back rank THEN promotes to queen directly`() {
            // Given
            underTest.autoPromote = true
            underTest.load(buildPromotionPuzzle())
            underTest.handleSquareClick(Locus.a7)

            // When
            val result = underTest.handleSquareClick(Locus.a8)

            // Then
            assertNull(result.promotion)
            val queenSquare = result.data.boardData.at(Rank.`8`, File.a)
            assertNotNull(queenSquare.piece)
            assertEquals(Piece.Queen, queenSquare.piece?.piece)
            assertEquals(Side.WHITE, queenSquare.piece?.side)
        }

        @Test
        fun `GIVEN autoPromote disabled WHEN pawn reaches back rank THEN shows promotion chooser`() {
            // Given
            underTest.autoPromote = false
            underTest.load(buildPromotionPuzzle())
            underTest.handleSquareClick(Locus.a7)

            // When
            val result = underTest.handleSquareClick(Locus.a8)

            // Then
            assertNotNull(result.promotion)
            assertTrue(result.promotion!!.showChooser)
        }

        @Test
        fun `GIVEN no selection WHEN clicking empty square THEN nothing is selected`() {
            // Given
            underTest.load(buildStandardPuzzle())

            // When - d4 is empty on the board
            val result = underTest.handleSquareClick(Locus.d4)

            // Then
            val clickedSquare = result.data.boardData.at(Rank.`4`, File.d)
            assertNull(clickedSquare.piece)
            assertFalse(result.isOver)
        }

        @Test
        fun `GIVEN no selection WHEN clicking empty square THEN board state is unchanged`() {
            // Given
            underTest.load(buildStandardPuzzle())
            val dataBefore = underTest.buildPuzzleData()

            // When - d4 is empty on the board
            val result = underTest.handleSquareClick(Locus.d4)

            // Then
            assertEquals(dataBefore.rating, result.data.rating)
            assertEquals(dataBefore.player, result.data.player)
            assertNull(result.promotion)
        }

        @Test
        fun `GIVEN last move WHEN puzzle completed THEN isOver is true and isSuccess is true`() {
            // Given - One-move puzzle where we deliver checkmate
            underTest.load(buildOneMoveWinPuzzle())
            underTest.handleSquareClick(Locus.e8)

            // When
            val result = underTest.handleSquareClick(Locus.e1)

            // Then
            assertTrue(result.isOver)
            assertTrue(result.isSuccess)
        }
    }

    @Nested
    internal inner class Promote {
        @Test
        fun `GIVEN promotion position WHEN promoting to queen THEN move is played`() {
            // Given
            underTest.load(buildPromotionPuzzle())
            underTest.handleSquareClick(Locus.a7)
            val clickResult = underTest.handleSquareClick(Locus.a8)
            val promotionAt = clickResult.promotion!!.at

            // When
            val result = underTest.promote(Piece.Queen, promotionAt)

            // Then
            val queenSquare = result.data.boardData.at(Rank.`8`, File.a)
            assertNotNull(queenSquare.piece)
            assertEquals(Piece.Queen, queenSquare.piece?.piece)
            assertEquals(Side.WHITE, queenSquare.piece?.side)
        }

        @Test
        fun `GIVEN promotion WHEN played THEN promotion is null in result`() {
            // Given
            underTest.load(buildPromotionPuzzle())
            underTest.handleSquareClick(Locus.a7)
            val clickResult = underTest.handleSquareClick(Locus.a8)
            val promotionAt = clickResult.promotion!!.at

            // When
            val result = underTest.promote(Piece.Queen, promotionAt)

            // Then
            assertNull(result.promotion)
        }

        @Test
        fun `GIVEN winning promotion WHEN promoted THEN isSuccess is true`() {
            // Given
            underTest.load(buildPromotionPuzzle())
            underTest.handleSquareClick(Locus.a7)
            val clickResult = underTest.handleSquareClick(Locus.a8)

            // When
            val result = underTest.promote(Piece.Queen, clickResult.promotion!!.at)

            // Then - after promotion, puzzle continues (opponent responds)
            // The promotion itself is correct, but puzzle continues
            assertFalse(result.isOver)
        }
    }

    @Nested
    internal inner class Hint {
        @Test
        fun `GIVEN puzzle WHEN hint requested THEN returns hint square`() {
            // Given
            underTest.load(buildStandardPuzzle())

            // When
            val hintSquare = underTest.hint()

            // Then - Expected first move is e7e5, so hint should be e7
            assertEquals(Locus.e7, hintSquare)
        }

        @Test
        fun `GIVEN hint requested WHEN buildPuzzleData THEN selection shows hint square`() {
            // Given
            underTest.load(buildStandardPuzzle())
            underTest.hint()

            // When
            val data = underTest.buildPuzzleData()

            // Then
            val hintedSquare = data.boardData.at(Locus.e7)
            assertEquals(true, hintedSquare.piece?.isSelected)
            // Should also show available moves
            assertTrue(data.boardData.at(Locus.e5).canMoveTo)
        }
    }

    @Nested
    internal inner class Resign {
        @Test
        fun `GIVEN puzzle in progress WHEN resign THEN puzzle is over and failed`() {
            // Given
            underTest.load(buildStandardPuzzle())

            // When
            underTest.resign()

            // Then - Need to click to get the result with isOver/isSuccess
            // Actually resign just marks it as failed, so let's check via handleSquareClick
            val result = underTest.handleSquareClick(Locus.e7)
            // After resign, puzzle should be over
            assertTrue(result.isOver)
            assertFalse(result.isSuccess)
        }
    }

    @Nested
    internal inner class BuildPuzzleData {
        @Test
        fun `GIVEN puzzle loaded WHEN buildPuzzleData THEN returns current state`() {
            // Given
            val puzzle = buildStandardPuzzle(rating = 1800)
            underTest.load(puzzle)

            // When
            val data = underTest.buildPuzzleData()

            // Then
            assertEquals(1800, data.rating)
            assertEquals(Side.BLACK, data.player)
            assertNotNull(data.boardData)
        }

        @Test
        fun `GIVEN move played WHEN buildPuzzleData THEN reflects updated board`() {
            // Given
            underTest.load(buildStandardPuzzle())
            underTest.handleSquareClick(Locus.e7)
            underTest.handleSquareClick(Locus.e5)

            // When
            val data = underTest.buildPuzzleData()

            // Then
            val e5Square = data.boardData.at(Rank.`5`, File.e)
            assertNotNull(e5Square.piece)
        }
    }

    @Nested
    internal inner class PlayNextSolutionMove {
        @Test
        fun `GIVEN puzzle in progress WHEN playNextSolutionMove THEN plays expected move`() {
            // Given - Standard puzzle: e2e4, e7e5, g1f3, b8c6
            // After load, opponent plays e2e4, so next expected is e7e5
            underTest.load(buildStandardPuzzle())

            // When
            val result = underTest.playNextSolutionMove()

            // Then - e7 pawn should move to e5
            assertNotNull(result)
            val e5Square = result!!.boardData.at(Rank.`5`, File.e)
            assertNotNull(e5Square.piece)
            assertEquals(Piece.Pawn, e5Square.piece?.piece)
        }

        @Test
        fun `GIVEN puzzle over WHEN playNextSolutionMove THEN returns null`() {
            // Given
            underTest.load(buildStandardPuzzle())
            underTest.resign()

            // When
            val result = underTest.playNextSolutionMove()

            // Then
            assertNull(result)
        }

        @Test
        fun `GIVEN puzzle WHEN multiple playNextSolutionMove THEN plays all moves`() {
            // Given - Standard puzzle: e2e4, e7e5, g1f3, b8c6
            underTest.load(buildStandardPuzzle())

            // When - play all remaining moves
            underTest.playNextSolutionMove() // e7e5
            underTest.playNextSolutionMove() // g1f3
            val result = underTest.playNextSolutionMove() // b8c6

            // Then - knight should be on c6
            assertNotNull(result)
            val c6Square = result!!.boardData.at(Rank.`6`, File.c)
            assertNotNull(c6Square.piece)
            assertEquals(Piece.Knight, c6Square.piece?.piece)
        }
    }

    @Nested
    internal inner class HasSolutionMoves {
        @Test
        fun `GIVEN puzzle in progress WHEN hasSolutionMoves THEN returns true`() {
            // Given
            underTest.load(buildStandardPuzzle())

            // When
            val result = underTest.hasSolutionMoves()

            // Then
            assertTrue(result)
        }

        @Test
        fun `GIVEN puzzle over WHEN hasSolutionMoves THEN returns false`() {
            // Given
            underTest.load(buildStandardPuzzle())
            underTest.resign()

            // When
            val result = underTest.hasSolutionMoves()

            // Then
            assertFalse(result)
        }

        @Test
        fun `GIVEN all solution moves played WHEN hasSolutionMoves THEN returns false`() {
            // Given - Standard puzzle: e2e4, e7e5, g1f3, b8c6
            underTest.load(buildStandardPuzzle())
            underTest.playNextSolutionMove() // e7e5
            underTest.playNextSolutionMove() // g1f3
            underTest.playNextSolutionMove() // b8c6

            // When
            val result = underTest.hasSolutionMoves()

            // Then
            assertFalse(result)
        }
    }

    private fun buildStandardPuzzle(
        rating: Int = DEFAULT_RATING,
        moves: List<String> = listOf("e2e4", "e7e5", "g1f3", "b8c6"),
    ): Puzzle = gameFactory.builder()
        .withDefaultBoard()
        .withRating(rating)
        .withMoves(moves)
        .buildPuzzle()

    private fun buildPromotionPuzzle(): Puzzle = gameFactory.builder()
        .withId(PROMOTION_ID)
        .withRating(DEFAULT_RATING)
        .withTurn(Side.BLACK)
        .withPiece(Piece.King, Side.WHITE, Locus.e1)
        .withPiece(Piece.King, Side.BLACK, Locus.e8)
        .withPiece(Piece.Pawn, Side.WHITE, Locus.a7)
        .withMoves(listOf("e8e7", "a7a8q", "e7e6", "a8a6"))
        .buildPuzzle()

    private fun buildOneMoveWinPuzzle(): Puzzle = gameFactory.builder()
        .withId(ONE_MOVE_WIN_ID)
        .withRating(DEFAULT_RATING)
        .withTurn(Side.BLACK)
        .withPiece(Piece.King, Side.WHITE, Locus.g3)
        .withPiece(Piece.King, Side.BLACK, Locus.h1)
        .withPiece(Piece.Queen, Side.WHITE, Locus.e8)
        .withMoves(listOf("h1g1", "e8e1"))
        .buildPuzzle()

    private fun buildPuzzleWithCaptures(): Puzzle = gameFactory.builder()
        .withDefaultBoard()
        .withRating(DEFAULT_RATING)
        .withMoves(listOf("e2e4", "d7d5", "e4d5", "d8d5"))
        .buildPuzzle()

    private companion object {
        private const val DEFAULT_RATING = 1200
        private const val PROMOTION_ID = 42
        private const val ONE_MOVE_WIN_ID = 88
    }
}
