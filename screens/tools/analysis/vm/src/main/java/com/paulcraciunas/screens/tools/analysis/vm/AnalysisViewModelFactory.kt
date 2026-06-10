package com.paulcraciunas.screens.tools.analysis.vm

import com.paulcraciunas.domain.api.puzzles.GetPuzzleFen
import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.logic.builders.Builders
import com.paulcraciunas.screens.data.BoardSession
import com.paulcraciunas.screens.data.GameNavigation
import com.paulcraciunas.screens.data.GamePlayableBoard
import com.paulcraciunas.serializer.api.Serializer
import timber.log.Timber

internal class AnalysisSession(
    private val fenSerializer: Serializer,
    private val getPuzzleFen: GetPuzzleFen,
) {
    private var puzzleId: Int? = null
    private var game = Builders.gameFactory().builder().withDefaultBoard().buildGame()
    val turn: Side
        get() = game.info.turn
    val moveIndex: Int
        get() = game.currentMoveIndex
    val fen: String
        get() = fenSerializer.of(game)

    fun withPuzzle(id: Int) {
        puzzleId = id
    }

    suspend fun createSession(): BoardSession {
        puzzleId?.let { id ->
            try {
                getPuzzleFen(id)?.let { puzzleData ->
                    game = fenSerializer.from(puzzleData.fen)
                    if (game.state == Game.GameState.Ready) game.start()
                    game.play(ply = puzzleData.firstMove)
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to load FEN position for puzzle $id")
            }
        }
        return BoardSession(navigation = GameNavigation(game))
            .load(GamePlayableBoard(game = game, player = turn))
    }
}
