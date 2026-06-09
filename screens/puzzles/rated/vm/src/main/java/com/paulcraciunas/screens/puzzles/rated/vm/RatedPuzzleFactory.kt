package com.paulcraciunas.screens.puzzles.rated.vm

import com.paulcraciunas.domain.api.general.EloResult
import com.paulcraciunas.domain.api.general.Timer
import com.paulcraciunas.domain.api.puzzles.GetRatedPuzzle
import com.paulcraciunas.domain.api.puzzles.OnPuzzleComplete
import com.paulcraciunas.domain.api.puzzles.PuzzleCompletionResult
import com.paulcraciunas.screens.data.BoardSession
import com.paulcraciunas.screens.data.NoOpNavigation
import com.paulcraciunas.screens.data.PuzzlePlayableBoard
import com.paulcraciunas.screens.data.PuzzleSolution
import com.paulcraciunas.screens.data.ScriptedOpponent
import com.paulcraciunas.screens.data.catchPuzzleExhausted
import com.paulcraciunas.screens.data.engine.PlaySession
import com.paulcraciunas.screens.data.engine.PlaySessionConfiguration
import com.paulcraciunas.screens.data.engine.PlaySessionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update

internal fun ratedConfiguration(): PlaySessionConfiguration = PlaySessionConfiguration(
    endMode = PlaySessionConfiguration.EndMode.OnSourceExhausted,
    timed = null,
    hints = PlaySessionConfiguration.HintMode.Single,
    autoNextOverride = false,
)

internal class RatedPuzzleSessions(
    private val getRatedPuzzle: GetRatedPuzzle,
    private val ratingChange: MutableStateFlow<EloResult>,
) : PlaySession.SessionsSource {
    override fun invoke(): Flow<BoardSession> = flow {
        while (true) {
            val puzzleData = getRatedPuzzle()
            ratingChange.update { puzzleData.ratingChange }
            emit(
                BoardSession(
                    navigation = NoOpNavigation,
                    solution = PuzzleSolution(puzzleData.puzzle),
                    opponent = ScriptedOpponent(puzzleData.puzzle),
                ).load(PuzzlePlayableBoard(puzzleData.puzzle))
            )
        }
    }.catchPuzzleExhausted("rated puzzle")
}

internal class RatedPuzzleOnComplete(
    private val onPuzzleComplete: OnPuzzleComplete,
    private val ratingChange: StateFlow<EloResult>,
    private val timer: Timer,
) : PlaySession.OnComplete {
    override suspend fun invoke(onCompleteState: PlaySessionState) {
        val boardState = onCompleteState.boardState
        val elo = ratingChange.value
        onPuzzleComplete(
            PuzzleCompletionResult(
                puzzleId = boardState.id,
                puzzleRating = boardState.rating!!,
                wasSuccessful = boardState.won,
                ratingChange = elo.getNormalized(success = boardState.won),
                timeSpentMillis = timer.elapsed(),
            )
        )
    }
}
