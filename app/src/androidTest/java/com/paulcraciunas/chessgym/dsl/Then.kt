package com.paulcraciunas.chessgym.dsl

import androidx.compose.ui.test.junit4.ComposeTestRule
import com.paulcraciunas.chessgym.dsl.assertions.HomeScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.PuzzleDashboardAssertions
import com.paulcraciunas.chessgym.dsl.assertions.RatedPuzzleScreenAssertions

object Then {
    lateinit var compose: ComposeTestRule
    lateinit var homeScreen: HomeScreenAssertions
    lateinit var puzzleDashboard: PuzzleDashboardAssertions
    lateinit var ratedPuzzle: RatedPuzzleScreenAssertions

    fun init(rule: ComposeTestRule) {
        compose = rule
        homeScreen = HomeScreenAssertions(rule)
        puzzleDashboard = PuzzleDashboardAssertions(rule)
        ratedPuzzle = RatedPuzzleScreenAssertions(rule)
    }
}
