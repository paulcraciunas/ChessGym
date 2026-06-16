package com.paulcraciunas.screens.puzzles.rated.vm

import com.paulcraciunas.screens.data.engine.SingleSessionConfiguration

internal fun ratedSessionConfiguration(): SingleSessionConfiguration = SingleSessionConfiguration(
    gameOverBehavior = SingleSessionConfiguration.GameOverBehavior.Terminate,
    hints = SingleSessionConfiguration.HintMode.Single,
)
