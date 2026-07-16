package com.paulcraciunas.game.logic

import com.paulcraciunas.game.logic.api.Builder
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
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
        underTest.play(Locus.e2, Locus.e4)

        // Then
        assertEquals(Puzzle.State.InProgress, underTest.state)
    }

    @Test
    fun `GIVEN puzzle in progress WHEN playing incorrect move THEN state becomes Failed`() {
        // Given
        underTest = builder.withMoves("e2e4", "e7e5").buildPuzzle()
        underTest.start()

        // When
        underTest.play(Locus.e2, Locus.e3) // Wrong move

        // Then
        assertEquals(Puzzle.State.Failed, underTest.state)
    }

    @Test
    fun `GIVEN puzzle with one move WHEN playing correct move THEN state becomes Success`() {
        // Given
        underTest = builder.withMoves("e2e4").buildPuzzle()
        underTest.start()

        // When
        underTest.play(Locus.e2, Locus.e4)

        // Then
        assertEquals(Puzzle.State.Success, underTest.state)
    }

    @Test
    fun `GIVEN puzzle in progress WHEN resigning THEN state becomes Failed`() {
        // Given
        underTest = builder.withMoves("e2e4", "e7e5").buildPuzzle()
        underTest.start()

        // When
        underTest.resign()

        // Then
        assertEquals(Puzzle.State.Failed, underTest.state)
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
        assertEquals(Piece.Pawn, underTest.board.at(Locus.e4))
        assertNull(underTest.board.at(Locus.e2))
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
        underTest.play(Locus.e2, Locus.e3) // This will fail the puzzle

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
        underTest.play(Locus.e2, Locus.e4) // This will complete the puzzle

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
        assertEquals(Piece.Pawn, underTest.board.at(Locus.e4))
        assertEquals(Piece.Pawn, underTest.board.at(Locus.e5))
        assertEquals(Piece.Pawn, underTest.board.at(Locus.d4))
        assertNull(underTest.board.at(Locus.e2))
        assertNull(underTest.board.at(Locus.e7))
        assertNull(underTest.board.at(Locus.d2))
    }

    @Test
    fun `GIVEN puzzle WHEN checking isRunning THEN returns correct state`() {
        // Given
        underTest = builder.withMoves("e2e4").buildPuzzle()

        // When & Then
        assertEquals(Puzzle.State.Idle, underTest.state)

        underTest.start()
        assertEquals(Puzzle.State.InProgress, underTest.state)

        underTest.play(Locus.e2, Locus.e4)
        assertEquals(Puzzle.State.Success, underTest.state)
    }

    @Test
    fun `GIVEN alternate checkmate move WHEN playing wrong but checkmate move THEN state becomes Success`() {
        // White: Ka1, Qh7, Rd1, Rf1. Black: Ke8.
        // Expected: Qd7# (defended by Rd1; Kf8 is blocked by rook at f1)
        // Alternate: Qf7# (same thing as above, it's symmetrical)
        underTest = RealBuilder(PlyFactory())
            .withRating(1200)
            .withPiece(Piece.King, Side.WHITE, Locus.a1)
            .withPiece(Piece.King, Side.BLACK, Locus.e8)
            .withPiece(Piece.Queen, Side.WHITE, Locus.h7)
            .withPiece(Piece.Rook, Side.WHITE, Locus.d1)
            .withPiece(Piece.Rook, Side.WHITE, Locus.f1)
            .withMoves("h7d7") // Expected: Qd7#
            .buildPuzzle()
        underTest.start()

        // When - play alternate checkmate: Qf7#
        underTest.play(Locus.h7, Locus.f7)

        // Then
        assertEquals(Puzzle.State.Success, underTest.state)
    }

    @Test
    fun `GIVEN last move gives check but not checkmate WHEN playing wrong move THEN state becomes Failed`() {
        // Regression: a wrong last move that gave check was incorrectly accepted as success
        // because plies were cleared before the checkmate check ran.
        // Position: White Kg1, Qd1, Rook a7. Black Ke8.
        // Expected: Qd1-d7 (check). Wrong move: Qd1-d8 (gives check but black king can recapture).
        underTest = RealBuilder(PlyFactory())
            .withRating(1000)
            .withPiece(Piece.King, Side.WHITE, Locus.g1)
            .withPiece(Piece.Queen, Side.WHITE, Locus.d1)
            .withPiece(Piece.Rook, Side.WHITE, Locus.a7)
            .withPiece(Piece.King, Side.BLACK, Locus.e8)
            .withMoves("d1d7") // Expected: Queen to d7 (check with rook on a7)
            .buildPuzzle()
        underTest.start()

        // When - play Qd8+ (check, but king can capture queen)
        underTest.play(Locus.d1, Locus.d8)

        // Then - must be Failed, not Success
        assertEquals(Puzzle.State.Failed, underTest.state)
    }

    @Test
    fun `GIVEN non-checkmate alternate move WHEN playing wrong move THEN state becomes Failed`() {
        // Given
        underTest = RealBuilder(PlyFactory())
            .withRating(1200)
            .withPiece(Piece.King, Side.WHITE, Locus.e1)
            .withPiece(Piece.Queen, Side.WHITE, Locus.d1)
            .withPiece(Piece.King, Side.BLACK, Locus.e8)
            .withMoves("d1d4") // Expected move
            .buildPuzzle()
        underTest.start()

        // When - play a different move that doesn't result in checkmate
        underTest.play(Locus.d1, Locus.d2)

        // Then
        assertEquals(Puzzle.State.Failed, underTest.state)
    }
}
