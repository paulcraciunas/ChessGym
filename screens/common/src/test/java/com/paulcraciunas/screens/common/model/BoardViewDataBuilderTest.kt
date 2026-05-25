package com.paulcraciunas.screens.common.model

import com.paulcraciunas.game.logic.api.GameFactory
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
import org.junit.jupiter.api.Test

internal class BoardViewDataBuilderTest {
    private val gameFactory: GameFactory = RealGameFactory()
    private val underTest: BoardViewDataBuilder = BoardViewDataBuilder()
    private val lastMoveFrom = Locus.e2
    private val lastMoveTo = Locus.e4
    private val captureTarget = Locus.e7

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

    @Test
    fun `GIVEN built data WHEN modifying builder THEN built data is not affected`() {
        // Given
        underTest.load(buildPuzzle())
        val firstBuild = underTest.build()

        // When - modify the builder by adding selection
        underTest.withSelection(lastMoveFrom, listOf(lastMoveTo, captureTarget))

        // Then - first build should still have no selection
        firstBuild.at(lastMoveFrom).apply {
            assertNotNull(piece)
            assertEquals(false, piece?.isSelected)
        }
        assertFalse(firstBuild.at(lastMoveTo).canMoveTo)
    }

    @Test
    fun `GIVEN multiple builds WHEN comparing THEN each build is independent`() {
        // Given
        underTest.load(buildPuzzle())
        val firstBuild = underTest.build()

        underTest.withSelection(lastMoveFrom, listOf(lastMoveTo, captureTarget))
        val secondBuild = underTest.build()

        underTest.clearSelection()
        val thirdBuild = underTest.build()

        // Then - each build reflects the state at build time
        assertEquals(false, firstBuild.at(lastMoveFrom).piece?.isSelected)
        assertFalse(firstBuild.at(lastMoveTo).canMoveTo)

        assertEquals(true, secondBuild.at(lastMoveFrom).piece?.isSelected)
        assertTrue(secondBuild.at(lastMoveTo).canMoveTo)

        assertEquals(false, thirdBuild.at(lastMoveFrom).piece?.isSelected)
        assertFalse(thirdBuild.at(lastMoveTo).canMoveTo)
    }

    @Test
    fun `GIVEN built data WHEN refreshing builder THEN built data is not affected`() {
        // Given
        underTest.load(buildPuzzle())
        underTest.withSelection(lastMoveFrom, listOf(lastMoveTo, captureTarget))
        val builtWithSelection = underTest.build()

        // When
        underTest.refresh()

        // Then - built data still has the selection
        assertEquals(true, builtWithSelection.at(lastMoveFrom).piece?.isSelected)
        assertTrue(builtWithSelection.at(lastMoveTo).canMoveTo)
    }

    @Test
    fun `GIVEN loaded board WHEN selecting empty square THEN nothing is selected`() {
        // Given
        underTest.load(buildPuzzle())
        val emptySquare = Locus.e5

        // When
        underTest.withSelection(emptySquare, emptyList())

        // Then
        assertNull(underTest.selected)
    }

    @Test
    fun `GIVEN loaded board WHEN selecting empty square THEN board state is unchanged`() {
        // Given
        underTest.load(buildPuzzle())
        val dataBefore = underTest.build()
        val emptySquare = Locus.e5

        // When
        underTest.withSelection(emptySquare, emptyList())
        val dataAfter = underTest.build()

        // Then
        assertNull(dataAfter.at(emptySquare).piece)
        assertFalse(dataAfter.at(emptySquare).canMoveTo)
        assertNull(dataBefore.at(emptySquare).piece)
    }

    @Test
    fun `GIVEN existing selection WHEN selecting empty square THEN previous selection is preserved`() {
        // Given
        underTest.load(buildPuzzle())
        underTest.withSelection(lastMoveFrom, listOf(lastMoveTo))
        val emptySquare = Locus.e5

        // When
        underTest.withSelection(emptySquare, emptyList())

        // Then -- previous selection is still active
        assertEquals(lastMoveFrom, underTest.selected)
        val data = underTest.build()
        assertEquals(true, data.at(lastMoveFrom).piece?.isSelected)
    }

    private fun buildPuzzle(): Puzzle = gameFactory.builder()
        .withDefaultBoard()
        .withRating(1200)
        .withLastPly(Side.WHITE, Piece.Pawn, lastMoveFrom, lastMoveTo)
        .buildPuzzle()
}
