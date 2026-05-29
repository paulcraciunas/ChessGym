package com.paulcraciunas.screens.data

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Result
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.state.GameInfo

class GamePlayableBoard(
    val game: Game,
    override val player: Side,
) : PlayableBoard {
    override val board: IBoard get() = game.board
    override val info: GameInfo get() = game.info
    override val activeSide: Side get() = game.info.turn
    override val rating: Int? get() = game.rating
    override val id: Int? get() = null

    override fun initialize() {
        if (game.state == Game.GameState.Ready) {
            game.start()
        }
    }

    override fun plies(from: Locus): List<Ply> = game.plies(from)
    override fun ply(from: Locus, to: Locus): Ply? = game.ply(from, to)
    override fun play(from: Locus, to: Locus): Unit = game.play(from, to)
    override fun play(ply: Ply): Unit = game.play(ply)
    override fun resign(): Unit = game.resign()
    override fun isOver(): Boolean = game.state is Game.GameState.Finished
    override fun outcome(): PlayableData.Outcome? {
        val result = (game.state as? Game.GameState.Finished)?.result ?: return null
        val lastPly = game.info.lastPly ?: return PlayableData.Outcome.Lost // Finish what you started, dear player
        return when {
            result == Result.Resigned -> PlayableData.Outcome.Lost
            result.isDraw() -> PlayableData.Outcome.Drew
            lastPly.turn == player -> PlayableData.Outcome.Won
            else -> PlayableData.Outcome.Lost
        }
    }
}
