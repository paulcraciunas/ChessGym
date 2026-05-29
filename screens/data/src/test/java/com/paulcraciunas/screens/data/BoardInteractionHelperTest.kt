package com.paulcraciunas.screens.data

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.RealGameFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class BoardInteractionHelperTest {
    private val gameFactory = RealGameFactory()

    @Nested
    internal inner class LoadWithGame {
        private val underTest = BoardInteractionHelper()

        @Test
        fun `GIVEN game WHEN load THEN returns PlayableData with correct player`() {
            val playable = GamePlayableBoard(buildDefaultGame(), Side.WHITE)
            val result = underTest.load(playable)
            assertEquals(Side.WHITE, result.player)
        }

        @Test
        fun `GIVEN game WHEN load as black THEN returns PlayableData with black player`() {
            val playable = GamePlayableBoard(buildDefaultGame(), Side.BLACK)
            val result = underTest.load(playable)
            assertEquals(Side.BLACK, result.player)
        }

        @Test
        fun `GIVEN game WHEN load THEN returns PlayableData with no captured pieces`() {
            val playable = GamePlayableBoard(buildDefaultGame(), Side.WHITE)
            val result = underTest.load(playable)
            assertEquals("", result.captured.byPlayer)
            assertEquals("", result.captured.byOpponent)
        }

        @Test
        fun `GIVEN game WHEN load THEN returns PlayableData with boardData`() {
            val playable = GamePlayableBoard(buildDefaultGame(), Side.WHITE)
            val result = underTest.load(playable)
            assertNotNull(result.boardData)
        }

        @Test
        fun `GIVEN game WHEN load THEN isLoaded is true`() {
            val playable = GamePlayableBoard(buildDefaultGame(), Side.WHITE)
            underTest.load(playable)
            assertTrue(underTest.isLoaded())
        }

        @Test
        fun `GIVEN game with rating WHEN load THEN rating is accessible`() {
            val game = gameFactory.builder()
                .withDefaultBoard()
                .withRating(1600)
                .buildGame()
            val playable = GamePlayableBoard(game, Side.WHITE)
            underTest.load(playable)
            assertEquals(1600, underTest.current().rating)
        }

        @Test
        fun `GIVEN game WHEN load THEN id is null`() {
            val playable = GamePlayableBoard(buildDefaultGame(), Side.WHITE)
            val result = underTest.load(playable)
            assertNull(result.id)
        }
    }

    @Nested
    internal inner class LoadWithPuzzle {
        private val underTest = BoardInteractionHelper()

        @Test
        fun `GIVEN puzzle WHEN load THEN returns PlayableData with correct player`() {
            val playable = PuzzlePlayableBoard(buildStandardPuzzle())
            val result = underTest.load(playable)
            assertEquals(Side.BLACK, result.player)
        }

        @Test
        fun `GIVEN puzzle WHEN load THEN returns PlayableData with correct rating`() {
            val playable = PuzzlePlayableBoard(buildStandardPuzzle(rating = 1500))
            val result = underTest.load(playable)
            assertEquals(1500, result.rating)
        }

        @Test
        fun `GIVEN puzzle WHEN load THEN id is accessible`() {
            val puzzle = gameFactory.builder()
                .withId(42)
                .withDefaultBoard()
                .withRating(1200)
                .withMoves(listOf("e2e4", "e7e5", "g1f3", "b8c6"))
                .buildPuzzle()
            val playable = PuzzlePlayableBoard(puzzle)
            underTest.load(playable)
            assertEquals(42, underTest.current().id)
        }
    }

    @Nested
    internal inner class HandleSquareClickWithGame {
        private val underTest = BoardInteractionHelper()

        @Test
        fun `GIVEN no selection WHEN clicking own piece THEN piece is selected with moves`() {
            underTest.load(GamePlayableBoard(buildDefaultGame(), Side.WHITE))

            val result = underTest.handleSquareClick(Locus.e2)

            val selectedSquare = result.data.boardData.at(Locus.e2)
            assertEquals(true, selectedSquare.piece?.isSelected)
            assertTrue(result.data.boardData.availableMoves.isNotEmpty())
        }

        @Test
        fun `GIVEN no selection WHEN clicking opponent piece THEN piece is not selected`() {
            underTest.load(GamePlayableBoard(buildDefaultGame(), Side.WHITE))

            val result = underTest.handleSquareClick(Locus.e7)

            assertNull(result.data.boardData.selection)
            assertTrue(result.data.boardData.availableMoves.isEmpty())
            assertFalse(result.movePlayed)
        }

        @Test
        fun `GIVEN playing as black WHEN clicking active side piece THEN piece is selected`() {
            underTest.load(GamePlayableBoard(buildDefaultGame(), Side.BLACK))

            val result = underTest.handleSquareClick(Locus.e2)

            assertEquals(Locus.e2, result.data.boardData.selection)
            assertTrue(result.data.boardData.availableMoves.isNotEmpty())
            assertFalse(result.movePlayed)
        }

        @Test
        fun `GIVEN piece selected WHEN clicking same square THEN selection is cleared`() {
            underTest.load(GamePlayableBoard(buildDefaultGame(), Side.WHITE))
            underTest.handleSquareClick(Locus.e2)

            val result = underTest.handleSquareClick(Locus.e2)

            assertNull(result.data.boardData.selection)
            assertFalse(result.movePlayed)
        }

        @Test
        fun `GIVEN piece selected WHEN clicking invalid move THEN selection is cleared`() {
            underTest.load(GamePlayableBoard(buildDefaultGame(), Side.WHITE))
            underTest.handleSquareClick(Locus.e2)

            val result = underTest.handleSquareClick(Locus.e5)

            assertNull(result.data.boardData.selection)
            assertFalse(result.movePlayed)
        }

        @Test
        fun `GIVEN piece selected WHEN clicking valid move THEN move is played`() {
            underTest.load(GamePlayableBoard(buildDefaultGame(), Side.WHITE))
            underTest.handleSquareClick(Locus.e2)

            val result = underTest.handleSquareClick(Locus.e4)

            assertNull(result.data.boardData.at(Locus.e2).piece)
            assertNotNull(result.data.boardData.at(Locus.e4).piece)
            assertEquals(Piece.Pawn, result.data.boardData.at(Locus.e4).piece?.piece?.piece)
            assertTrue(result.movePlayed)
        }

        @Test
        fun `GIVEN game WHEN move played THEN last move is highlighted`() {
            underTest.load(GamePlayableBoard(buildDefaultGame(), Side.WHITE))
            underTest.handleSquareClick(Locus.e2)

            val result = underTest.handleSquareClick(Locus.e4)

            assertTrue(result.data.boardData.at(Locus.e2).lastMove)
            assertTrue(result.data.boardData.at(Locus.e4).lastMove)
        }

        @Test
        fun `GIVEN game WHEN move played THEN animating piece is set`() {
            underTest.load(GamePlayableBoard(buildDefaultGame(), Side.WHITE))
            underTest.handleSquareClick(Locus.e2)

            val result = underTest.handleSquareClick(Locus.e4)

            val animating = result.data.boardData.animatingPiece
            assertNotNull(animating)
            assertEquals(Piece.Pawn, animating!!.piece.piece)
            assertEquals(Side.WHITE, animating.piece.side)
            assertEquals(Locus.e2, animating.from)
            assertEquals(Locus.e4, animating.to)
        }

        @Test
        fun `GIVEN no selection WHEN clicking empty square THEN nothing is selected`() {
            underTest.load(GamePlayableBoard(buildDefaultGame(), Side.WHITE))

            val result = underTest.handleSquareClick(Locus.e4)

            assertNull(result.data.boardData.at(Locus.e4).piece)
            assertFalse(result.movePlayed)
        }

        @Test
        fun `GIVEN game not over WHEN move played THEN isOver is false`() {
            underTest.load(GamePlayableBoard(buildDefaultGame(), Side.WHITE))
            underTest.handleSquareClick(Locus.e2)

            val result = underTest.handleSquareClick(Locus.e4)

            assertFalse(result.data.isOver)
        }

        @Test
        fun `GIVEN checkmate position WHEN delivering checkmate THEN isOver is true`() {
            val game = gameFactory.builder()
                .withTurn(Side.WHITE)
                .withPiece(Piece.King, Side.WHITE, Locus.g1)
                .withPiece(Piece.King, Side.BLACK, Locus.h8)
                .withPiece(Piece.Rook, Side.WHITE, Locus.g7)
                .withPiece(Piece.Queen, Side.WHITE, Locus.f6)
                .buildGame()
            underTest.load(GamePlayableBoard(game, Side.WHITE))
            underTest.handleSquareClick(Locus.f6)

            val result = underTest.handleSquareClick(Locus.f8)

            assertTrue(result.data.isOver)
        }
    }

    @Nested
    internal inner class HandleSquareClickWithPuzzle {
        private val underTest = BoardInteractionHelper()

        @Test
        fun `GIVEN puzzle WHEN clicking own piece THEN piece is selected`() {
            underTest.load(PuzzlePlayableBoard(buildStandardPuzzle()))

            val result = underTest.handleSquareClick(Locus.e7)

            assertEquals(true, result.data.boardData.at(Locus.e7).piece?.isSelected)
            assertTrue(result.data.boardData.availableMoves.isNotEmpty())
        }

        @Test
        fun `GIVEN puzzle WHEN clicking opponent piece THEN piece is not selected`() {
            underTest.load(PuzzlePlayableBoard(buildStandardPuzzle()))

            val result = underTest.handleSquareClick(Locus.e4)

            assertNull(result.data.boardData.selection)
            assertTrue(result.data.boardData.availableMoves.isEmpty())
        }

        @Test
        fun `GIVEN puzzle WHEN correct move played THEN opponent auto-responds`() {
            underTest.load(PuzzlePlayableBoard(buildStandardPuzzle()))
            underTest.handleSquareClick(Locus.e7)

            val result = underTest.handleSquareClick(Locus.e5)

            assertNotNull(result.data.boardData.at(Locus.e5).piece)
            assertFalse(result.data.isOver)
        }

        @Test
        fun `GIVEN puzzle WHEN wrong move played THEN isOver is true`() {
            underTest.load(PuzzlePlayableBoard(buildStandardPuzzle()))
            underTest.handleSquareClick(Locus.d7)

            val result = underTest.handleSquareClick(Locus.d5)

            assertTrue(result.data.isOver)
        }
    }

    @Nested
    internal inner class PromoteWithGame {
        private val underTest = BoardInteractionHelper()

        @Test
        fun `GIVEN pawn reaching back rank WHEN clicking target THEN returns promotion required`() {
            underTest.load(GamePlayableBoard(buildPromotionGame(), Side.WHITE))
            underTest.handleSquareClick(Locus.a7)

            val result = underTest.handleSquareClick(Locus.a8)

            assertNotNull(result.promotion)
            assertTrue(result.promotion!!.showChooser)
            assertEquals(Locus.a8, result.promotion.at)
        }

        @Test
        fun `GIVEN autoPromote enabled WHEN pawn reaches back rank THEN promotes to queen directly`() {
            underTest.autoPromote = true
            underTest.load(GamePlayableBoard(buildPromotionGame(), Side.WHITE))
            underTest.handleSquareClick(Locus.a7)

            val result = underTest.handleSquareClick(Locus.a8)

            assertNull(result.promotion)
            assertTrue(result.movePlayed)
            val queenSquare = result.data.boardData.at(Locus.a8)
            assertEquals(Piece.Queen, queenSquare.piece?.piece?.piece)
            assertEquals(Side.WHITE, queenSquare.piece?.piece?.side)
        }

        @Test
        fun `GIVEN promotion position WHEN promoting to knight THEN knight appears on board`() {
            underTest.load(GamePlayableBoard(buildPromotionGame(), Side.WHITE))
            underTest.handleSquareClick(Locus.a7)
            val clickResult = underTest.handleSquareClick(Locus.a8)
            val promotionAt = clickResult.promotion!!.at

            val result = underTest.promote(Piece.Knight, promotionAt)

            val knightSquare = result.data.boardData.at(Locus.a8)
            assertEquals(Piece.Knight, knightSquare.piece?.piece?.piece)
            assertTrue(result.movePlayed)
            assertNull(result.promotion)
        }
    }

    @Nested
    internal inner class PlayMove {
        private val underTest = BoardInteractionHelper()

        @Test
        fun `GIVEN loaded game WHEN playMove THEN move is applied`() {
            underTest.load(GamePlayableBoard(buildDefaultGame(), Side.WHITE))

            val result = underTest.playMove(Locus.e2, Locus.e4)

            assertNull(result.boardData.at(Locus.e2).piece)
            assertNotNull(result.boardData.at(Locus.e4).piece)
            assertEquals(Piece.Pawn, result.boardData.at(Locus.e4).piece?.piece?.piece)
        }

        @Test
        fun `GIVEN loaded game WHEN playMove THEN last move is highlighted`() {
            underTest.load(GamePlayableBoard(buildDefaultGame(), Side.WHITE))

            val result = underTest.playMove(Locus.e2, Locus.e4)

            assertTrue(result.boardData.at(Locus.e2).lastMove)
            assertTrue(result.boardData.at(Locus.e4).lastMove)
        }

        @Test
        fun `GIVEN loaded game WHEN playMove THEN animating piece is set`() {
            underTest.load(GamePlayableBoard(buildDefaultGame(), Side.WHITE))

            val result = underTest.playMove(Locus.g1, Locus.f3)

            val animating = result.boardData.animatingPiece
            assertNotNull(animating)
            assertEquals(Piece.Knight, animating!!.piece.piece)
        }

        @Test
        fun `GIVEN loaded game WHEN playMove with promotion THEN piece is promoted`() {
            underTest.load(GamePlayableBoard(buildPromotionGame(), Side.WHITE))

            val result = underTest.playMove(Locus.a7, Locus.a8, Piece.Queen)

            val promotedSquare = result.boardData.at(Locus.a8)
            assertEquals(Piece.Queen, promotedSquare.piece?.piece?.piece)
            assertEquals(Side.WHITE, promotedSquare.piece?.piece?.side)
        }

        @Test
        fun `GIVEN loaded game WHEN playMove captures THEN captured list is updated`() {
            underTest.load(GamePlayableBoard(buildCaptureGame(), Side.WHITE))
            underTest.playMove(Locus.e2, Locus.e4)
            underTest.playMove(Locus.d7, Locus.d5)

            underTest.playMove(Locus.e4, Locus.d5)

            assertEquals(Piece.Pawn.unicode, underTest.current().captured.byPlayer)
        }
    }

    @Nested
    internal inner class NavigationWithGame {
        @Test
        fun `GIVEN game at start WHEN canUndo THEN returns false`() {
            val game = buildDefaultGame()
            val underTest = BoardInteractionHelper(navigation = GameNavigation())
            underTest.load(GamePlayableBoard(game, Side.WHITE))

            assertFalse(underTest.canUndo())
        }

        @Test
        fun `GIVEN game at start WHEN canReplay THEN returns false`() {
            val game = buildDefaultGame()
            val underTest = BoardInteractionHelper(navigation = GameNavigation())
            underTest.load(GamePlayableBoard(game, Side.WHITE))

            assertFalse(underTest.canReplay())
        }

        @Test
        fun `GIVEN game with moves WHEN undoLast THEN boardData reflects previous position`() {
            val game = buildDefaultGame()
            val underTest = BoardInteractionHelper(navigation = GameNavigation())
            underTest.load(GamePlayableBoard(game, Side.WHITE))
            underTest.handleSquareClick(Locus.e2)
            underTest.handleSquareClick(Locus.e4)
            assertTrue(underTest.canUndo())

            val result = underTest.undoLast()

            assertNotNull(result.boardData.at(Locus.e2).piece)
            assertEquals(Piece.Pawn, result.boardData.at(Locus.e2).piece?.piece?.piece)
            assertNull(result.boardData.at(Locus.e4).piece)
        }

        @Test
        fun `GIVEN game with moves WHEN undoAll THEN boardData reflects starting position`() {
            val game = buildDefaultGame()
            val underTest = BoardInteractionHelper(navigation = GameNavigation())
            underTest.load(GamePlayableBoard(game, Side.WHITE))
            underTest.handleSquareClick(Locus.e2)
            underTest.handleSquareClick(Locus.e4)

            val result = underTest.undoAll()

            assertNotNull(result.boardData.at(Locus.e2).piece)
            assertFalse(underTest.canUndo())
            assertTrue(underTest.canReplay())
        }

        @Test
        fun `GIVEN game undone WHEN replayNext THEN boardData shows next move`() {
            val game = buildDefaultGame()
            val underTest = BoardInteractionHelper(navigation = GameNavigation())
            underTest.load(GamePlayableBoard(game, Side.WHITE))
            underTest.handleSquareClick(Locus.e2)
            underTest.handleSquareClick(Locus.e4)
            underTest.undoLast()

            val result = underTest.replayNext()

            assertNotNull(result.boardData.at(Locus.e4).piece)
            assertEquals(Piece.Pawn, result.boardData.at(Locus.e4).piece?.piece?.piece)
            assertTrue(result.boardData.at(Locus.e4).lastMove)
        }

        @Test
        fun `GIVEN game fully undone WHEN replayAll THEN boardData shows final position`() {
            val game = buildDefaultGame()
            val underTest = BoardInteractionHelper(navigation = GameNavigation())
            underTest.load(GamePlayableBoard(game, Side.WHITE))
            underTest.handleSquareClick(Locus.e2)
            underTest.handleSquareClick(Locus.e4)
            underTest.undoAll()

            val result = underTest.replayAll()

            assertNotNull(result.boardData.at(Locus.e4).piece)
            assertFalse(underTest.canReplay())
        }
    }

    @Nested
    internal inner class NavigationWithNoOp {
        private val underTest = BoardInteractionHelper()

        @Test
        fun `GIVEN no navigation WHEN canUndo THEN returns false`() {
            underTest.load(GamePlayableBoard(buildDefaultGame(), Side.WHITE))
            assertFalse(underTest.canUndo())
        }

        @Test
        fun `GIVEN no navigation WHEN canReplay THEN returns false`() {
            underTest.load(GamePlayableBoard(buildDefaultGame(), Side.WHITE))
            assertFalse(underTest.canReplay())
        }
    }

    @Nested
    internal inner class SolutionWithPuzzle {
        @Test
        fun `GIVEN puzzle WHEN hint THEN returns hint square selected`() {
            val puzzle = buildStandardPuzzle()
            val underTest = BoardInteractionHelper(solution = PuzzleSolution())
            underTest.load(PuzzlePlayableBoard(puzzle))

            val data = underTest.hint()

            assertEquals(Locus.e7, data.boardData.selection)
            assertEquals(true, data.boardData.at(Locus.e7).piece?.isSelected)
            assertTrue(data.boardData.availableMoves.isNotEmpty())
        }

        @Test
        fun `GIVEN puzzle WHEN playNextSolutionMove THEN plays expected move`() {
            val puzzle = buildStandardPuzzle()
            val underTest = BoardInteractionHelper(solution = PuzzleSolution())
            underTest.load(PuzzlePlayableBoard(puzzle))

            val result = underTest.playNextSolutionMove()

            assertNotNull(result)
            val e5Square = result!!.boardData.at(Locus.e5)
            assertNotNull(e5Square.piece)
            assertEquals(Piece.Pawn, e5Square.piece?.piece?.piece)
        }

        @Test
        fun `GIVEN puzzle WHEN hasSolutionMoves THEN returns true`() {
            val puzzle = buildStandardPuzzle()
            val underTest = BoardInteractionHelper(solution = PuzzleSolution())
            underTest.load(PuzzlePlayableBoard(puzzle))

            assertTrue(underTest.hasSolutionMoves())
        }

        @Test
        fun `GIVEN puzzle over WHEN playNextSolutionMove THEN returns null`() {
            val puzzle = buildStandardPuzzle()
            val underTest = BoardInteractionHelper(solution = PuzzleSolution())
            underTest.load(PuzzlePlayableBoard(puzzle))
            puzzle.resign()

            val result = underTest.playNextSolutionMove()

            assertNull(result)
        }
    }

    @Nested
    internal inner class SolutionWithNoOp {
        private val underTest = BoardInteractionHelper()

        @Test
        fun `GIVEN no solution WHEN hint THEN returns current data unchanged`() {
            underTest.load(GamePlayableBoard(buildDefaultGame(), Side.WHITE))
            val before = underTest.current()

            val result = underTest.hint()

            assertEquals(before, result)
        }

        @Test
        fun `GIVEN no solution WHEN playNextSolutionMove THEN returns null`() {
            underTest.load(GamePlayableBoard(buildDefaultGame(), Side.WHITE))
            assertNull(underTest.playNextSolutionMove())
        }

        @Test
        fun `GIVEN no solution WHEN hasSolutionMoves THEN returns false`() {
            underTest.load(GamePlayableBoard(buildDefaultGame(), Side.WHITE))
            assertFalse(underTest.hasSolutionMoves())
        }
    }

    @Nested
    internal inner class Refresh {
        private val underTest = BoardInteractionHelper()

        @Test
        fun `GIVEN loaded game WHEN refresh THEN returns current state`() {
            val playable = GamePlayableBoard(buildDefaultGame(), Side.WHITE)
            underTest.load(playable)
            underTest.handleSquareClick(Locus.e2)
            underTest.handleSquareClick(Locus.e4)

            val result = underTest.refresh()

            assertNotNull(result.boardData.at(Locus.e4).piece)
            assertNull(result.boardData.selection)
            assertNull(result.boardData.animatingPiece)
        }
    }

    @Nested
    internal inner class SelectSquare {
        private val underTest = BoardInteractionHelper()

        @Test
        fun `GIVEN loaded game WHEN selectSquare THEN square is selected with moves`() {
            underTest.load(GamePlayableBoard(buildDefaultGame(), Side.WHITE))

            val result = underTest.selectSquare(Locus.e2)

            assertEquals(Locus.e2, result.boardData.selection)
            assertTrue(result.boardData.availableMoves.isNotEmpty())
        }
    }

    // Test helpers

    private fun buildDefaultGame(): Game = gameFactory.builder()
        .withDefaultBoard()
        .buildGame()

    private fun buildPromotionGame(): Game = gameFactory.builder()
        .withTurn(Side.WHITE)
        .withPiece(Piece.King, Side.WHITE, Locus.e1)
        .withPiece(Piece.King, Side.BLACK, Locus.e8)
        .withPiece(Piece.Pawn, Side.WHITE, Locus.a7)
        .buildGame()

    private fun buildCaptureGame(): Game = gameFactory.builder()
        .withDefaultBoard()
        .buildGame()

    private fun buildStandardPuzzle(rating: Int = 1200): Puzzle = gameFactory.builder()
        .withDefaultBoard()
        .withRating(rating)
        .withMoves(listOf("e2e4", "e7e5", "g1f3", "b8c6"))
        .buildPuzzle()
}
