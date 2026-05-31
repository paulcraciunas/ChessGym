package com.paulcraciunas.chessgym.macrobenchmark

import android.content.Intent
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PuzzleRushBenchmark {
    @get:Rule
    val benchmarkRule: MacrobenchmarkRule = MacrobenchmarkRule()

    @Test
    fun puzzleRush_frameTimings() {
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.DEFAULT,
            startupMode = StartupMode.WARM,
            iterations = 10,
            setupBlock = { startBenchmarkActivity() },
            measureBlock = { playTenPuzzlesWithFinalFailure() },
        )
    }

    private fun MacrobenchmarkScope.startBenchmarkActivity() {
        pressHome()
        startActivityAndWait(
            Intent(ACTION_BENCHMARK).apply {
                setPackage(TARGET_PACKAGE)
            },
        )
        device.wait(
            Until.hasObject(By.res(SCREEN_TAG)),
            SCREEN_READY_TIMEOUT_MS,
        )
    }

    private fun MacrobenchmarkScope.playTenPuzzlesWithFinalFailure() {
        CORRECT_MOVES.forEachIndexed { puzzleIndex, moves ->
            moves.forEach { (from, to) ->
                tapSquare(from)
                device.waitForIdle()
                tapSquare(to)
                device.waitForIdle()
                Thread.sleep(MOVE_SETTLE_MS)
            }
            if (puzzleIndex < CORRECT_MOVES.size - 1) {
                Thread.sleep(PUZZLE_TRANSITION_MS)
            }
        }
        // Final wrong move on puzzle 10 to end the rush
        val (wrongFrom, wrongTo) = WRONG_FINAL_MOVE
        tapSquare(wrongFrom)
        device.waitForIdle()
        tapSquare(wrongTo)
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.tapSquare(square: String) {
        val selector = By.res("$SQUARE_TAG_PREFIX$square")
        val squareObject = device.wait(Until.findObject(selector), SQUARE_TIMEOUT_MS)
        squareObject.click()
    }

    companion object {
        private const val TARGET_PACKAGE = "com.paulcraciunas.chessgym.benchmark"
        private const val ACTION_BENCHMARK = "com.paulcraciunas.chessgym.BENCHMARK_PUZZLE_RUSH"

        private const val SCREEN_TAG = "puzzle_rush_screen"
        private const val SQUARE_TAG_PREFIX = "square_"

        /**
         * Correct Black moves for puzzles 1–9 (each puzzle has 2 player moves).
         * Puzzle 10 only gets its first correct move here; the second is [WRONG_FINAL_MOVE].
         * Must be kept in sync with BenchmarkGetBufferedPuzzleSeries.BLACK_PLAYER_MOVES.
         */
        private val CORRECT_MOVES: List<List<Pair<String, String>>> = listOf(
            listOf("e7" to "e5", "b8" to "c6"), // Puzzle 1
            listOf("d7" to "d5", "e7" to "e6"), // Puzzle 2
            listOf("e7" to "e5", "g8" to "f6"), // Puzzle 3
            listOf("d7" to "d5", "g8" to "f6"), // Puzzle 4
            listOf("c7" to "c5", "d7" to "d6"), // Puzzle 5
            listOf("g8" to "f6", "e7" to "e6"), // Puzzle 6
            listOf("e7" to "e6", "d7" to "d5"), // Puzzle 7
            listOf("f7" to "f5", "g8" to "f6"), // Puzzle 8
            listOf("c7" to "c5", "g8" to "f6"), // Puzzle 9
            listOf("e7" to "e5"),                // Puzzle 10 — first move only
        )

        /** Wrong move for puzzle 10. Correct is d7→d5; we play a7→a6. */
        private val WRONG_FINAL_MOVE: Pair<String, String> = "a7" to "a6"

        private const val SCREEN_READY_TIMEOUT_MS = 10_000L
        private const val SQUARE_TIMEOUT_MS = 5_000L
        private const val MOVE_SETTLE_MS = 400L
        private const val PUZZLE_TRANSITION_MS = 600L
    }
}
