package com.paulcraciunas.game.logic.impl

import com.paulcraciunas.game.logic.api.Builder
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
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
        assertEquals(Piece.Pawn, puzzle.board.at(Locus.e4))
        assertNull(puzzle.board.at(Locus.e2))
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
        val from: Locus = Locus.e7

        // When
        val moves: List<Locus> = underTest.moves(from)

        // Then
        assertEquals(setOf(Locus.e6, Locus.e5), moves.toSet())
    }

    @Test
    fun `GIVEN loaded puzzle WHEN checking canPlay THEN valid moves are allowed and invalid are rejected`() {
        // Given
        loadDefaultPuzzle(listOf("e2e4", "e7e5", "g1f3"))

        // When
        val canPlayValid: Boolean = underTest.canPlay(Locus.e7, Locus.e5)
        val canPlayInvalid: Boolean = underTest.canPlay(Locus.e7, Locus.e4)

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
        underTest.play(Locus.e7, Locus.e5)

        // Then
        assertEquals(Piece.Pawn, puzzle.board.at(Locus.e5))
        assertEquals(Piece.Knight, puzzle.board.at(Locus.f3))
        assertNull(puzzle.board.at(Locus.e7))
        assertNull(puzzle.board.at(Locus.g1))
    }

    @Test
    fun `GIVEN loaded puzzle WHEN playing invalid move THEN throws assertion error`() {
        // Given
        loadDefaultPuzzle(listOf("e2e4", "e7e5", "g1f3"))

        // When & Then
        assertThrows(AssertionError::class.java) {
            underTest.play(Locus.e7, Locus.e4)
        }
    }

    @Test
    fun `GIVEN loaded puzzle WHEN requesting hint THEN next expected move is returned`() {
        // Given
        loadDefaultPuzzle(listOf("e2e4", "e7e5", "g1f3"))

        // When
        val hint: Locus = underTest.hint()

        // Then
        assertEquals(Locus.e7, hint)
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
        val canPromote: Boolean = underTest.canPromote(Locus.e7, Locus.e5)

        // Then
        assertFalse(canPromote)
    }

    @Test
    fun `GIVEN promotion move WHEN promoting THEN pawn is promoted and puzzle progresses`() {
        // Given
        val puzzle = buildPromotionPuzzle()
        val underTest = RealPuzzleInteractor().apply { load(puzzle) }

        // When
        underTest.promote(Locus.a7, Locus.a8, Piece.Queen)

        // Then
        assertEquals(Piece.Queen, puzzle.board.at(Locus.a8))
        assertNull(puzzle.board.at(Locus.a7))
        assertTrue(underTest.isSuccess())
    }

    @Test
    fun `GIVEN loaded puzzle WHEN white captures black piece THEN captured is in white list`() {
        // Given - e2e4, d7d5, e4d5 means WHITE captures BLACK's pawn
        val puzzle = buildDefaultPuzzle(listOf("e2e4", "d7d5", "e4d5"))
        val underTest = RealPuzzleInteractor().apply { load(puzzle) }

        // When
        underTest.play(Locus.d7, Locus.d5)

        // Then - WHITE captured BLACK's pawn, so it appears in WHITE's captured list
        val capturedByWhite = underTest.captured[Side.WHITE]!!
        val capturedByBlack = underTest.captured[Side.BLACK]!!
        assertEquals(listOf(Piece.Pawn), capturedByWhite)
        assertTrue(capturedByBlack.isEmpty())
    }

    @Test
    fun `GIVEN loaded puzzle WHEN black captures white piece THEN captured is in black list`() {
        // Given - e2e4, d7d5, g1f3, d5e4 means BLACK captures WHITE's pawn
        val puzzle = buildDefaultPuzzle(listOf("e2e4", "d7d5", "g1f3", "d5e4"))
        val underTest = RealPuzzleInteractor().apply { load(puzzle) }

        // When
        underTest.play(Locus.d7, Locus.d5)
        underTest.play(Locus.d5, Locus.e4)

        // Then - BLACK captured WHITE's pawn, so it appears in BLACK's captured list
        val capturedByWhite = underTest.captured[Side.WHITE]!!
        val capturedByBlack = underTest.captured[Side.BLACK]!!
        assertTrue(capturedByWhite.isEmpty())
        assertEquals(listOf(Piece.Pawn), capturedByBlack)
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

    @Test
    fun `GIVEN loaded puzzle WHEN playing wrong move THEN puzzle fails without crash`() {
        // Given
        loadDefaultPuzzle(listOf("e2e4", "e7e5", "g1f3"))

        // When - play a valid but incorrect move (e6 instead of expected e5)
        underTest.play(Locus.e7, Locus.e6)

        // Then - puzzle should be failed, no crash from attempting playNextMove
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
            .withPiece(Piece.King, Side.WHITE, Locus.e1)
            .withPiece(Piece.King, Side.BLACK, Locus.e8)
            .withPiece(Piece.Pawn, Side.WHITE, Locus.a7)
            .withMoves(listOf("e8e7", "a7a8q", "e7e6"))
            .buildPuzzle()

    private companion object {
        private const val DEFAULT_RATING: Int = 1200
    }
}
