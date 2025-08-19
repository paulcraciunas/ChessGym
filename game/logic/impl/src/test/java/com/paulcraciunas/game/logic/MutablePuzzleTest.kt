package com.paulcraciunas.game.logic

import com.paulcraciunas.game.logic.api.Builder
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.RealBuilder
import com.paulcraciunas.game.logic.impl.plies.PlyFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

internal class MutablePuzzleTest {
    private val builder: Builder = RealBuilder(PlyFactory()).withDefaultBoard().withRating(1200)

    private lateinit var underTest: Puzzle

    @Test
    fun `GIVEN puzzle with moves WHEN starting THEN state becomes InProgress`() {
        // Given
        underTest = builder.withMoves("e2e4", "e7e5").buildPuzzle()

        // When
        underTest.start()

        // Then
        assertEquals(Puzzle.State.InProgress, underTest.state)
    }

    @Test
    fun `GIVEN puzzle in progress WHEN playing correct move THEN move is accepted and state remains InProgress`() {
        // Given
        underTest = builder.withMoves("e2e4", "e7e5").buildPuzzle()
        underTest.start()

        // When
        underTest.play("e2".loc(), "e4".loc())

        // Then
        assertEquals(Puzzle.State.InProgress, underTest.state)
    }

    @Test
    fun `GIVEN puzzle in progress WHEN playing incorrect move THEN state becomes Failed`() {
        // Given
        underTest = builder.withMoves("e2e4", "e7e5").buildPuzzle()
        underTest.start()

        // When
        underTest.play("e2".loc(), "e3".loc()) // Wrong move

        // Then
        assertEquals(Puzzle.State.Failed, underTest.state)
    }

    @Test
    fun `GIVEN puzzle with one move WHEN playing correct move THEN state becomes Success`() {
        // Given
        underTest = builder.withMoves("e2e4").buildPuzzle()
        underTest.start()

        // When
        underTest.play("e2".loc(), "e4".loc())

        // Then
        assertEquals(Puzzle.State.Success, underTest.state)
    }

    @Test
    fun `GIVEN puzzle in progress WHEN abandoning THEN state becomes Failed`() {
        // Given
        underTest = builder.withMoves("e2e4", "e7e5").buildPuzzle()
        underTest.start()

        // When
        underTest.abandon()

        // Then
        assertEquals(Puzzle.State.Failed, underTest.state)
    }

    @Test
    fun `GIVEN puzzle in progress WHEN getting hint THEN returns correct location`() {
        // Given
        underTest = builder.withMoves("e2e4", "e7e5").buildPuzzle()
        underTest.start()

        // When
        val at = underTest.hint()

        // Then
        assertEquals("e2".loc(), at)
    }

    @Test
    fun `GIVEN puzzle not started WHEN getting hint THEN throws exception`() {
        // Given
        underTest = builder.withMoves("e2e4").buildPuzzle()

        // When & Then
        assertThrows(AssertionError::class.java) {
            underTest.hint()
        }
    }

    @Test
    fun `GIVEN puzzle in progress WHEN playing next move THEN opponent move is executed`() {
        // Given
        underTest = builder.withMoves("e2e4", "e7e5").buildPuzzle()
        underTest.start()

        // When
        underTest.playNextMove()

        // Then
        assertEquals(Puzzle.State.InProgress, underTest.state)
        // Verify the move was actually made on the board
        assertEquals(Piece.Pawn, underTest.board.at("e4".loc()))
        assertNull(underTest.board.at("e2".loc()))
    }

    @Test
    fun `GIVEN puzzle in progress WHEN playing next move with no moves left THEN returns false`() {
        // Given
        underTest = builder.withMoves("e2e4").buildPuzzle()
        underTest.start()

        // When
        underTest.playNextMove()

        // Then
        assertEquals(Puzzle.State.Success, underTest.state)
    }

    @Test
    fun `GIVEN puzzle not started WHEN playing next move THEN throws exception`() {
        // Given
        underTest = builder.withMoves("e2e4").buildPuzzle()

        // When & Then
        assertThrows(AssertionError::class.java) {
            underTest.playNextMove()
        }
    }

    @Test
    fun `GIVEN puzzle failed WHEN playing next move THEN throws exception`() {
        // Given
        underTest = builder.withMoves("e2e4").buildPuzzle()
        underTest.start()
        underTest.play("e2".loc(), "e3".loc()) // This will fail the puzzle

        // When & Then
        assertThrows(AssertionError::class.java) {
            underTest.playNextMove()
        }
    }

    @Test
    fun `GIVEN puzzle completed WHEN playing next move THEN throws exception`() {
        // Given
        underTest = builder.withMoves("e2e4").buildPuzzle()
        underTest.start()
        underTest.play("e2".loc(), "e4".loc()) // This will complete the puzzle

        // When & Then
        assertThrows(AssertionError::class.java) {
            underTest.playNextMove()
        }
    }

    @Test
    fun `GIVEN puzzle with multiple moves WHEN playing next move multiple times THEN all moves are executed`() {
        // Given
        underTest = builder.withMoves("e2e4", "e7e5", "d2d4").buildPuzzle()
        underTest.start()

        // When
        underTest.playNextMove()
        underTest.playNextMove()
        underTest.playNextMove()

        // Then
        assertEquals(Puzzle.State.Success, underTest.state)
        // Verify the move was actually made on the board
        assertEquals(Piece.Pawn, underTest.board.at("e4".loc()))
        assertEquals(Piece.Pawn, underTest.board.at("e5".loc()))
        assertEquals(Piece.Pawn, underTest.board.at("d4".loc()))
        assertNull(underTest.board.at("e2".loc()))
        assertNull(underTest.board.at("e7".loc()))
        assertNull(underTest.board.at("d2".loc()))
    }

    @Test
    fun `GIVEN puzzle WHEN checking isRunning THEN returns correct state`() {
        // Given
        underTest = builder.withMoves("e2e4").buildPuzzle()

        // When & Then
        assertEquals(Puzzle.State.Idle, underTest.state)

        underTest.start()
        assertEquals(Puzzle.State.InProgress, underTest.state)

        underTest.play("e2".loc(), "e4".loc())
        assertEquals(Puzzle.State.Success, underTest.state)
    }
}
