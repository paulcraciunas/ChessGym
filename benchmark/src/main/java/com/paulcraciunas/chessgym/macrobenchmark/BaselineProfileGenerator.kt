package com.paulcraciunas.chessgym.macrobenchmark

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generateStartupProfile() = rule.collect(
        packageName = TARGET_PACKAGE,
        includeInStartupProfile = true,
    ) {
        startActivityAndWait()
        device.wait(Until.hasObject(By.res(HOME_SCREEN_TAG)), SCREEN_TIMEOUT_MS)
    }

    @Test
    fun generatePuzzleRushProfile() = rule.collect(
        packageName = TARGET_PACKAGE,
        includeInStartupProfile = false,
    ) {
        startActivityAndWait()
        device.wait(Until.hasObject(By.res(HOME_SCREEN_TAG)), SCREEN_TIMEOUT_MS)

        navigateToPuzzleDashboard()
        openPuzzleRush()
        playPuzzleRushSession()
    }

    @Test
    fun generateSettingsProfile() = rule.collect(
        packageName = TARGET_PACKAGE,
        includeInStartupProfile = false,
    ) {
        startActivityAndWait()
        device.wait(Until.hasObject(By.res(HOME_SCREEN_TAG)), SCREEN_TIMEOUT_MS)

        openDrawer()
        navigateToSettings()
        scrollSettings()
    }

    @Test
    fun generateAnalysisProfile() = rule.collect(
        packageName = TARGET_PACKAGE,
        includeInStartupProfile = false,
    ) {
        startActivityAndWait()
        device.wait(Until.hasObject(By.res(HOME_SCREEN_TAG)), SCREEN_TIMEOUT_MS)

        navigateToToolsDashboard()
        openAnalysis()
        waitForEngineToLoad()
    }

    @Test
    fun generateAchievementsProfile() = rule.collect(
        packageName = TARGET_PACKAGE,
        includeInStartupProfile = false,
    ) {
        startActivityAndWait()
        device.wait(Until.hasObject(By.res(HOME_SCREEN_TAG)), SCREEN_TIMEOUT_MS)

        navigateToAchievements()
        scrollAchievements()
    }

    private fun MacrobenchmarkScope.navigateToPuzzleDashboard() {
        device.findObject(By.text("Puzzles")).parent?.parent?.click()
        device.wait(Until.hasObject(By.res(PUZZLE_DASHBOARD_SCREEN_TAG)), SCREEN_TIMEOUT_MS)
    }

    private fun MacrobenchmarkScope.openPuzzleRush() {
        device.findObject(By.text("Puzzle Rush")).parent?.click()
        device.wait(Until.hasObject(By.res(PUZZLE_RUSH_SCREEN_TAG)), SCREEN_TIMEOUT_MS)
    }

    private fun MacrobenchmarkScope.playPuzzleRushSession() {
        repeat(RUSH_PUZZLE_COUNT) {
            BLACK_PLAYER_MOVES.forEach { (from, to) ->
                tapSquare(from)
                tapSquare(to)
                Thread.sleep(MOVE_SETTLE_MS)
            }
            Thread.sleep(PUZZLE_TRANSITION_MS)
        }
    }

    private fun MacrobenchmarkScope.openDrawer() {
        device.findObject(By.res(HOME_BUTTON_TAG)).click()
        device.waitForIdle()
        Thread.sleep(DRAWER_ANIMATION_MS)
    }

    private fun MacrobenchmarkScope.navigateToSettings() {
        device.findObject(By.text("Settings")).parent?.click()
        device.waitForIdle()
        device.wait(Until.hasObject(By.text(SETTINGS_TITLE)), SCREEN_TIMEOUT_MS)
    }

    private fun MacrobenchmarkScope.scrollSettings() {
        val scrollable = device.wait(Until.findObject(By.scrollable(true)), SCROLL_TIMEOUT_MS)
        scrollable?.scroll(Direction.DOWN, FULL_SCROLL_PERCENT)
        device.waitForIdle()
        scrollable?.scroll(Direction.UP, FULL_SCROLL_PERCENT)
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.navigateToToolsDashboard() {
        device.findObject(By.text("Tools")).parent?.parent?.click()
        device.wait(Until.hasObject(By.res(TOOLS_DASHBOARD_SCREEN_TAG)), SCREEN_TIMEOUT_MS)
    }

    private fun MacrobenchmarkScope.openAnalysis() {
        device.findObject(By.text("Analysis")).parent?.click()
        device.wait(Until.hasObject(By.res(ANALYSIS_SCREEN_TAG)), SCREEN_TIMEOUT_MS)
    }

    private fun MacrobenchmarkScope.waitForEngineToLoad() {
        device.wait(Until.hasObject(By.res(ENGINE_LINES_TAG)), ENGINE_LOAD_TIMEOUT_MS)
        Thread.sleep(ENGINE_SETTLE_MS)
    }

    private fun MacrobenchmarkScope.navigateToAchievements() {
        device.findObject(By.desc(ACHIEVEMENTS_DESCRIPTION)).click()
        device.wait(Until.hasObject(By.res(ACHIEVEMENTS_SCREEN_TAG)), SCREEN_TIMEOUT_MS)
    }

    private fun MacrobenchmarkScope.scrollAchievements() {
        Thread.sleep(SETTLE_MS)
        val scrollable = device.wait(Until.findObject(By.scrollable(true)), SCROLL_TIMEOUT_MS)
        scrollable?.scroll(Direction.DOWN, FULL_SCROLL_PERCENT)
        device.waitForIdle()
        scrollable?.scroll(Direction.DOWN, FULL_SCROLL_PERCENT)
        device.waitForIdle()
        scrollable?.scroll(Direction.DOWN, FULL_SCROLL_PERCENT)
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.tapSquare(square: String) {
        val obj = device.wait(Until.findObject(By.res("$SQUARE_TAG_PREFIX$square")), SQUARE_TIMEOUT_MS)
        obj.click()
        device.waitForIdle()
    }

    companion object {
        private const val TARGET_PACKAGE = "com.paulcraciunas.chessgym.benchmark"

        private const val HOME_SCREEN_TAG = "home_screen"
        private const val HOME_BUTTON_TAG = "app_bar_home_button"
        private const val SETTINGS_TITLE = "Settings"

        private const val PUZZLE_DASHBOARD_SCREEN_TAG = "puzzle_dashboard_screen"
        private const val PUZZLE_RUSH_SCREEN_TAG = "puzzle_rush_screen"

        private const val TOOLS_DASHBOARD_SCREEN_TAG = "tools_dashboard_screen"
        private const val ANALYSIS_SCREEN_TAG = "analysis_screen"
        private const val ENGINE_LINES_TAG = "analysis_engine_lines"

        private const val ACHIEVEMENTS_DESCRIPTION = "Achievements"
        private const val ACHIEVEMENTS_SCREEN_TAG = "achievements_screen"

        private const val SQUARE_TAG_PREFIX = "square_"

        private val BLACK_PLAYER_MOVES: List<Pair<String, String>> = listOf(
            "e7" to "e5",
            "b8" to "c6",
            "f8" to "c5",
            "g8" to "f6",
            "e5" to "d4",
        )
        private const val RUSH_PUZZLE_COUNT = 3

        private const val SCREEN_TIMEOUT_MS = 10_000L
        private const val SCROLL_TIMEOUT_MS = 5_000L
        private const val SQUARE_TIMEOUT_MS = 5_000L
        private const val ENGINE_LOAD_TIMEOUT_MS = 10_000L
        private const val ENGINE_SETTLE_MS = 2_000L
        private const val SETTLE_MS = 1_000L
        private const val MOVE_SETTLE_MS = 400L
        private const val PUZZLE_TRANSITION_MS = 600L
        private const val DRAWER_ANIMATION_MS = 500L
        private const val FULL_SCROLL_PERCENT = 100f
    }
}
