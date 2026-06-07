package com.paulcraciunas.screens.data.engine

data class PlaySessionConfiguration(
    val endMode: EndMode = EndMode.OnFirstFailure,
    val timed: Timed? = null,
    val hints: HintMode? = null,
    val autoPromote: Boolean = true,
    val autoNext: Boolean = true,
    val waitForAnimations: Boolean = true,
) {
    enum class TimedMode { StartImmediately, StartOnClick }
    enum class HintMode { Unlimited, Single }
    enum class EndMode { OnFirstFailure, OnSourceExhausted }

    data class Timed(
        val durationInMs: Long,
        val mode: TimedMode,
    )
}
