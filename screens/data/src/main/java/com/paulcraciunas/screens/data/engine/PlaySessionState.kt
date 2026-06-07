package com.paulcraciunas.screens.data.engine

import com.paulcraciunas.screens.data.BoardState
import com.paulcraciunas.screens.data.SessionResult

data class PlaySessionState(
    val status: Status = Status.Loading,
    val results: List<SessionResult> = emptyList(),
    val boardState: BoardState = BoardState.empty,
    val remainingTimeMs: Long? = null,
    val hintAvailable: Boolean = false,
    val navigation: Navigation? = null,
    val isAnimating: Boolean = false,
    val errorMessage: String? = null,
    val abandonRequested: Boolean = false,
    val showSummary: Boolean = false,
) {
    enum class Status { Loading, Ready, Playing, Paused, Ended, Failed }

    data class Navigation(
        val canGoBack: Boolean,
        val canGoForward: Boolean,
        val completedMoves: Int,
        val algebraicHistory: String,
    )
}
