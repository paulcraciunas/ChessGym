package com.paulcraciunas.screens.puzzles.streak.ui

object PuzzleStreakScreenTags {
    private const val PREFIX = "puzzle_streak_"
    const val SCREEN = "${PREFIX}screen"
    const val STREAK_COUNTER = "${PREFIX}counter"

    object Summary {
        private const val SUMMARY = "${PREFIX}summary_"
        const val DIALOG = "${SUMMARY}dialog"
        const val DISMISS = "${SUMMARY}dismiss"
    }

    object Ended {
        private const val ENDED = "${PREFIX}ended_"
        const val CONTROLS = "${ENDED}controls"
        const val NEW_STREAK = "${ENDED}new_streak"
    }
}
