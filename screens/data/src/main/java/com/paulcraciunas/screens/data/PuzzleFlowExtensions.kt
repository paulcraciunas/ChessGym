package com.paulcraciunas.screens.data

import com.paulcraciunas.domain.api.puzzles.NoPuzzleException
import com.paulcraciunas.domain.api.puzzles.PuzzleGenerationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import timber.log.Timber

/**
 * Catches [PuzzleGenerationException] caused by [NoPuzzleException] and completes gracefully.
 * All other exceptions are rethrown so they surface as [com.paulcraciunas.screens.data.engine.PlaySessionState.Status.Failed].
 *
 * Use this on any puzzle session source Flow to handle the "no more puzzles" scenario
 * without crashing the session.
 */
fun <T> Flow<T>.catchPuzzleExhausted(screenName: String): Flow<T> = catch { up ->
    if (up is PuzzleGenerationException) {
        Timber.w(up, "Failed to retrieve puzzle from DB")
        if (up.cause !is NoPuzzleException) {
            throw up
        }
    } else {
        Timber.e(up, "Unexpected exception while playing $screenName")
        throw up
    }
}
