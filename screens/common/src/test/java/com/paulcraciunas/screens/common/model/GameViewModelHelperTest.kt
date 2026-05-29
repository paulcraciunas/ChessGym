package com.paulcraciunas.screens.common.model

import com.paulcraciunas.game.logic.api.Game
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

internal class GameViewModelHelperTest {
    private val gameFactory = RealGameFactory()
    private val underTest = GameViewModelHelper()

    @Nested
    internal inner class Load {
        @Test
        fun `GIVEN game WHEN load THEN returns GameData with correct player`() {
            // Given
            val game = buildDefaultGame()

            // When
            val result = underTest.load(game, Side.WHITE)

            // Then
            assertEquals(Side.WHITE, result.player)
        }

        @Test
        fun `GIVEN game WHEN load as black THEN returns GameData with black player`() {
            // Given
            val game = buildDefaultGame()

            // When
            val result = underTest.load(game, Side.BLACK)

            // Then
            assertEquals(Side.BLACK, result.player)
        }

        @Test
        fun `GIVEN game WHEN load THEN returns GameData with no captured pieces`() {
            // Given
            val game = buildDefaultGame()

            // When
            val result = underTest.load(game, Side.WHITE)

            // Then
            assertNotNull(result.captured)
            assertEquals(result.captured.byPlayer, "")
            assertEquals(result.captured.byOpponent, "")
        }

        @Test
        fun `GIVEN game WHEN load THEN returns GameData with boardData`() {
            // Given
            val game = buildDefaultGame()

            // When
            val result = underTest.load(game, Side.WHITE)

            // Then
            assertNotNull(result.boardData)
        }

        @Test
        fun `GIVEN game WHEN load THEN helper properties are updated`() {
            // Given
            val game = buildDefaultGame()

            // When
            underTest.load(game, Side.WHITE)

            // Then
            assertEquals(Side.WHITE, underTest.current().player)
        }

        @Test
        fun `GIVEN game with rating WHEN load THEN rating is accessible`() {
            // Given
            val game = gameFactory.builder()
                .withDefaultBoard()
                .withRating(1600)
                .buildGame()

            // When
            underTest.load(game, Side.WHITE)

            // Then
            assertEquals(1600, underTest.current().rating)
        }
    }

    @Nested
    internal inner class HandleSquareClick {
        @Test
        fun `GIVEN no selection WHEN clicking piece THEN piece is selected with moves`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)

            // When
            val result = underTest.handleSquareClick(Locus.e2)

