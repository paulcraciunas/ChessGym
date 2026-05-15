package com.paulcraciunas.chessgym.dsl.assertions

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import com.paulcraciunas.screens.tools.clock.ui.ClockScreenTags

class ClockScreenAssertions(private val rule: ComposeTestRule) {

    fun isDisplayed(): ClockScreenAssertions = apply {
        rule.onNodeWithTag(ClockScreenTags.SCREEN).assertIsDisplayed()
    }

    fun isInSetupPhase(): ClockScreenAssertions = apply {
        rule.onNodeWithTag(ClockScreenTags.Controls.TIME_SELECTOR).assertIsDisplayed()
        rule.onNodeWithTag(ClockScreenTags.WHITE_BUTTON).assertIsDisplayed()
        rule.onNodeWithTag(ClockScreenTags.BLACK_BUTTON).assertIsDisplayed()
    }

    fun isInPlayingPhase(): ClockScreenAssertions = apply {
        rule.onNodeWithTag(ClockScreenTags.STOP_BUTTON).assertIsDisplayed()
    }

    fun isInFinishedPhase(): ClockScreenAssertions = apply {
        rule.onNodeWithTag(ClockScreenTags.NEW_GAME_BUTTON).assertIsDisplayed()
    }

    fun whiteButtonIsDisabled(): ClockScreenAssertions = apply {
        rule.onNodeWithTag(ClockScreenTags.WHITE_BUTTON).assertIsNotEnabled()
    }

    fun blackButtonIsEnabled(): ClockScreenAssertions = apply {
        rule.onNodeWithTag(ClockScreenTags.BLACK_BUTTON).assertIsEnabled()
    }

    fun showsStopButton(): ClockScreenAssertions = apply {
        rule.onNodeWithTag(ClockScreenTags.STOP_BUTTON).assertIsDisplayed()
    }

    fun showsNewGameButton(): ClockScreenAssertions = apply {
        rule.onNodeWithTag(ClockScreenTags.NEW_GAME_BUTTON).assertIsDisplayed()
    }
}
