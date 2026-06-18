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
class PuzzleStreakBenchmark {
    @get:Rule
    val benchmarkRule: MacrobenchmarkRule = MacrobenchmarkRule()

    @Test
    fun puzzleStreak_frameTimings() {
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.DEFAULT,
            startupMode = StartupMode.WARM,
            iterations = 10,
            setupBlock = { launchAndNavigateToPuzzleStreak() },
            measureBlock = { playBlackMoves() },
        )
    }

    private fun MacrobenchmarkScope.launchAndNavigateToPuzzleStreak() {
        pressHome()
        startActivityAndWait()
        device.wait(Until.hasObject(By.res(HOME_SCREEN_TAG)), SCREEN_READY_TIMEOUT_MS)

        device.findObject(By.desc(PUZZLE_DASHBOARD_DESCRIPTION)).click()
        device.wait(Until.hasObject(By.res(PUZZLE_DASHBOARD_SCREEN_TAG)), SCREEN_READY_TIMEOUT_MS)

        device.findObject(By.res(PUZZLE_STREAK_CARD_TAG)).click()
        device.wait(Until.hasObject(By.res(PUZZLE_STREAK_SCREEN_TAG)), SCREEN_READY_TIMEOUT_MS)
    }

    private fun MacrobenchmarkScope.playBlackMoves() {
        BLACK_PLAYER_MOVES.forEach { (from, to) ->
            tapSquare(from)
            device.waitForIdle()
            tapSquare(to)
            device.waitForIdle()
            Thread.sleep(MOVE_SETTLE_MS)
        }
    }

    private fun MacrobenchmarkScope.tapSquare(square: String) {
        val selector = By.res("$SQUARE_TAG_PREFIX$square")
        val squareObject = device.wait(Until.findObject(selector), SQUARE_TIMEOUT_MS)
        squareObject.click()
    }

    companion object {
        private const val TARGET_PACKAGE = "com.paulcraciunas.chessgym.benchmark"

        private const val HOME_SCREEN_TAG = "home_screen"
        private const val PUZZLE_DASHBOARD_DESCRIPTION = "Puzzle Dashboard"
        private const val PUZZLE_DASHBOARD_SCREEN_TAG = "puzzle_dashboard_screen"
        private const val PUZZLE_STREAK_CARD_TAG = "puzzle_dashboard_card_puzzle_streak"
        private const val PUZZLE_STREAK_SCREEN_TAG = "puzzle_streak_screen"
        private const val SQUARE_TAG_PREFIX = "square_"

        /**
         * 5 Black moves matching BenchmarkPuzzleRepository.MOVE_LINE.
         * Each tap-pair triggers: selection recomposition, move animation, opponent auto-move.
         */
        private val BLACK_PLAYER_MOVES: List<Pair<String, String>> = listOf(
            "e7" to "e5",
            "b8" to "c6",
            "f8" to "c5",
            "g8" to "f6",
            "e5" to "d4",
        )

        private const val SCREEN_READY_TIMEOUT_MS = 30_000L
        private const val SQUARE_TIMEOUT_MS = 5_000L
        private const val MOVE_SETTLE_MS = 400L
    }
}
