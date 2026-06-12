package com.paulcraciunas.chessgym.dsl.actions

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.core.app.ActivityScenario
import com.paulcraciunas.chessgym.MainActivity

class AppActions(private val rule: ComposeTestRule) {
    private lateinit var scenario: ActivityScenario<MainActivity>

    fun launch() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        rule.waitForIdle()
    }

    fun close() {
        scenario.close()
    }
}
