package com.paulcraciunas.chessgym.macrobenchmark

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
            setupBlock = { launchAndNavigateToPuzzleRush() },
            measureBlock = { playTenPuzzlesWithFinalFailure() },
        )
    }

    private fun MacrobenchmarkScope.launchAndNavigateToPuzzleRush() {
        pressHome()
        startActivityAndWait()
        device.wait(Until.hasObject(By.res(HOME_SCREEN_TAG)), SCREEN_READY_TIMEOUT_MS)

        device.findObject(By.text("Puzzles")).parent?.parent?.click()
        device.wait(Until.hasObject(By.res(PUZZLE_DASHBOARD_SCREEN_TAG)), SCREEN_READY_TIMEOUT_MS)

        device.findObject(By.text("Puzzle Rush")).parent?.click()
        device.wait(Until.hasObject(By.res(PUZZLE_RUSH_SCREEN_TAG)), SCREEN_READY_TIMEOUT_MS)
    }

    private fun MacrobenchmarkScope.playTenPuzzlesWithFinalFailure() {
        repeat(CORRECT_PLAY_COUNT) {
            moveLine.forEach { move(it.first, it.second) }
            Thread.sleep(PUZZLE_TRANSITION_MS)
        }
        // fail final puzzle
        for (i in 0 until moveLine.size - 1) {
            move(moveLine[i].first, moveLine[i].second)
        }
        move(wrongFinalMove.first, wrongFinalMove.second)
    }

    private fun MacrobenchmarkScope.move(from: String, to: String) {
        tapSquare(from)
        device.waitForIdle()
        tapSquare(to)
        device.waitForIdle()
        Thread.sleep(MOVE_SETTLE_MS)
    }

    private fun MacrobenchmarkScope.tapSquare(square: String) {
        val selector = By.res("$SQUARE_TAG_PREFIX$square")
        val squareObject = device.wait(Until.findObject(selector), SQUARE_TIMEOUT_MS)
        squareObject.click()
    }

    companion object {
        private const val TARGET_PACKAGE = "com.paulcraciunas.chessgym.benchmark"

        private const val HOME_SCREEN_TAG = "home_screen"
        private const val PUZZLE_DASHBOARD_SCREEN_TAG = "puzzle_dashboard_screen"
        private const val PUZZLE_RUSH_SCREEN_TAG = "puzzle_rush_screen"
        private const val SQUARE_TAG_PREFIX = "square_"

        /**
         * Correct Black moves for all puzzles.
         * Must stay in sync with BenchmarkPuzzleRepository.MOVE_LINE.
         */
        private val moveLine: List<Pair<String, String>> = listOf(
            // "e2" to "e4", // White 1. e4 (setup -- auto-played before puzzle starts)
            "e7" to "e5", // Black 1... e5
            // "g1" to "f3", // White 2. Nf3
            "b8" to "c6", // Black 2... Nc6
            // "f1" to "c4", // White 3. Bc4
            "f8" to "c5", // Black 3... Bc5
            // "c2" to "c3", // White 4. c3
            "g8" to "f6", // Black 4... Nf6
            // "d2" to "d4", // White 5. d4
            "e5" to "d4", // Black 5... exd4
        )
        private const val CORRECT_PLAY_COUNT = 9

        /** Wrong move for puzzle 10 to end the rush. Correct would be e5->d4. */
        private val wrongFinalMove: Pair<String, String> = "a7" to "a6"

        private const val SCREEN_READY_TIMEOUT_MS = 10_000L
        private const val SQUARE_TIMEOUT_MS = 3_000L
        private const val MOVE_SETTLE_MS = 1000L
        private const val PUZZLE_TRANSITION_MS = 600L
    }
}
