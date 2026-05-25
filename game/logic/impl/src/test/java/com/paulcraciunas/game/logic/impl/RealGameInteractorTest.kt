package com.paulcraciunas.game.logic.impl

import com.paulcraciunas.game.logic.api.Builder
import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.plies.PlyFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class RealGameInteractorTest {
    private val builder: Builder = RealBuilder(PlyFactory())

    private val underTest = RealGameInteractor()

    @Test
    fun `GIVEN game WHEN loading THEN game is started and captured is cleared`() {
        // Given
        val game = buildDefaultGame()

        // When
        underTest.load(game, Side.WHITE)

        // Then
        assertEquals(Game.GameState.InProgress, game.state)
        assertTrue(underTest.captured[Side.WHITE]!!.isEmpty())
        assertTrue(underTest.captured[Side.BLACK]!!.isEmpty())
    }

    @Test
    fun `GIVEN loaded game WHEN checking player THEN value matches provided player`() {
        // Given
        val game = buildDefaultGame()

        // When
        underTest.load(game, Side.WHITE)

        // Then
        assertEquals(Side.WHITE, underTest.player)
    }

    @Test
    fun `GIVEN loaded game WHEN checking player as black THEN returns black`() {
        // Given
        val game = buildDefaultGame()

        // When
        underTest.load(game, Side.BLACK)

        // Then
        assertEquals(Side.BLACK, underTest.player)
    }

    @Test
    fun `GIVEN loaded game with rating WHEN checking rating THEN value matches`() {
        // Given
        val game = builder.withDefaultBoard().withRating(DEFAULT_RATING).buildGame()

        // When
        underTest.load(game, Side.WHITE)

        // Then
        assertEquals(DEFAULT_RATING, underTest.rating)
    }

    @Test
    fun `GIVEN loaded game WHEN requesting moves THEN available destinations are returned`() {
        // Given
        loadDefaultGame()
        val from: Locus = Locus.e2

        // When
        val moves: List<Locus> = underTest.moves(from)

        // Then
        assertEquals(setOf(Locus.e3, Locus.e4), moves.toSet())
    }

    @Test
    fun `GIVEN loaded game WHEN requesting moves from knight THEN valid knight moves returned`() {
        // Given
        loadDefaultGame()
        val from: Locus = Locus.g1

        // When
        val moves: List<Locus> = underTest.moves(from)

        // Then
        assertEquals(setOf(Locus.f3, Locus.h3), moves.toSet())
    }

    @Test
    fun `GIVEN loaded game WHEN requesting moves from empty square THEN empty list returned`() {
        // Given
        loadDefaultGame()

        // When
        val moves: List<Locus> = underTest.moves(Locus.e4)

        // Then
        assertTrue(moves.isEmpty())
    }

    @Test
    fun `GIVEN loaded game WHEN checking canPlay THEN valid moves are allowed and invalid are rejected`() {
        // Given
        loadDefaultGame()

        // When
        val canPlayValid: Boolean = underTest.canPlay(Locus.e2, Locus.e4)
        val canPlayInvalid: Boolean = underTest.canPlay(Locus.e2, Locus.e5)

        // Then
        assertTrue(canPlayValid)
        assertFalse(canPlayInvalid)
    }

    @Test
    fun `GIVEN loaded game WHEN playing valid move THEN move is applied to board`() {
        // Given
        val game = buildDefaultGame()
        underTest.load(game, Side.WHITE)

        // When
        underTest.play(Locus.e2, Locus.e4)

        // Then
        assertEquals(Piece.Pawn, game.board.at(Locus.e4))
        assertNull(game.board.at(Locus.e2))
    }

    @Test
    fun `GIVEN loaded game WHEN playing invalid move THEN throws assertion error`() {
        // Given
        loadDefaultGame()

        // When & Then
        assertThrows(AssertionError::class.java) {
            underTest.play(Locus.e2, Locus.e5)
        }
    }

    @Test
    fun `GIVEN loaded game WHEN playing move THEN lastPly is updated`() {
        // Given
        loadDefaultGame()

        // When
        underTest.play(Locus.e2, Locus.e4)

        // Then
        val lastPly = underTest.lastPly
        assertNotNull(lastPly)
        assertEquals(Locus.e2, lastPly!!.from)
        assertEquals(Locus.e4, lastPly.to)
    }

    @Test
    fun `GIVEN loaded game WHEN checking canPromote for normal move THEN returns false`() {
        // Given
        loadDefaultGame()

        // When
        val canPromote: Boolean = underTest.canPromote(Locus.e2, Locus.e4)

        // Then
        assertFalse(canPromote)
    }

    @Test
    fun `GIVEN promotion position WHEN checking canPromote THEN returns true`() {
        // Given
        val game = buildPromotionGame()
        underTest.load(game, Side.WHITE)

        // When
        val canPromote: Boolean = underTest.canPromote(Locus.a7, Locus.a8)

        // Then
        assertTrue(canPromote)
    }

    @Test
    fun `GIVEN promotion position WHEN promoting THEN pawn is promoted`() {
        // Given
        val game = buildPromotionGame()
        underTest.load(game, Side.WHITE)

        // When
        underTest.promote(Locus.a7, Locus.a8, Piece.Queen)

        // Then
        assertEquals(Piece.Queen, game.board.at(Locus.a8))
        assertNull(game.board.at(Locus.a7))
    }

    @Test
    fun `GIVEN white captures black piece WHEN playing THEN captured list is updated`() {
        // Given
        val game = buildDefaultGame()
        underTest.load(game, Side.WHITE)
        underTest.play(Locus.e2, Locus.e4)
        underTest.play(Locus.d7, Locus.d5)

        // When
        underTest.play(Locus.e4, Locus.d5)

        // Then - WHITE captured BLACK's pawn, so it appears in WHITE's captured list
        val capturedByWhite = underTest.captured[Side.WHITE]!!
        val capturedByBlack = underTest.captured[Side.BLACK]!!
        assertTrue(capturedByWhite.contains(Piece.Pawn))
        assertTrue(capturedByBlack.isEmpty())
    }

    @Test
    fun `GIVEN black captures white piece WHEN playing THEN captured list is updated`() {
        // Given
        val game = buildDefaultGame()
        underTest.load(game, Side.WHITE)
        underTest.play(Locus.e2, Locus.e4)
        underTest.play(Locus.d7, Locus.d5)
        underTest.play(Locus.g1, Locus.f3)

        // When
        underTest.play(Locus.d5, Locus.e4)

        // Then - BLACK captured WHITE's pawn, so it appears in BLACK's captured list
        val capturedByWhite = underTest.captured[Side.WHITE]!!
        val capturedByBlack = underTest.captured[Side.BLACK]!!
        assertTrue(capturedByWhite.isEmpty())
        assertTrue(capturedByBlack.contains(Piece.Pawn))
    }

    @Test
    fun `GIVEN game in progress WHEN resigning THEN game is over`() {
        // Given
        loadDefaultGame()

        // When
        underTest.resign()

        // Then
        assertTrue(underTest.isOver())
    }

    @Test
    fun `GIVEN fresh game WHEN checking isOver THEN returns false`() {
        // Given
        loadDefaultGame()

        // Then
        assertFalse(underTest.isOver())
    }

    @Test
    fun `GIVEN loaded game WHEN multiple moves played THEN captured tracks cumulative captures`() {
        // Given
        val game = buildDefaultGame()
        underTest.load(game, Side.WHITE)
        underTest.play(Locus.e2, Locus.e4)
        underTest.play(Locus.d7, Locus.d5)
        underTest.play(Locus.e4, Locus.d5) // White captures pawn

        // When - Black recaptures
        underTest.play(Locus.d8, Locus.d5) // Black captures pawn

        // Then - Both sides have captured one pawn each
        val capturedByWhite = underTest.captured[Side.WHITE]!!
        val capturedByBlack = underTest.captured[Side.BLACK]!!
        assertEquals(1, capturedByWhite.count { it == Piece.Pawn })
        assertEquals(1, capturedByBlack.count { it == Piece.Pawn })
    }

    private fun loadDefaultGame() {
        underTest.load(buildDefaultGame(), Side.WHITE)
    }

    private fun buildDefaultGame(): Game =
        builder.withDefaultBoard().buildGame()

    private fun buildPromotionGame(): Game =
        builder
            .withTurn(Side.WHITE)
            .withPiece(Piece.King, Side.WHITE, Locus.e1)
            .withPiece(Piece.King, Side.BLACK, Locus.e8)
            .withPiece(Piece.Pawn, Side.WHITE, Locus.a7)
            .buildGame()

    private companion object {
        private const val DEFAULT_RATING: Int = 1200
    }
}
