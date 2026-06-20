package com.paulcraciunas.puzzles.di

import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.puzzles.api.PuzzleRepository

/**
 * Deterministic puzzle repository for macrobenchmarks.
 *
 * Provides pre-built puzzles with known move sequences so that benchmark
 * measurements are repeatable across runs. The move data here must stay
 * in sync with the tap sequences in the :benchmark macrobenchmark tests.
 */
internal class BenchmarkPuzzleRepository : PuzzleRepository {
    private val factory = RealGameFactory()

    override suspend fun get(count: Int): List<Puzzle> = mutableListOf<Puzzle>().apply {
        repeat(count) {
            add(buildPuzzle(PUZZLE_ID_BASE + it))
        }
    }

    override suspend fun getById(id: Int): Puzzle? = null
    override suspend fun getByRating(targetRating: Int): Puzzle = buildPuzzle(targetRating)
    override suspend fun getByRatingRange(min: Int, max: Int): Puzzle = buildPuzzle((min + max) / 2)

    private fun buildPuzzle(id: Int): Puzzle =
        factory.builder()
            .withDefaultBoard()
            .withId(id)
            .withRating(id)
            .withMoves(MOVE_LINE)
            .buildPuzzle()

    companion object {
        const val PUZZLE_ID_BASE: Int = 1_000

        /**
         * Full move sequence (alternating White/Black starting with White).
         * The puzzle auto-plays White moves; the benchmark taps Black moves.
         *
         * Position: standard starting position after White plays 1.e4. Black to move.
         * Line: 1...e5 2.Nf3 Nc6 3.Bc4 Bc5 4.c3 Nf6 5.d4 exd4
         */
        val MOVE_LINE: List<String> = listOf(
            "e2e4", // White 1. e4 (setup -- auto-played before puzzle starts)
            "e7e5", // Black 1... e5
            "g1f3", // White 2. Nf3
            "b8c6", // Black 2... Nc6
            "f1c4", // White 3. Bc4
            "f8c5", // Black 3... Bc5
            "c2c3", // White 4. c3
            "g8f6", // Black 4... Nf6
            "d2d4", // White 5. d4
            "e5d4", // Black 5... exd4
        )
    }
}
