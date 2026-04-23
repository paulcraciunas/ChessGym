package com.paulcraciunas.chessgym.dsl

import androidx.compose.ui.test.junit4.ComposeTestRule
import com.paulcraciunas.chessgym.dsl.actions.HomeScreenActions
import com.paulcraciunas.chessgym.dsl.actions.NavigationActions
import com.paulcraciunas.chessgym.dsl.actions.RatedPuzzleScreenActions

object When {
    lateinit var compose: ComposeTestRule
    lateinit var activityLauncher: () -> Unit
    lateinit var navigation: NavigationActions
    lateinit var homeScreen: HomeScreenActions
    lateinit var ratedPuzzle: RatedPuzzleScreenActions

    fun init(rule: ComposeTestRule, launcher: () -> Unit) {
        compose = rule
        activityLauncher = launcher
        navigation = NavigationActions(rule)
        homeScreen = HomeScreenActions(rule)
        ratedPuzzle = RatedPuzzleScreenActions(rule)
    }

    fun appIsLaunched(): When = apply {
        activityLauncher()
        compose.waitForIdle()
    }
}
