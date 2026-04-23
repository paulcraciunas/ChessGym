package com.paulcraciunas.screens.puzzles.rated.ui

/**
 * Test tags for the Rated Puzzle Screen to be used in UI Tests and Compose semantics.
 */
object RatedPuzzleScreenTags {
    private const val PREFIX = "rated_puzzle_"
    const val SCREEN = "${PREFIX}screen"
    const val LOADING = "${PREFIX}loading"
    const val FAILED = "${PREFIX}failed"
    const val TITLE = "${PREFIX}title"

    object Finished {
        private const val FINISHED = "${PREFIX}finished_"
        const val CONTROLS = "${FINISHED}controls"
        const val RESULT_TEXT = "${FINISHED}result"
        const val RATING_CHANGE = "${FINISHED}rating_change"
        const val PLAY_NEXT = "${FINISHED}play_next"
    }
}
