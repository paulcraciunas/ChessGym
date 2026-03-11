package com.paulcraciunas.chessgym.dsl

import androidx.compose.ui.test.junit4.ComposeTestRule
import com.paulcraciunas.chessgym.dsl.assertions.HomeScreenAssertions

object Then {
    lateinit var compose: ComposeTestRule
    lateinit var homeScreen: HomeScreenAssertions

    fun init(rule: ComposeTestRule) {
        compose = rule
        homeScreen = HomeScreenAssertions(rule)
    }
}
