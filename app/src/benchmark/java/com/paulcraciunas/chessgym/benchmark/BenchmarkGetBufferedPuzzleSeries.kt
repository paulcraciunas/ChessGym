package com.paulcraciunas.chessgym.benchmark

import com.paulcraciunas.domain.api.puzzles.GetBufferedPuzzleSeries
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.impl.RealGameFactory
import kotlinx.coroutines.CoroutineScope

/**
 * Returns a series of 10 fixed puzzles so the benchmark always exercises the same positions.
 *
 * Each puzzle starts from the standard opening position and has 4 moves
 * (setup, player, response, player) — giving 2 board state transitions per puzzle
 * that the benchmark measures for recomposition performance.
 */
class BenchmarkGetBufferedPuzzleSeries : GetBufferedPuzzleSeries {
    private val puzzles: List<Puzzle> by lazy { buildBenchmarkPuzzles() }
    private var index: Int = 0

    override fun start(scope: CoroutineScope, batchSize: Int, ratingStart: Int, increment: Int) {
        index = 0
    }

    override suspend fun next(): Puzzle {
        check(index < puzzles.size) { "No more benchmark puzzles available" }
        return puzzles[index++]
    }

    private fun buildBenchmarkPuzzles(): List<Puzzle> {
        val factory = RealGameFactory()
        return PUZZLE_DATA.mapIndexed { idx, moves ->
            factory.builder()
                .withDefaultBoard()
                .withId(BENCHMARK_PUZZLE_BASE_ID + idx)
                .withRating(BASE_RATING + idx * RATING_INCREMENT)
                .withMoves(moves)
                .buildPuzzle()
        }
    }

    companion object {
        const val BENCHMARK_PUZZLE_BASE_ID: Int = 8_001
        private const val BASE_RATING: Int = 400
        private const val RATING_INCREMENT: Int = 60

        /**
         * 10 opening puzzles. Each has 4 moves: White setup → Black move → White response → Black move.
         * The puzzle autoplay White moves; the benchmark taps Black moves.
         */
        private val PUZZLE_DATA: List<List<String>> = listOf(
            listOf("e2e4", "e7e5", "g1f3", "b8c6"), // 1. e4 e5 2. Nf3 Nc6
            listOf("d2d4", "d7d5", "c2c4", "e7e6"), // 1. d4 d5 2. c4 e6
            listOf("c2c4", "e7e5", "b1c3", "g8f6"), // 1. c4 e5 2. Nc3 Nf6
            listOf("g1f3", "d7d5", "g2g3", "g8f6"), // 1. Nf3 d5 2. g3 Nf6
            listOf("e2e4", "c7c5", "g1f3", "d7d6"), // 1. e4 c5 2. Nf3 d6
            listOf("d2d4", "g8f6", "c2c4", "e7e6"), // 1. d4 Nf6 2. c4 e6
            listOf("e2e4", "e7e6", "d2d4", "d7d5"), // 1. e4 e6 2. d4 d5
            listOf("d2d4", "f7f5", "c2c4", "g8f6"), // 1. d4 f5 2. c4 Nf6
            listOf("c2c4", "c7c5", "g1f3", "g8f6"), // 1. c4 c5 2. Nf3 Nf6
            listOf("b2b3", "e7e5", "c1b2", "d7d5"), // 1. b3 e5 2. Bb2 d5
        )
    }
}
