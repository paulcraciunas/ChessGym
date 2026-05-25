package com.paulcraciunas.game.logic.api

import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.impl.RealGameFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PlyAlgebraicExtensionTest {
    private val gameFactory = RealGameFactory()

    @Test
    fun `GIVEN empty list WHEN algebraic THEN returns empty string`() {
        val result = emptyList<Ply>().algebraic()

        assertEquals("", result)
    }

    @Test
    fun `GIVEN single white move WHEN algebraic THEN returns numbered move`() {
        val game = newGame()
        game.play(Locus.e2, Locus.e4)

        val result = game.history.algebraic()

        assertEquals("1. e4", result)
    }

    @Test
    fun `GIVEN one full move WHEN algebraic THEN returns white and black moves`() {
        val game = newGame()
        game.play(Locus.e2, Locus.e4)
        game.play(Locus.e7, Locus.e5)

        val result = game.history.algebraic()

        assertEquals("1. e4 e5", result)
    }

    @Test
    fun `GIVEN two full moves WHEN algebraic THEN returns both numbered moves`() {
        val game = newGame()
        game.play(Locus.e2, Locus.e4)
        game.play(Locus.e7, Locus.e5)
        game.play(Locus.g1, Locus.f3)
        game.play(Locus.b8, Locus.c6)

        val result = game.history.algebraic()

        assertEquals("1. e4 e5 2. Nf3 Nc6", result)
    }

    @Test
    fun `GIVEN three full moves WHEN algebraic THEN formats correctly`() {
        val game = newGame()
        game.play(Locus.e2, Locus.e4)
        game.play(Locus.e7, Locus.e5)
        game.play(Locus.g1, Locus.f3)
        game.play(Locus.b8, Locus.c6)
        game.play(Locus.f1, Locus.b5)

        val result = game.history.algebraic()

        assertTrue(result.startsWith("1. e4 e5 2. Nf3 Nc6 3. Bb5"))
    }

    private fun newGame(): Game {
        val game = gameFactory.builder().withDefaultBoard().buildGame()
        game.start()
        return game
    }
}
