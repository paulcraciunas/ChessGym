package com.paulcraciunas.screens.common.model

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.game.logic.api.board.loc
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
    private val underTest = GameViewModelHelper(gameFactory.gameInteractor())

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
        fun `GIVEN game WHEN load THEN returns GameData with captured pieces`() {
            // Given
            val game = buildDefaultGame()

            // When
            val result = underTest.load(game, Side.WHITE)

            // Then
            assertNotNull(result.captured)
            assertTrue(result.captured.containsKey(Side.WHITE))
            assertTrue(result.captured.containsKey(Side.BLACK))
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
            assertEquals(Side.WHITE, underTest.player)
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
            assertEquals(1600, underTest.rating)
        }

        @Test
        fun `GIVEN new game WHEN load THEN captured lists are empty`() {
            // Given
            val game = buildDefaultGame()

            // When
            val result = underTest.load(game, Side.WHITE)

            // Then
            assertTrue(result.captured[Side.WHITE]!!.isEmpty())
            assertTrue(result.captured[Side.BLACK]!!.isEmpty())
        }
    }

    @Nested
    internal inner class HandleSquareClick {
        @Test
        fun `GIVEN no selection WHEN clicking piece THEN piece is selected with moves`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)

            // When
            val result = underTest.handleSquareClick("e2".loc())

            // Then
            val selectedSquare = result.data.boardData.at(Rank.`2`, File.e)
            assertEquals(true, selectedSquare.piece?.isSelected)
            assertTrue(result.data.boardData.at(Rank.`4`, File.e).canMoveTo)
            assertTrue(result.data.boardData.at(Rank.`3`, File.e).canMoveTo)
        }

        @Test
        fun `GIVEN piece selected WHEN clicking same square THEN selection is cleared`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)
            underTest.handleSquareClick("e2".loc())

            // When
            val result = underTest.handleSquareClick("e2".loc())

            // Then
            val selectedSquare = result.data.boardData.at(Rank.`2`, File.e)
            assertEquals(false, selectedSquare.piece?.isSelected)
            assertFalse(result.data.boardData.at(Rank.`4`, File.e).canMoveTo)
        }

        @Test
        fun `GIVEN piece selected WHEN clicking invalid move THEN selection is cleared`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)
            underTest.handleSquareClick("e2".loc())

            // When - e2 to e5 is not a valid pawn move
            val result = underTest.handleSquareClick("e5".loc())

            // Then
            val selectedSquare = result.data.boardData.at(Rank.`2`, File.e)
            assertEquals(false, selectedSquare.piece?.isSelected)
            assertFalse(result.data.boardData.at(Rank.`4`, File.e).canMoveTo)
        }

        @Test
        fun `GIVEN piece selected WHEN clicking valid move THEN move is played`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)
            underTest.handleSquareClick("e2".loc())

            // When
            val result = underTest.handleSquareClick("e4".loc())

            // Then
            val fromSquare = result.data.boardData.at(Rank.`2`, File.e)
            val toSquare = result.data.boardData.at(Rank.`4`, File.e)
            assertNull(fromSquare.piece)
            assertNotNull(toSquare.piece)
            assertEquals(Piece.Pawn, toSquare.piece?.piece)
        }

        @Test
        fun `GIVEN piece selected WHEN valid move played THEN movePlayed is true`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)
            underTest.handleSquareClick("e2".loc())

            // When
            val result = underTest.handleSquareClick("e4".loc())

            // Then
            assertTrue(result.movePlayed)
            assertEquals("e2".loc(), result.moveFrom)
        }

        @Test
        fun `GIVEN piece selected WHEN same square clicked THEN movePlayed is false`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)
            underTest.handleSquareClick("e2".loc())

            // When
            val result = underTest.handleSquareClick("e2".loc())

            // Then
            assertFalse(result.movePlayed)
            assertNull(result.moveFrom)
        }

        @Test
        fun `GIVEN no selection WHEN clicking empty square THEN nothing is selected`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)

            // When
            val result = underTest.handleSquareClick("e4".loc())

            // Then
            val clickedSquare = result.data.boardData.at(Rank.`4`, File.e)
            assertNull(clickedSquare.piece)
            assertFalse(result.movePlayed)
        }

        @Test
        fun `GIVEN game WHEN move played THEN last move is highlighted`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)
            underTest.handleSquareClick("e2".loc())

            // When
            val result = underTest.handleSquareClick("e4".loc())

            // Then
            val fromSquare = result.data.boardData.at(Rank.`2`, File.e)
            val toSquare = result.data.boardData.at(Rank.`4`, File.e)
            assertTrue(fromSquare.lastMove)
            assertTrue(toSquare.lastMove)
        }

        @Test
        fun `GIVEN game WHEN move played THEN animating piece is set`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)
            underTest.handleSquareClick("e2".loc())

            // When
            val result = underTest.handleSquareClick("e4".loc())

            // Then
            val animating = result.data.boardData.animatingPiece
            assertNotNull(animating)
            assertEquals(Piece.Pawn, animating!!.piece)
            assertEquals(Side.WHITE, animating.side)
            assertEquals(Locus(File.e, Rank.`2`), animating.from)
            assertEquals(Locus(File.e, Rank.`4`), animating.to)
        }

        @Test
        fun `GIVEN pawn reaching back rank WHEN clicking target THEN returns promotion required`() {
            // Given
            underTest.load(buildPromotionGame(), Side.WHITE)
            underTest.handleSquareClick("a7".loc())

            // When
            val result = underTest.handleSquareClick("a8".loc())

            // Then
            assertNotNull(result.promotion)
            assertTrue(result.promotion!!.showChooser)
            assertEquals(Locus(File.a, Rank.`8`), result.promotion.at)
            assertEquals("a7".loc(), result.moveFrom)
        }

        @Test
        fun `GIVEN autoPromote enabled WHEN pawn reaches back rank THEN promotes to queen directly`() {
            // Given
            underTest.autoPromote = true
            underTest.load(buildPromotionGame(), Side.WHITE)
            underTest.handleSquareClick("a7".loc())

            // When
            val result = underTest.handleSquareClick("a8".loc())

            // Then
            assertNull(result.promotion)
            assertTrue(result.movePlayed)
            assertEquals("a7".loc(), result.moveFrom)
            assertEquals(Piece.Queen, result.autoPromotedTo)
            val queenSquare = result.data.boardData.at(Rank.`8`, File.a)
            assertNotNull(queenSquare.piece)
            assertEquals(Piece.Queen, queenSquare.piece?.piece)
            assertEquals(Side.WHITE, queenSquare.piece?.side)
        }

        @Test
        fun `GIVEN autoPromote disabled WHEN pawn reaches back rank THEN shows promotion chooser`() {
            // Given
            underTest.autoPromote = false
            underTest.load(buildPromotionGame(), Side.WHITE)
            underTest.handleSquareClick("a7".loc())

            // When
            val result = underTest.handleSquareClick("a8".loc())

            // Then
            assertNotNull(result.promotion)
            assertTrue(result.promotion!!.showChooser)
            assertNull(result.autoPromotedTo)
            assertEquals("a7".loc(), result.moveFrom)
        }

        @Test
        fun `GIVEN game not over WHEN move played THEN isOver is false`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)
            underTest.handleSquareClick("e2".loc())

            // When
            val result = underTest.handleSquareClick("e4".loc())

            // Then
            assertFalse(result.isOver)
        }

        @Test
        fun `GIVEN checkmate position WHEN delivering checkmate THEN isOver is true`() {
            // Given - White Queen can deliver mate on f7, Rook covers 7th rank
            val game = gameFactory.builder()
                .withTurn(Side.WHITE)
                .withPiece(Piece.King, Side.WHITE, "g1".loc())
                .withPiece(Piece.King, Side.BLACK, "h8".loc())
                .withPiece(Piece.Rook, Side.WHITE, "g7".loc())
                .withPiece(Piece.Queen, Side.WHITE, "f6".loc())
                .buildGame()
            underTest.load(game, Side.WHITE)
            underTest.handleSquareClick("f6".loc())

            // When - Queen goes to f8, mating (King on h8 can't escape: g7 blocked by Rook, g8/h7 covered by Queen)
            val result = underTest.handleSquareClick("f8".loc())

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
            val result = underTest.playMove("e2".loc(), "e4".loc())

            // Then
            val fromSquare = result.boardData.at(Rank.`2`, File.e)
            val toSquare = result.boardData.at(Rank.`4`, File.e)
            assertNull(fromSquare.piece)
            assertNotNull(toSquare.piece)
            assertEquals(Piece.Pawn, toSquare.piece?.piece)
        }

        @Test
        fun `GIVEN loaded game WHEN playMove THEN last move is highlighted`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)

            // When
            val result = underTest.playMove("e2".loc(), "e4".loc())

            // Then
            assertTrue(result.boardData.at(Rank.`2`, File.e).lastMove)
            assertTrue(result.boardData.at(Rank.`4`, File.e).lastMove)
        }

        @Test
        fun `GIVEN loaded game WHEN playMove THEN animating piece is set`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)

            // When
            val result = underTest.playMove("g1".loc(), "f3".loc())

            // Then
            val animating = result.boardData.animatingPiece
            assertNotNull(animating)
            assertEquals(Piece.Knight, animating!!.piece)
            assertEquals(Side.WHITE, animating.side)
            assertEquals(Locus(File.g, Rank.`1`), animating.from)
            assertEquals(Locus(File.f, Rank.`3`), animating.to)
        }

        @Test
        fun `GIVEN loaded game WHEN playMove with promotion THEN piece is promoted`() {
            // Given
            underTest.load(buildPromotionGame(), Side.WHITE)

            // When
            val result = underTest.playMove("a7".loc(), "a8".loc(), Piece.Queen)

            // Then
            val promotedSquare = result.boardData.at(Rank.`8`, File.a)
            assertNotNull(promotedSquare.piece)
            assertEquals(Piece.Queen, promotedSquare.piece?.piece)
            assertEquals(Side.WHITE, promotedSquare.piece?.side)
        }

        @Test
        fun `GIVEN loaded game WHEN playMove captures THEN captured list is updated`() {
            // Given
            underTest.load(buildCaptureGame(), Side.WHITE)
            underTest.playMove("e2".loc(), "e4".loc())
            underTest.playMove("d7".loc(), "d5".loc())

            // When
            val result = underTest.playMove("e4".loc(), "d5".loc())

            // Then
            val capturedByWhite = result.captured[Side.WHITE]!!
            assertTrue(capturedByWhite.contains(Piece.Pawn))
        }
    }

    @Nested
    internal inner class Promote {
        @Test
        fun `GIVEN promotion position WHEN promoting to queen THEN move is played`() {
            // Given
            underTest.load(buildPromotionGame(), Side.WHITE)
            underTest.handleSquareClick("a7".loc())
            val clickResult = underTest.handleSquareClick("a8".loc())
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
            underTest.load(buildPromotionGame(), Side.WHITE)
            underTest.handleSquareClick("a7".loc())
            val clickResult = underTest.handleSquareClick("a8".loc())
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
            underTest.handleSquareClick("a7".loc())
            val clickResult = underTest.handleSquareClick("a8".loc())
            val promotionAt = clickResult.promotion!!.at

            // When
            val result = underTest.promote(Piece.Knight, promotionAt)

            // Then
            val knightSquare = result.data.boardData.at(Rank.`8`, File.a)
            assertNotNull(knightSquare.piece)
            assertEquals(Piece.Knight, knightSquare.piece?.piece)
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
            val result = underTest.handleSquareClick("e2".loc())
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
            val data = underTest.buildPuzzleData()

            // Then
            assertEquals(Side.WHITE, data.player)
            assertNotNull(data.boardData)
        }

        @Test
        fun `GIVEN move played WHEN buildPuzzleData THEN reflects updated board`() {
            // Given
            underTest.load(buildDefaultGame(), Side.WHITE)
            underTest.handleSquareClick("e2".loc())
            underTest.handleSquareClick("e4".loc())

            // When
            val data = underTest.buildPuzzleData()

            // Then
            val e4Square = data.boardData.at(Rank.`4`, File.e)
            assertNotNull(e4Square.piece)
            assertEquals(Piece.Pawn, e4Square.piece?.piece)
        }
    }

    private fun buildDefaultGame(): Game = gameFactory.builder()
        .withDefaultBoard()
        .buildGame()

    private fun buildPromotionGame(): Game = gameFactory.builder()
        .withTurn(Side.WHITE)
        .withPiece(Piece.King, Side.WHITE, "e1".loc())
        .withPiece(Piece.King, Side.BLACK, "e8".loc())
        .withPiece(Piece.Pawn, Side.WHITE, "a7".loc())
        .buildGame()

    private fun buildCaptureGame(): Game = gameFactory.builder()
        .withDefaultBoard()
        .buildGame()
}
