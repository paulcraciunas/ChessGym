package com.paulcraciunas.game.logic.api.diagnostics

/**
 * Thread-safe container that records moves played during the current puzzle or game.
 * Reset when a new puzzle/game is started.
 * Accessed by the global exception handler to enrich crash reports.
 */
object PlayedMovesLog {
    private val moves: MutableList<String> = mutableListOf()

    @Synchronized
    fun record(algebraicNotation: String) {
        moves.add(algebraicNotation)
    }

    @Synchronized
    fun reset() {
        moves.clear()
    }

    @Synchronized
    fun summary(): String = if (moves.isEmpty()) {
        "No moves played"
    } else {
        moves.joinToString(separator = " ")
    }
}
