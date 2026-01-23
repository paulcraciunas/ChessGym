package com.paulcraciunas.domain.api

import com.paulcraciunas.game.logic.api.board.Locus

/**
 * Use case for generating random chess square locations.
 *
 * Used by board visualization exercises like "Find the Square" game
 * to generate random target squares for the player to identify.
 */
interface GenerateRandomLoci {

    /**
     * Generates a new random chess square location.
     *
     * @return A random [Locus] representing a square on the chess board (e.g., e4, a1, h8)
     */
    operator fun invoke(): Locus
}
