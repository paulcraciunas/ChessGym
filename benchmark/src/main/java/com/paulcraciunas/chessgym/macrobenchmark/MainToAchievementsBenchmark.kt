package com.paulcraciunas.chessgym.macrobenchmark

import android.content.Intent
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainToAchievementsBenchmark {
    @get:Rule
    val benchmarkRule: MacrobenchmarkRule = MacrobenchmarkRule()

    @Test
    fun mainToAchievements_frameTimings() {
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.DEFAULT,
            startupMode = StartupMode.WARM,
            iterations = 10,
            setupBlock = { startBenchmarkActivity() },
            measureBlock = { navigateToAchievementsAndScroll() },
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
            Until.hasObject(By.res(HOME_SCREEN_TAG)),
            SCREEN_READY_TIMEOUT_MS,
        )
    }

    private fun MacrobenchmarkScope.navigateToAchievementsAndScroll() {
        val achievementsButton = device.wait(
            Until.findObject(By.desc(ACHIEVEMENTS_BUTTON_DESCRIPTION)),
            BUTTON_TIMEOUT_MS,
        )
        achievementsButton.click()
        device.waitForIdle()

        device.wait(
            Until.hasObject(By.res(ACHIEVEMENTS_SCREEN_TAG)),
            SCREEN_READY_TIMEOUT_MS,
        )

        Thread.sleep(SETTLE_DELAY_MS)

        val scrollable = device.wait(
            Until.findObject(By.scrollable(true)),
            SCROLL_TIMEOUT_MS,
        )
        scrollable.scroll(Direction.DOWN, FULL_SCROLL_PERCENT)
        device.waitForIdle()
        scrollable.scroll(Direction.DOWN, FULL_SCROLL_PERCENT)
        device.waitForIdle()
        scrollable.scroll(Direction.DOWN, FULL_SCROLL_PERCENT)
        device.waitForIdle()
    }

    companion object {
        private const val TARGET_PACKAGE = "com.paulcraciunas.chessgym.benchmark"
        private const val ACTION_BENCHMARK =
            "com.paulcraciunas.chessgym.BENCHMARK_MAIN_TO_ACHIEVEMENTS"

        private const val HOME_SCREEN_TAG = "home_screen"
        private const val ACHIEVEMENTS_SCREEN_TAG = "benchmark_achievements_screen"
        private const val ACHIEVEMENTS_BUTTON_DESCRIPTION = "Achievements"

        private const val SCREEN_READY_TIMEOUT_MS = 10_000L
        private const val BUTTON_TIMEOUT_MS = 5_000L
        private const val SCROLL_TIMEOUT_MS = 5_000L
        private const val SETTLE_DELAY_MS = 1_000L
        private const val FULL_SCROLL_PERCENT = 100f
    }
}
