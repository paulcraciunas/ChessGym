package com.paulcraciunas.screens.tools.clock.ui

object ClockScreenTags {
    private const val PREFIX = "clock_"
    const val SCREEN = "${PREFIX}screen"
    const val WHITE_BUTTON = "${PREFIX}white_button"
    const val BLACK_BUTTON = "${PREFIX}black_button"
    const val STOP_BUTTON = "${PREFIX}stop_button"
    const val NEW_GAME_BUTTON = "${PREFIX}new_game_button"

    object Controls {
        private const val CTRL = "${PREFIX}controls_"
        const val TIME_SELECTOR = "${CTRL}time_selector"
        const val INCREMENT_SELECTOR = "${CTRL}increment_selector"
    }
}
