package com.paulcraciunas.chessgym.dsl

import androidx.compose.ui.test.junit4.ComposeTestRule
import com.paulcraciunas.chessgym.dsl.assertions.BlindModeScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.BoardVisDashboardAssertions
import com.paulcraciunas.chessgym.dsl.assertions.FailedPuzzlesScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.HomeScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.NavigationAssertions
import com.paulcraciunas.chessgym.dsl.assertions.PuzzleDashboardAssertions
import com.paulcraciunas.chessgym.dsl.assertions.PuzzleRushScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.PuzzleStreakScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.RatedPuzzleScreenAssertions

object Then {
    lateinit var compose: ComposeTestRule
    lateinit var navigation: NavigationAssertions
    lateinit var homeScreen: HomeScreenAssertions
    lateinit var puzzleDashboard: PuzzleDashboardAssertions
    lateinit var ratedPuzzle: RatedPuzzleScreenAssertions
    lateinit var puzzleRush: PuzzleRushScreenAssertions
    lateinit var puzzleStreak: PuzzleStreakScreenAssertions
    lateinit var failedPuzzles: FailedPuzzlesScreenAssertions
    lateinit var boardVisDashboard: BoardVisDashboardAssertions
    lateinit var blindMode: BlindModeScreenAssertions

    fun init(rule: ComposeTestRule) {
        compose = rule
        navigation = NavigationAssertions(rule)
        homeScreen = HomeScreenAssertions(rule)
        puzzleDashboard = PuzzleDashboardAssertions(rule)
        ratedPuzzle = RatedPuzzleScreenAssertions(rule)
        puzzleRush = PuzzleRushScreenAssertions(rule)
        puzzleStreak = PuzzleStreakScreenAssertions(rule)
        failedPuzzles = FailedPuzzlesScreenAssertions(rule)
        boardVisDashboard = BoardVisDashboardAssertions(rule)
        blindMode = BlindModeScreenAssertions(rule)
    }
}
