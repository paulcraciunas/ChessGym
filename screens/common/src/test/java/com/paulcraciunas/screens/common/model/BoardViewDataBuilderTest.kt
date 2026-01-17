package com.paulcraciunas.screens.common.model

import com.paulcraciunas.game.logic.api.GameFactory
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.loc
import com.paulcraciunas.game.logic.impl.RealGameFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class BoardViewDataBuilderTest {
    private val gameFactory: GameFactory = RealGameFactory()
    private val underTest: BoardViewDataBuilder = BoardViewDataBuilder()
    private val lastMoveFrom = "e2".loc()
    private val lastMoveTo = "e4".loc()
    private val captureTarget = "e7".loc()

    @Test
    fun `GIVEN puzzle with last ply WHEN loading THEN pieces and last move are marked`() {
        // Given
        underTest.load(buildPuzzle())

        // When
        val data = underTest.build()

        // Then
        data.at(lastMoveFrom).apply {
            assertNotNull(piece)
            assertTrue(lastMove)
            assertFalse(canMoveTo)
            assertEquals(Piece.Pawn, piece?.piece)
            assertEquals(Side.WHITE, piece?.side)
        }

        data.at(lastMoveTo).apply {
            assertTrue(lastMove)
            assertNull(piece)
        }

        data.at(captureTarget).apply {
            assertNotNull(piece)
            assertEquals(Piece.Pawn, piece?.piece)
            assertEquals(Side.BLACK, piece?.side)
        }
    }

    @Test
    fun `GIVEN loaded board WHEN selecting a piece THEN selection and moves are marked`() {
        // Given
        underTest.load(buildPuzzle())
        val moves = listOf(lastMoveTo, captureTarget)
        underTest.withSelection(lastMoveFrom, moves)

        // When
        val data = underTest.build()

        // Then
        data.at(lastMoveFrom).apply {
            assertNotNull(piece)
            assertEquals(true, piece?.isSelected)
        }

        data.at(lastMoveTo).apply {
            assertTrue(canMoveTo)
            assertNull(piece)
        }

        data.at(captureTarget).apply {
            assertEquals(true, piece?.isSelected)
            assertFalse(canMoveTo)
        }
    }

    @Test
    fun `GIVEN selection WHEN clearing THEN selection and moves are reset`() {
        // Given
        val puzzle = buildPuzzle()
        underTest.load(puzzle)
        underTest.withSelection(lastMoveFrom, listOf(lastMoveTo, captureTarget))

        // When
        underTest.clearSelection()

        // Then
        assertNull(underTest.selected)
        val data = underTest.build()
        data.at(lastMoveFrom).apply {
            assertNotNull(piece)
            assertEquals(false, piece?.isSelected)
        }

        assertFalse(data.at(lastMoveTo).canMoveTo)

        data.at(captureTarget).apply {
            assertNotNull(piece)
            assertEquals(false, piece?.isSelected)
        }
    }

    @Test
    fun `GIVEN selection WHEN refreshing THEN selection is cleared and last move is restored`() {
        // Given
        underTest.load(buildPuzzle())
        underTest.withSelection(lastMoveFrom, listOf(lastMoveTo, captureTarget))

        // When
        underTest.refresh()

        // Then
        assertNull(underTest.selected)
        val data = underTest.build()
        data.at(lastMoveFrom).apply {
            assertNotNull(piece)
            assertEquals(false, piece?.isSelected)
            assertTrue(lastMove)
        }

        data.at(lastMoveTo).apply {
            assertFalse(canMoveTo)
            assertTrue(lastMove)
        }
    }

    private fun buildPuzzle(): Puzzle = gameFactory.builder()
        .withDefaultBoard()
        .withRating(1200)
        .withLastPly(Side.WHITE, Piece.Pawn, lastMoveFrom, lastMoveTo)
        .buildPuzzle()
}
