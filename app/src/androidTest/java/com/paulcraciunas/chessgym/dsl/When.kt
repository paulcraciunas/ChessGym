package com.paulcraciunas.chessgym.dsl

import androidx.compose.ui.test.junit4.ComposeTestRule
import com.paulcraciunas.chessgym.dsl.actions.HomeScreenActions

object When {
    lateinit var compose: ComposeTestRule
    lateinit var activityLauncher: () -> Unit
    lateinit var homeScreen: HomeScreenActions

    fun init(rule: ComposeTestRule, launcher: () -> Unit) {
        compose = rule
        activityLauncher = launcher
        homeScreen = HomeScreenActions(rule)
    }

    fun appIsLaunched(): When = apply {
        activityLauncher()
        compose.waitForIdle()
    }
}
