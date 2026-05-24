package com.paulcraciunas.chessgym.benchmark

import com.paulcraciunas.domain.api.puzzles.GetStreakPuzzle
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.impl.RealGameFactory

/**
 * Returns a fixed puzzle so v1 and v2 benchmarks exercise the same position and solution.
 *
 * The puzzle has 5 player (Black) moves interleaved with 5 auto-played White moves,
 * giving 10 board state transitions to measure recomposition performance across.
 *
 * Position: standard starting position after White plays 1.e4. Black to move.
 * Line: 1...e5 2.Nf3 Nc6 3.Bc4 Bc5 4.c3 Nf6 5.d4 exd4
 */
class BenchmarkGetStreakPuzzle : GetStreakPuzzle {
    private val puzzle: Puzzle by lazy { buildBenchmarkPuzzle() }

    override suspend fun invoke(): GetStreakPuzzle.Data =
        GetStreakPuzzle.Data(
            puzzle = puzzle,
            currentStreakCount = 0,
        )

    private fun buildBenchmarkPuzzle(): Puzzle =
        RealGameFactory().builder()
            .withDefaultBoard()
            .withId(BENCHMARK_PUZZLE_ID)
            .withRating(GetStreakPuzzle.BASE_RATING)
            .withMoves(BENCHMARK_MOVE_LINE)
            .buildPuzzle()

    companion object {
        const val BENCHMARK_PUZZLE_ID: Int = 9_001

        /**
         * Full sequence of moves (alternating White/Black starting with White).
         * The puzzle auto-plays White moves; the benchmark taps Black moves.
         */
        private val BENCHMARK_MOVE_LINE: List<String> = listOf(
            "e2e4", // White 1. e4  (setup — auto-played before puzzle starts)
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

        /**
         * The Black player's moves as source→destination square pairs.
         * Must be kept in sync with [BENCHMARK_MOVE_LINE] and [PuzzleStreakBenchmark].
         */
        val BLACK_PLAYER_MOVES: List<Pair<String, String>> = listOf(
            "e7" to "e5",
            "b8" to "c6",
            "f8" to "c5",
            "g8" to "f6",
            "e5" to "d4",
        )
    }
}
