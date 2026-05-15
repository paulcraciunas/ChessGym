package com.paulcraciunas.chessgym.screens.tools

import com.paulcraciunas.chessgym.base.BaseUiTest
import com.paulcraciunas.chessgym.dsl.Given
import com.paulcraciunas.chessgym.dsl.Then
import com.paulcraciunas.chessgym.dsl.When
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Test

@HiltAndroidTest
internal class AnalysisScreenTest : BaseUiTest() {

    @Before
    override fun setUp() {
        super.setUp()
        Given.settings.puzzlesDownloaded()
        Given.user.isDefault()
    }

    @Test
    fun WHEN_navigated_to_analysis_THEN_shows_screen() {
        navigateToAnalysis()

        Then.analysisScreen.isDisplayed()
    }

    @Test
    fun WHEN_navigated_to_analysis_THEN_shows_evaluation_bar() {
        navigateToAnalysis()

        Then.analysisScreen
            .isDisplayed()
            .showsEvaluationBar()
    }

    @Test
    fun WHEN_analysis_runs_THEN_shows_engine_lines() {
        navigateToAnalysis()

        When.compose.waitUntil(ENGINE_WAIT_TIMEOUT_MS) {
            runCatching {
                Then.analysisScreen.showsEngineLines()
                true
            }.getOrDefault(false)
        }

        Then.analysisScreen.showsEngineLines()
    }

    @Test
    fun WHEN_going_back_THEN_returns_to_tools_dashboard() {
        navigateToAnalysis()
        Then.analysisScreen.isDisplayed()

        When.navigation.goBack()

        Then.toolsDashboard.isDisplayed()
    }

    @Test
    fun GIVEN_light_mode_WHEN_navigated_to_analysis_THEN_shows_light_background() {
        Given.settings.lightMode()

        navigateToAnalysis()

        Then.analysisScreen.isDisplayed()
        Then.theme.isLightMode()
    }

    @Test
    fun GIVEN_dark_mode_WHEN_navigated_to_analysis_THEN_shows_dark_background() {
        Given.settings.darkMode()

        navigateToAnalysis()

        Then.analysisScreen.isDisplayed()
        Then.theme.isDarkMode()
    }

    @Test
    fun GIVEN_no_animations_WHEN_navigated_to_analysis_THEN_shows_screen() {
        Given.settings.noAnimations()

        navigateToAnalysis()

        Then.analysisScreen.isDisplayed()
    }

    @Test
    fun GIVEN_borders_enabled_WHEN_navigated_to_analysis_THEN_shows_borders() {
        Given.settings.hasBorders()

        navigateToAnalysis()

        Then.analysisScreen
            .isDisplayed()
            .showsBorders()
    }

    @Test
    fun GIVEN_no_borders_WHEN_navigated_to_analysis_THEN_hides_borders() {
        Given.settings.noBorders()

        navigateToAnalysis()

        Then.analysisScreen
            .isDisplayed()
            .doesNotShowBorders()
    }

    private fun navigateToAnalysis() {
        When.appIsLaunched()
        When.navigation.navigateToTools()
        When.toolsDashboard.openAnalysis()
    }

    private companion object {
        const val ENGINE_WAIT_TIMEOUT_MS = 10_000L
    }
}
