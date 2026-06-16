package com.paulcraciunas.screens.data.engine

import com.paulcraciunas.screens.data.BOARD_ANIMATION_DURATION_MS
import com.paulcraciunas.screens.data.PIECE_MOVE_ANIMATION_DURATION_MS

data class SingleSessionConfiguration(
    val gameOverBehavior: GameOverBehavior = GameOverBehavior.Terminate,
    val hints: HintMode? = null,
    val solutionStepDelayMs: Long = DEFAULT_SOLUTION_STEP_DELAY_MS,
    val moveAnimationMs: Long = PIECE_MOVE_ANIMATION_DURATION_MS.toLong(),
    val boardSwapAnimationMs: Long = BOARD_ANIMATION_DURATION_MS.toLong(),
) {
    enum class GameOverBehavior { Terminate, AllowNavigation }
    enum class HintMode { Unlimited, Single }

    companion object {
        const val DEFAULT_SOLUTION_STEP_DELAY_MS = 750L
    }
}
