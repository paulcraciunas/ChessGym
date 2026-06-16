package com.paulcraciunas.screens.puzzles.streak.vm

import com.paulcraciunas.domain.api.puzzles.GetStreakPuzzle
import com.paulcraciunas.screens.data.BoardSession
import com.paulcraciunas.screens.data.NoOpNavigation
import com.paulcraciunas.screens.data.PuzzlePlayableBoard
import com.paulcraciunas.screens.data.PuzzleSolution
import com.paulcraciunas.screens.data.ScriptedOpponent
import com.paulcraciunas.screens.data.catchPuzzleExhausted
import com.paulcraciunas.screens.data.engine.PlaySession
import com.paulcraciunas.screens.data.engine.PlaySessionConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal fun streakConfiguration(): PlaySessionConfiguration = PlaySessionConfiguration(
    endMode = PlaySessionConfiguration.EndMode.OnFirstFailure,
    timed = null,
    hints = PlaySessionConfiguration.HintMode.Single,
)

internal class PuzzleStreakSessions(private val getStreakPuzzle: GetStreakPuzzle) : PlaySession.SessionsSource {
    var streakCount: Int = 0
        private set

    override fun invoke(): Flow<BoardSession> = flow {
        while (true) {
            val streakSession = getStreakPuzzle()
            streakCount = streakSession.currentStreakCount
            emit(
                BoardSession(
                    navigation = NoOpNavigation,
                    solution = PuzzleSolution(streakSession.puzzle),
                    opponent = ScriptedOpponent(streakSession.puzzle),
                ).load(PuzzlePlayableBoard(streakSession.puzzle))
            )
        }
    }.catchPuzzleExhausted("puzzle streak")
}
