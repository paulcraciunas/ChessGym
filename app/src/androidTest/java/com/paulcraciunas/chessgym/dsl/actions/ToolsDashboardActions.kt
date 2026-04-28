package com.paulcraciunas.chessgym.dsl.actions

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.paulcraciunas.screens.tools.dashboard.ui.ToolsDashboardTags

class ToolsDashboardActions(private val rule: ComposeTestRule) {

    fun openClock(): ToolsDashboardActions = clickCard(ToolsDashboardTags.Cards.CLOCK)

    fun openAnalysis(): ToolsDashboardActions = clickCard(ToolsDashboardTags.Cards.ANALYSIS)

    fun openImportGame(): ToolsDashboardActions = clickCard(ToolsDashboardTags.Cards.IMPORT_GAME)

    private fun clickCard(tag: String): ToolsDashboardActions = apply {
        rule.onNodeWithTag(tag).performScrollTo().performClick()
        rule.waitForIdle()
    }
}
