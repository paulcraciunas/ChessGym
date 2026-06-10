package com.paulcraciunas.screens.data.engine

import com.paulcraciunas.screens.data.BoardState

data class SingleSessionState(
    val status: Status = Status.Loading,
    val boardState: BoardState = BoardState.empty,
    val navigation: Navigation? = null,
    val hintAvailable: Boolean = false,
    val isAnimating: Boolean = false,
    val errorMessage: String? = null,
    val abandonRequested: Boolean = false,
) {
    enum class Status {
        Loading,
        Ready,
        Playing,
        GameOver,
        Failed,
    }

    data class Navigation(
        val canGoBack: Boolean,
        val canGoForward: Boolean,
        val completedMoves: Int,
        val algebraicHistory: String,
    )
}
