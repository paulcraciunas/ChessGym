package com.paulcraciunas.game.logic.impl

import com.paulcraciunas.game.logic.api.Builder
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.loc
import com.paulcraciunas.game.logic.impl.plies.PlyFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class RealPuzzleInteractorTest {
    private val builder: Builder = RealBuilder(PlyFactory()).withRating(DEFAULT_RATING)

    private val underTest = RealPuzzleInteractor()

    @Test
    fun `GIVEN puzzle WHEN loading THEN opponent move is executed and captured is cleared`() {
        // Given
        val puzzle = buildDefaultPuzzle(listOf("e2e4", "e7e5", "g1f3"))

        // When
        underTest.load(puzzle)

        // Then
        assertEquals(Piece.Pawn, puzzle.board.at("e4".loc()))
        assertNull(puzzle.board.at("e2".loc()))
        assertTrue(underTest.captured[Side.WHITE].isNullOrEmpty())
        assertTrue(underTest.captured[Side.BLACK].isNullOrEmpty())
    }

    @Test
    fun `GIVEN loaded puzzle WHEN checking rating and player THEN values match puzzle`() {
        // Given
        val puzzle = buildDefaultPuzzle(listOf("e2e4", "e7e5", "g1f3"))

        // When
        underTest.load(puzzle)

        // Then
        assertEquals(DEFAULT_RATING, underTest.rating)
        assertEquals(puzzle.player, underTest.player)
    }

    @Test
    fun `GIVEN loaded puzzle WHEN requesting moves THEN available destinations are returned`() {
        // Given
        loadDefaultPuzzle(listOf("e2e4", "e7e5", "g1f3"))
        val from: Locus = "e7".loc()

        // When
        val moves: List<Locus> = underTest.moves(from)

        // Then
        assertEquals(setOf("e6".loc(), "e5".loc()), moves.toSet())
    }

    @Test
    fun `GIVEN loaded puzzle WHEN checking canPlay THEN valid moves are allowed and invalid are rejected`() {
        // Given
        loadDefaultPuzzle(listOf("e2e4", "e7e5", "g1f3"))

        // When
        val canPlayValid: Boolean = underTest.canPlay("e7".loc(), "e5".loc())
        val canPlayInvalid: Boolean = underTest.canPlay("e7".loc(), "e4".loc())

        // Then
        assertTrue(canPlayValid)
        assertFalse(canPlayInvalid)
    }

    @Test
    fun `GIVEN loaded puzzle WHEN playing expected move THEN player and opponent moves are applied`() {
        // Given
        val puzzle = buildDefaultPuzzle(listOf("e2e4", "e7e5", "g1f3"))
        val underTest = RealPuzzleInteractor().apply { load(puzzle) }

        // When
        underTest.play("e7".loc(), "e5".loc())

        // Then
        assertEquals(Piece.Pawn, puzzle.board.at("e5".loc()))
        assertEquals(Piece.Knight, puzzle.board.at("f3".loc()))
        assertNull(puzzle.board.at("e7".loc()))
        assertNull(puzzle.board.at("g1".loc()))
    }

    @Test
    fun `GIVEN loaded puzzle WHEN playing invalid move THEN throws assertion error`() {
        // Given
        loadDefaultPuzzle(listOf("e2e4", "e7e5", "g1f3"))

        // When & Then
        assertThrows(AssertionError::class.java) {
            underTest.play("e7".loc(), "e4".loc())
        }
    }

    @Test
    fun `GIVEN loaded puzzle WHEN requesting hint THEN next expected move is returned`() {
        // Given
        loadDefaultPuzzle(listOf("e2e4", "e7e5", "g1f3"))

        // When
        val hint: Locus = underTest.hint()

        // Then
        assertEquals("e7".loc(), hint)
    }

    @Test
    fun `GIVEN failed puzzle WHEN requesting hint THEN throws assertion error`() {
        // Given
        loadDefaultPuzzle(listOf("e2e4", "e7e5", "g1f3"))
        underTest.resign()

        // When & Then
        assertThrows(AssertionError::class.java) {
            underTest.hint()
        }
    }

    @Test
    fun `GIVEN loaded puzzle WHEN checking canPromote THEN promotion is not available`() {
        // Given
        loadDefaultPuzzle(listOf("e2e4", "e7e5", "g1f3"))

        // When
        val canPromote: Boolean = underTest.canPromote("e7".loc(), "e5".loc())

        // Then
        assertFalse(canPromote)
    }

    @Test
    fun `GIVEN promotion move WHEN promoting THEN pawn is promoted and puzzle progresses`() {
        // Given
        val puzzle = buildPromotionPuzzle()
        val underTest = RealPuzzleInteractor().apply { load(puzzle) }

        // When
        underTest.promote("a7".loc(), "a8".loc(), Piece.Queen)

        // Then
        assertEquals(Piece.Queen, puzzle.board.at("a8".loc()))
        assertNull(puzzle.board.at("a7".loc()))
        assertTrue(underTest.isSuccess())
    }

    @Test
    fun `GIVEN loaded puzzle WHEN playing capture THEN captured list is updated`() {
        // Given
        val puzzle = buildDefaultPuzzle(listOf("e2e4", "d7d5", "e4d5"))
        val underTest = RealPuzzleInteractor().apply { load(puzzle) }

        // When
        underTest.play("d7".loc(), "d5".loc())

        // Then
        val capturedBlack: List<Piece> = underTest.captured[Side.BLACK] ?: emptyList()
        val capturedWhite: List<Piece> = underTest.captured[Side.WHITE] ?: emptyList()
        assertEquals(listOf(Piece.Pawn), capturedBlack)
        assertTrue(capturedWhite.isEmpty())
    }

    @Test
    fun `GIVEN loaded puzzle WHEN resigning THEN puzzle is over and not successful`() {
        // Given
        loadDefaultPuzzle(listOf("e2e4", "e7e5", "g1f3"))

        // When
        underTest.resign()

        // Then
        assertTrue(underTest.isOver())
        assertFalse(underTest.isSuccess())
    }

    private fun loadDefaultPuzzle(moves: List<String>) {
        underTest.load(buildDefaultPuzzle(moves))
    }

    private fun buildDefaultPuzzle(moves: List<String>): Puzzle =
        builder.withDefaultBoard().withMoves(moves).buildPuzzle()

    private fun buildPromotionPuzzle(): Puzzle =
        builder
            .withTurn(Side.BLACK)
            .withPiece(Piece.King, Side.WHITE, "e1".loc())
            .withPiece(Piece.King, Side.BLACK, "e8".loc())
            .withPiece(Piece.Pawn, Side.WHITE, "a7".loc())
            .withMoves(listOf("e8e7", "a7a8q", "e7e6"))
            .buildPuzzle()

    private companion object {
        private const val DEFAULT_RATING: Int = 1200
    }
}
