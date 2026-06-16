package com.paulcraciunas.screens.boardvis.pieces.vm

import com.paulcraciunas.domain.api.boardvis.GetKnightPathBufferedSeries
import com.paulcraciunas.domain.api.boardvis.KnightPathExercise
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.data.AbstractBoardSession
import com.paulcraciunas.screens.data.BoardState
import com.paulcraciunas.screens.data.BoardViewData
import com.paulcraciunas.screens.data.CapturedPieces
import com.paulcraciunas.screens.data.NavigationStrategy
import com.paulcraciunas.screens.data.NoOpNavigation
import com.paulcraciunas.screens.data.NoOpOpponent
import com.paulcraciunas.screens.data.NoOpSolution
import com.paulcraciunas.screens.data.OpponentStrategy
import com.paulcraciunas.screens.data.Outcome
import com.paulcraciunas.screens.data.SessionResult
import com.paulcraciunas.screens.data.SolutionStrategy
import com.paulcraciunas.screens.data.engine.PlaySession
import com.paulcraciunas.screens.data.engine.PlaySessionConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.ArrayDeque
import java.util.Queue

internal class KnightPathBoardSession : AbstractBoardSession() {
    override val navigation: NavigationStrategy = NoOpNavigation
    override val solution: SolutionStrategy = NoOpSolution
    override val opponent: OpponentStrategy = NoOpOpponent

    private lateinit var state: BoardState
    private lateinit var board: IBoard
    private lateinit var path: Queue<Locus>
    private val completedMoves: Queue<Locus> = ArrayDeque(GetKnightPathBufferedSeries.MAX_MOVES)

    fun load(exercise: KnightPathExercise, id: Int) = apply {
        completedMoves.clear()
        board = exercise.board
        state = BoardState(
            rating = null,
            player = Side.WHITE,
            id = id,
            boardData = BoardViewData.from(board = board).select(exercise.from, emptyList()), // always keep the knight selected
            movePlayed = false,
            promotion = null,
            captured = CapturedPieces(byPlayer = "", byOpponent = ""),
            outcome = null,
        )
        path = ArrayDeque(exercise.path)
        path.poll() // remove the first element, to keep only expected moves
    }

    override fun current(): BoardState = state
    override fun clear(): BoardState {
        state = state.copy(boardData = state.boardData.clearSelection())
        return state
    }

    override fun refresh(withAnimation: Boolean): BoardState = state
    override fun onClick(selection: Locus): BoardState {
        if (state.outcome != null) return state
        val expectedMove = path.peek()

        return when {
            selection == expectedMove -> moveTo(expectedMove)
            board.at(selection) != null -> state // Can't move there
            else -> applyMove(selection, Outcome.Lost)
        }
    }

    override fun autoPromote(enabled: Boolean) {}
    override fun promote(to: Piece, at: Locus): BoardState = throw IllegalArgumentException("Can't promote in Knight Path games")
    override fun promoteIfPending(to: Piece): BoardState = throw IllegalArgumentException("Can't promote in Knight Path games")
    override fun resign(): BoardState {
        state = state.copy(outcome = Outcome.Lost)
        return state
    }

    override fun result(): SessionResult {
        assert(state.outcome != null)
        return SessionResult(id = state.id, rating = null, outcome = state.outcome!!)
    }

    override fun canPlayOpponentMove(): Boolean = false
    override suspend fun playOpponentMove(): Boolean = false
    override suspend fun close() {}
    override fun hint(): BoardState = state

    private fun moveTo(to: Locus): BoardState {
        completedMoves.add(path.poll())
        return applyMove(to, if (path.isEmpty()) Outcome.Won else null)
    }

    private fun applyMove(to: Locus, outcome: Outcome?): BoardState {
        val currentSelection = state.boardData.selection!!
        board.move(from = currentSelection, to = to, turn = Side.WHITE)
        state = state.copy(
            boardData = BoardViewData.from(
                board = board,
                lastMove = currentSelection to to,
                withAnimation = true,
            ).select(at = to, moves = emptyList()), // always keep the knight selected
            movePlayed = true,
            outcome = outcome,
        )
        return state
    }
}

internal const val DURATION_MS = 30_000L
internal const val DANGER_THRESHOLD = 5_000L

internal fun knightPathConfiguration(): PlaySessionConfiguration = PlaySessionConfiguration(
    endMode = PlaySessionConfiguration.EndMode.OnFirstFailure,
    timed = PlaySessionConfiguration.Timed(
        durationInMs = DURATION_MS,
        mode = PlaySessionConfiguration.TimedMode.StartImmediately,
    ),
    hints = null,
    autoNextOverride = true,
)

internal class KnightPathSessions(
    private val exercises: GetKnightPathBufferedSeries,
) : PlaySession.SessionsSource {
    private var currentId = 0

    override fun invoke(): Flow<AbstractBoardSession> = exercises()
        .map { KnightPathBoardSession().load(exercise = it, id = ++currentId) }
}
