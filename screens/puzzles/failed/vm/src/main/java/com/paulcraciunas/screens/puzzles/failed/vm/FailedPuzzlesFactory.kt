package com.paulcraciunas.screens.puzzles.failed.vm

import com.paulcraciunas.domain.api.puzzles.GetFailedPuzzles
import com.paulcraciunas.screens.data.BoardSession
import com.paulcraciunas.screens.data.NoOpNavigation
import com.paulcraciunas.screens.data.PuzzlePlayableBoard
import com.paulcraciunas.screens.data.PuzzleSolution
import com.paulcraciunas.screens.data.ScriptedOpponent
import com.paulcraciunas.screens.data.catchPuzzleExhausted
import com.paulcraciunas.screens.data.engine.PlaySession
import com.paulcraciunas.screens.data.engine.PlaySessionConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal fun failedPuzzlesConfiguration(): PlaySessionConfiguration = PlaySessionConfiguration(
    endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted,
    timed = null,
    hints = PlaySessionConfiguration.HintMode.Unlimited,
    autoNextOverride = true,
)

internal class FailedPuzzlesSessions(
    private val getFailedPuzzles: GetFailedPuzzles,
) : PlaySession.SessionsSource {
    override fun invoke(): Flow<BoardSession> = getFailedPuzzles.execute()
        .map { puzzle ->
            BoardSession(
                navigation = NoOpNavigation,
                solution = PuzzleSolution(puzzle),
                opponent = ScriptedOpponent(puzzle),
            ).load(PuzzlePlayableBoard(puzzle))
        }
        .catchPuzzleExhausted("failed puzzles")
}
