package com.paulcraciunas.screens.boardvis.squares.vm

import javax.inject.Inject

interface GameDuration {
    val seconds: Int
}

class DefaultGameDuration @Inject constructor() : GameDuration {
    override val seconds: Int = FindTheSquareUiState.DEFAULT_DURATION_SECONDS
}
