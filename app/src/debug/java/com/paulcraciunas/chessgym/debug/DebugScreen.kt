package com.paulcraciunas.chessgym.debug

import kotlinx.serialization.Serializable

@Serializable
sealed class DebugScreen {
    @Serializable
    data object LoadPuzzle : DebugScreen()

    @Serializable
    data object ForceAchievement : DebugScreen()
}
