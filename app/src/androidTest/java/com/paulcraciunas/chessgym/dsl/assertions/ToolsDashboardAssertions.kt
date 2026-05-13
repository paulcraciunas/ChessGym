package com.paulcraciunas.chessgym.dsl.assertions

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import com.paulcraciunas.screens.tools.dashboard.ui.ToolsDashboardTags

class ToolsDashboardAssertions(private val rule: ComposeTestRule) {

    fun isDisplayed(): ToolsDashboardAssertions = apply {
        rule.onNodeWithTag(ToolsDashboardTags.SCREEN).assertIsDisplayed()
    }

    fun hasClockCard(): ToolsDashboardAssertions = apply {
        rule.onNodeWithTag(ToolsDashboardTags.Cards.CLOCK)
            .performScrollTo()
            .assertIsDisplayed()
    }

    fun hasAnalysisCard(): ToolsDashboardAssertions = apply {
        rule.onNodeWithTag(ToolsDashboardTags.Cards.ANALYSIS)
            .performScrollTo()
            .assertIsDisplayed()
    }

    fun hasImportGameCard(): ToolsDashboardAssertions = apply {
        rule.onNodeWithTag(ToolsDashboardTags.Cards.IMPORT_GAME)
            .performScrollTo()
            .assertIsDisplayed()
    }

    fun hasAllCards(): ToolsDashboardAssertions = apply {
        hasClockCard()
        hasAnalysisCard()
        hasImportGameCard()
    }
}
