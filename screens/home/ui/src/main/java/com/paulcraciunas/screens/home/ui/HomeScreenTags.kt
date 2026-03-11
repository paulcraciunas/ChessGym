package com.paulcraciunas.screens.home.ui

/**
 * Test tags for the Home Screen to be used in UI Tests and Compose semantics.
 */
object HomeScreenTags {
    private const val HOME = "home_"
    const val SCREEN = "${HOME}screen"
    const val LOADING = "${HOME}loading"
    const val STATS_CARD = "${HOME}stats_card"
    const val STATS_EXPANDED_CONTENT = "${HOME}stats_expanded_content"
    const val HIGH_SCORES_CARD = "${HOME}high_scores_card"
    const val HIGH_SCORES_EXPANDED_CONTENT = "${HOME}high_scores_expanded_content"

    object Profile {
        private const val PROFILE = "${HOME}profile_"
        const val CARD = "${PROFILE}card"
        const val NAME = "${PROFILE}name"
        const val INITIALS = "${PROFILE}initials"
        const val RATING = "${PROFILE}rating"
        const val ACTIVITIES = "${PROFILE}activities"
    }

    object Timeline {
        private const val TIMELINE = "${HOME}timeline"
        const val ROOT = TIMELINE
        const val EMPTY = "${TIMELINE}_empty"
    }
}
