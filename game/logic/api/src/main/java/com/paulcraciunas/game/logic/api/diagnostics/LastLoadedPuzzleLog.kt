package com.paulcraciunas.game.logic.api.diagnostics

/**
 * Thread-safe container that records the last puzzle fetched from the database.
 * Accessed by the global exception handler to enrich crash reports.
 */
object LastLoadedPuzzleLog {
    @Volatile
    var id: Int? = null
        private set

    @Volatile
    var rating: Int? = null
        private set

    @Volatile
    var fen: String? = null
        private set

    fun recordFen(fen: String) {
        this.fen = fen
    }

    fun record(id: Int?, rating: Int) {
        this.id = id
        this.rating = rating
    }

    fun summary(): String = "Puzzle(id=$id, rating=$rating, fen=${fen.toString()})"
}
