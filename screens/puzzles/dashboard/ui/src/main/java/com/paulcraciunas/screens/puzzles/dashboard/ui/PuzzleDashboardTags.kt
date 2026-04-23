package com.paulcraciunas.screens.puzzles.dashboard.ui

/**
 * Test tags for the Puzzle Dashboard Screen.
 */
object PuzzleDashboardTags {
    private const val PREFIX = "puzzle_dashboard_"
    const val SCREEN = "${PREFIX}screen"

    object Cards {
        private const val CARD = "${PREFIX}card_"
        const val RATED_PUZZLE = "${CARD}rated_puzzle"
        const val PUZZLE_RUSH = "${CARD}puzzle_rush"
        const val PUZZLE_STREAK = "${CARD}puzzle_streak"
        const val FAILED_PUZZLES = "${CARD}failed_puzzles"
    }
}

