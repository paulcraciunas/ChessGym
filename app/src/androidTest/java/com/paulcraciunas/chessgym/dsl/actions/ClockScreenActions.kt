package com.paulcraciunas.chessgym.dsl.actions

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.tools.clock.ui.ClockScreenTags

class ClockScreenActions(private val rule: ComposeTestRule) {

    fun tapWhite(): ClockScreenActions = apply {
        rule.onNodeWithTag(ClockScreenTags.WHITE_BUTTON).performClick()
        rule.waitForIdle()
    }

    fun tapBlack(): ClockScreenActions = apply {
        rule.onNodeWithTag(ClockScreenTags.BLACK_BUTTON).performClick()
        rule.waitForIdle()
    }

    fun stop(): ClockScreenActions = apply {
        rule.onNodeWithTag(ClockScreenTags.STOP_BUTTON).performClick()
        rule.waitForIdle()
    }

    fun newGame(): ClockScreenActions = apply {
        rule.onNodeWithTag(ClockScreenTags.NEW_GAME_BUTTON).performClick()
        rule.waitForIdle()
    }

    fun selectTime(minutes: Int): ClockScreenActions = apply {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val label = context.getString(R.string.clock_minutes_format, minutes)
        rule.onNodeWithText(label).performClick()
        rule.waitForIdle()
    }

    fun selectIncrement(seconds: Int): ClockScreenActions = apply {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val label = context.getString(R.string.clock_seconds_format, seconds)
        rule.onNodeWithText(label).performClick()
        rule.waitForIdle()
    }
}
