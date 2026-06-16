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
    enum class Status {
        Loading, // Initial starting state
        Ready, // State used before first session begins playing
        Playing, // State used while playing
        Paused, // State used after finishing one session, before loading the next
        Ended, // All sessions have been processed
        Failed // Unrecoverable exception encountered
    }

    data class Navigation(
        val canGoBack: Boolean,
        val canGoForward: Boolean,
        val completedMoves: Int,
        val algebraicHistory: String,
    )
}
