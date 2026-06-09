package com.paulcraciunas.screens.data.engine

import com.paulcraciunas.screens.data.BOARD_ANIMATION_DURATION_MS
import com.paulcraciunas.screens.data.PIECE_MOVE_ANIMATION_DURATION_MS

data class PlaySessionConfiguration(
    val endMode: EndMode = EndMode.OnFirstFailure,
    val timed: Timed? = null,
    val hints: HintMode? = null,
    val autoNextOverride: Boolean? = null, // When set, it overrides the autoNext application setting
    val moveAnimationMs: Long = PIECE_MOVE_ANIMATION_DURATION_MS.toLong(),
    val boardSwapAnimationMs: Long = BOARD_ANIMATION_DURATION_MS.toLong(),
    val solutionStepDelayMs: Long = DEFAULT_SOLUTION_STEP_DELAY_MS,
) {
    enum class TimedMode { StartImmediately, StartOnClick }
    enum class HintMode { Unlimited, Single }
    enum class EndMode { OnFirstFailure, OnSourceExhausted }

    data class Timed(
        val durationInMs: Long,
        val mode: TimedMode,
    )

    companion object {
        const val DEFAULT_SOLUTION_STEP_DELAY_MS = 750L
    }
}