            // Then
            val selectedSquare = result.data.boardData.at(Locus.e2)
            assertEquals(true, selectedSquare.piece?.isSelected)
            assertTrue(result.data.boardData.at(Locus.e4).canMoveTo)
            assertTrue(result.data.boardData.at(Locus.e3).canMoveTo)
        }

        @Test
        fun `GIVEN piece selected WHEN clicking same square THEN selection is cleared`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)
            underTest.handleSquareClick(Locus.e2)

            // When
            val result = underTest.handleSquareClick(Locus.e2)

            // Then
            val selectedSquare = result.data.boardData.at(Locus.e2)
            assertEquals(false, selectedSquare.piece?.isSelected)
            assertFalse(result.data.boardData.at(Locus.e4).canMoveTo)
        }

        @Test
        fun `GIVEN piece selected WHEN clicking invalid move THEN selection is cleared`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)
            underTest.handleSquareClick(Locus.e2)

            // When - e2 to e5 is not a valid pawn move
            val result = underTest.handleSquareClick(Locus.e5)

            // Then
            val selectedSquare = result.data.boardData.at(Locus.e2)
            assertEquals(false, selectedSquare.piece?.isSelected)
            assertFalse(result.data.boardData.at(Locus.e4).canMoveTo)
        }

        @Test
        fun `GIVEN piece selected WHEN clicking valid move THEN move is played`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)
            underTest.handleSquareClick(Locus.e2)

            // When
            val result = underTest.handleSquareClick(Locus.e4)

            // Then
            val fromSquare = result.data.boardData.at(Locus.e2)
            val toSquare = result.data.boardData.at(Locus.e4)
            assertNull(fromSquare.piece)
            assertNotNull(toSquare.piece)
            assertEquals(Piece.Pawn, toSquare.piece?.piece?.piece)
        }

        @Test
        fun `GIVEN piece selected WHEN valid move played THEN movePlayed is true`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)
            underTest.handleSquareClick(Locus.e2)

            // When
            val result = underTest.handleSquareClick(Locus.e4)

            // Then
            assertTrue(result.movePlayed)
        }

        @Test
        fun `GIVEN piece selected WHEN same square clicked THEN movePlayed is false`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)
            underTest.handleSquareClick(Locus.e2)

            // When
            val result = underTest.handleSquareClick(Locus.e2)

            // Then
            assertFalse(result.movePlayed)
        }

        @Test
        fun `GIVEN no selection WHEN clicking opponent piece THEN piece is not selected`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)

            // When
            val result = underTest.handleSquareClick(Locus.e7)

            // Then
            assertNull(result.data.boardData.selection)
            assertTrue(result.data.boardData.availableMoves.isEmpty())
            assertFalse(result.movePlayed)
        }

        @Test
        fun `GIVEN playing as black WHEN clicking white piece THEN piece is not selected`() {
            // Given
            underTest.load(buildDefaultGame(), Side.BLACK)

            // When
            underTest.handleSquareClick(Locus.e2)
            underTest.handleSquareClick(Locus.e4)
            val result = underTest.handleSquareClick(Locus.e4)

            // Then
            assertNull(result.data.boardData.selection)
            assertTrue(result.data.boardData.availableMoves.isEmpty())
            assertFalse(result.movePlayed)
        }

        @Test
        fun `GIVEN no selection WHEN clicking empty square THEN nothing is selected`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)

            // When
            val result = underTest.handleSquareClick(Locus.e4)

            // Then
            val clickedSquare = result.data.boardData.at(Locus.e4)
            assertNull(clickedSquare.piece)
            assertFalse(result.movePlayed)
        }

        @Test
        fun `GIVEN game WHEN move played THEN last move is highlighted`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)
            underTest.handleSquareClick(Locus.e2)

            // When
            val result = underTest.handleSquareClick(Locus.e4)

            // Then
            val fromSquare = result.data.boardData.at(Locus.e2)
            val toSquare = result.data.boardData.at(Locus.e4)
            assertTrue(fromSquare.lastMove)
            assertTrue(toSquare.lastMove)
        }

        @Test
        fun `GIVEN game WHEN move played THEN animating piece is set`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)
            underTest.handleSquareClick(Locus.e2)

            // When
            val result = underTest.handleSquareClick(Locus.e4)

            // Then
            val animating = result.data.boardData.animatingPiece
            assertNotNull(animating)
            assertEquals(Piece.Pawn, animating!!.piece.piece)
            assertEquals(Side.WHITE, animating.piece.side)
            assertEquals(Locus.e2, animating.from)
            assertEquals(Locus.e4, animating.to)
        }

        @Test
        fun `GIVEN pawn reaching back rank WHEN clicking target THEN returns promotion required`() {
            // Given
            underTest.load(buildPromotionGame(), Side.WHITE)
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
            underTest.load(buildPromotionGame(), Side.WHITE)
            underTest.handleSquareClick(Locus.a7)

            // When
            val result = underTest.handleSquareClick(Locus.a8)

            // Then
            assertNull(result.promotion)
            assertTrue(result.movePlayed)
            val queenSquare = result.data.boardData.at(Locus.a8)
            assertNotNull(queenSquare.piece)
            assertEquals(Piece.Queen, queenSquare.piece?.piece?.piece)
            assertEquals(Side.WHITE, queenSquare.piece?.piece?.side)
        }

        @Test
        fun `GIVEN autoPromote disabled WHEN pawn reaches back rank THEN shows promotion chooser`() {
            // Given
            underTest.autoPromote = false
            underTest.load(buildPromotionGame(), Side.WHITE)
            underTest.handleSquareClick(Locus.a7)

            // When
            val result = underTest.handleSquareClick(Locus.a8)

            // Then
            assertNotNull(result.promotion)
            assertTrue(result.promotion!!.showChooser)
        }

        @Test
        fun `GIVEN game not over WHEN move played THEN isOver is false`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)
            underTest.handleSquareClick(Locus.e2)

            // When
            val result = underTest.handleSquareClick(Locus.e4)

            // Then
            assertFalse(result.isOver)
        }

        @Test
        fun `GIVEN checkmate position WHEN delivering checkmate THEN isOver is true`() {
            // Given - White Queen can deliver mate on f7, Rook covers 7th rank
            val game = gameFactory.builder()
                .withTurn(Side.WHITE)
                .withPiece(Piece.King, Side.WHITE, Locus.g1)
                .withPiece(Piece.King, Side.BLACK, Locus.h8)
                .withPiece(Piece.Rook, Side.WHITE, Locus.g7)
                .withPiece(Piece.Queen, Side.WHITE, Locus.f6)
                .buildGame()
            underTest.load(game, Side.WHITE)
            underTest.handleSquareClick(Locus.f6)

            // When - Queen goes to f8, mating (King on h8 can't escape: g7 blocked by Rook, g8/h7 covered by Queen)
            val result = underTest.handleSquareClick(Locus.f8)

            // Then
            assertTrue(result.isOver)
        }
    }

    @Nested
    internal inner class PlayMove {
        @Test
        fun `GIVEN loaded game WHEN playMove THEN move is applied`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)

            // When
            val result = underTest.playMove(Locus.e2, Locus.e4)

            // Then
            val fromSquare = result.boardData.at(Locus.e2)
            val toSquare = result.boardData.at(Locus.e4)
            assertNull(fromSquare.piece)
            assertNotNull(toSquare.piece)
            assertEquals(Piece.Pawn, toSquare.piece?.piece?.piece)
        }

        @Test
        fun `GIVEN loaded game WHEN playMove THEN last move is highlighted`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)

            // When
            val result = underTest.playMove(Locus.e2, Locus.e4)

            // Then
            assertTrue(result.boardData.at(Locus.e2).lastMove)
            assertTrue(result.boardData.at(Locus.e4).lastMove)
        }

        @Test
        fun `GIVEN loaded game WHEN playMove THEN animating piece is set`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)

            // When
            val result = underTest.playMove(Locus.g1, Locus.f3)

            // Then
            val animating = result.boardData.animatingPiece
            assertNotNull(animating)
            assertEquals(Piece.Knight, animating!!.piece.piece)
            assertEquals(Side.WHITE, animating.piece.side)
            assertEquals(Locus.g1, animating.from)
            assertEquals(Locus.f3, animating.to)
        }

        @Test
        fun `GIVEN loaded game WHEN playMove with promotion THEN piece is promoted`() {
            // Given
            underTest.load(buildPromotionGame(), Side.WHITE)

            // When
            val result = underTest.playMove(Locus.a7, Locus.a8, Piece.Queen)

            // Then
            val promotedSquare = result.boardData.at(Locus.a8)
            assertNotNull(promotedSquare.piece)
            assertEquals(Piece.Queen, promotedSquare.piece?.piece?.piece)
            assertEquals(Side.WHITE, promotedSquare.piece?.piece?.side)
        }

        @Test
        fun `GIVEN loaded game WHEN playMove captures THEN captured list is updated`() {
            // Given
            underTest.load(buildCaptureGame(), Side.WHITE)
            underTest.playMove(Locus.e2, Locus.e4)
            underTest.playMove(Locus.d7, Locus.d5)

            // When
            underTest.playMove(Locus.e4, Locus.d5)

            // Then
            assertEquals(Piece.Pawn.unicode, underTest.current().captured.byPlayer)
        }
    }

    @Nested
    internal inner class Promote {
        @Test
        fun `GIVEN promotion position WHEN promoting to queen THEN move is played`() {
            // Given
            underTest.load(buildPromotionGame(), Side.WHITE)
            underTest.handleSquareClick(Locus.a7)
            val clickResult = underTest.handleSquareClick(Locus.a8)
            val promotionAt = clickResult.promotion!!.at

            // When
            val result = underTest.promote(Piece.Queen, promotionAt)

            // Then
            val queenSquare = result.data.boardData.at(Locus.a8)
            assertNotNull(queenSquare.piece)
            assertEquals(Piece.Queen, queenSquare.piece?.piece?.piece)
            assertEquals(Side.WHITE, queenSquare.piece?.piece?.side)
        }

        @Test
        fun `GIVEN promotion WHEN played THEN promotion is null in result`() {
            // Given
            underTest.load(buildPromotionGame(), Side.WHITE)
            underTest.handleSquareClick(Locus.a7)
            val clickResult = underTest.handleSquareClick(Locus.a8)
            val promotionAt = clickResult.promotion!!.at

            // When
            val result = underTest.promote(Piece.Queen, promotionAt)

            // Then
            assertNull(result.promotion)
        }

        @Test
        fun `GIVEN promotion WHEN promoting to knight THEN knight appears on board`() {
            // Given
            underTest.load(buildPromotionGame(), Side.WHITE)
            underTest.handleSquareClick(Locus.a7)
            val clickResult = underTest.handleSquareClick(Locus.a8)
            val promotionAt = clickResult.promotion!!.at

            // When
            val result = underTest.promote(Piece.Knight, promotionAt)

            // Then
            val knightSquare = result.data.boardData.at(Locus.a8)
            assertNotNull(knightSquare.piece)
            assertEquals(Piece.Knight, knightSquare.piece?.piece?.piece)
        }
    }

    @Nested
    internal inner class Resign {
        @Test
        fun `GIVEN game in progress WHEN resign THEN game is over`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)

            // When
            underTest.resign()

            // Then
            val result = underTest.handleSquareClick(Locus.e2)
            assertTrue(result.isOver)
        }
    }

    @Nested
    internal inner class BuildPuzzleData {
        @Test
        fun `GIVEN game loaded WHEN buildPuzzleData THEN returns current state`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)

            // When
            val data = underTest.current()

            // Then
            assertEquals(Side.WHITE, data.player)
            assertNotNull(data.boardData)
        }

        @Test
        fun `GIVEN move played WHEN buildPuzzleData THEN reflects updated board`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)
            underTest.handleSquareClick(Locus.e2)
            underTest.handleSquareClick(Locus.e4)

            // When
            val data = underTest.current()

            // Then
            val e4Square = data.boardData.at(Locus.e4)
            assertNotNull(e4Square.piece)
            assertEquals(Piece.Pawn, e4Square.piece?.piece?.piece)
        }
    }

    @Nested
    internal inner class Navigation {
        @Test
        fun `GIVEN game at start WHEN canUndo THEN returns false`() {
            underTest.load(buildDefaultGame(), Side.WHITE)

            assertFalse(underTest.canUndo())
        }

        @Test
        fun `GIVEN game at start WHEN canReplay THEN returns false`() {
            underTest.load(buildDefaultGame(), Side.WHITE)

            assertFalse(underTest.canReplay())
        }

        @Test
        fun `GIVEN game with moves WHEN undoLast THEN boardData reflects previous position`() {
            underTest.load(buildDefaultGame(), Side.WHITE)
            underTest.handleSquareClick(Locus.e2)
            underTest.handleSquareClick(Locus.e4)
            assertTrue(underTest.canUndo())

            val result = underTest.undoLast()

            val e2Square = result.boardData.at(Locus.e2)
            val e4Square = result.boardData.at(Locus.e4)
            assertNotNull(e2Square.piece)
            assertEquals(Piece.Pawn, e2Square.piece?.piece?.piece)
            assertNull(e4Square.piece)
        }

        @Test
        fun `GIVEN game with moves WHEN undoAll THEN boardData reflects starting position`() {
            underTest.load(buildDefaultGame(), Side.WHITE)
            underTest.handleSquareClick(Locus.e2)
            underTest.handleSquareClick(Locus.e4)

            val result = underTest.undoAll()

            val e2Square = result.boardData.at(Locus.e2)
            assertNotNull(e2Square.piece)
            assertEquals(Piece.Pawn, e2Square.piece?.piece?.piece)
            assertFalse(underTest.canUndo())
            assertTrue(underTest.canReplay())
        }

        @Test
        fun `GIVEN game undone WHEN replayNext THEN boardData shows next move`() {
            underTest.load(buildDefaultGame(), Side.WHITE)
            underTest.handleSquareClick(Locus.e2)
            underTest.handleSquareClick(Locus.e4)
            underTest.undoLast()

            val result = underTest.replayNext()

            val e4Square = result.boardData.at(Locus.e4)
            assertNotNull(e4Square.piece)
            assertEquals(Piece.Pawn, e4Square.piece?.piece?.piece)
            assertTrue(e4Square.lastMove)
        }

        @Test
        fun `GIVEN game undone WHEN replayNext THEN animating piece is not set`() {
            underTest.load(buildDefaultGame(), Side.WHITE)
            underTest.handleSquareClick(Locus.e2)
            underTest.handleSquareClick(Locus.e4)
            underTest.undoLast()

            val result = underTest.replayNext()

            val animating = result.boardData.animatingPiece
            assertNull(animating)
        }

        @Test
        fun `GIVEN game fully undone WHEN replayAll THEN boardData shows final position`() {
            underTest.load(buildDefaultGame(), Side.WHITE)
            underTest.handleSquareClick(Locus.e2)
            underTest.handleSquareClick(Locus.e4)
            underTest.undoAll()

            val result = underTest.replayAll()

            val e4Square = result.boardData.at(Locus.e4)
            assertNotNull(e4Square.piece)
            assertFalse(underTest.canReplay())
        }

        @Test
        fun `GIVEN game undone WHEN undoLast THEN no last move highlights`() {
            underTest.load(buildDefaultGame(), Side.WHITE)
            underTest.handleSquareClick(Locus.e2)
            underTest.handleSquareClick(Locus.e4)

            val result = underTest.undoLast()

            val e2Square = result.boardData.at(Locus.e2)
            val e4Square = result.boardData.at(Locus.e4)
            assertFalse(e2Square.lastMove)
            assertFalse(e4Square.lastMove)
        }
    }

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
}
