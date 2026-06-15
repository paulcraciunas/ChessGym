package com.paulcraciunas.screens.data

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.state.GameInfo

class GamePlayableBoard(val game: Game, override val player: Side) : PlayableBoard {
    override val board: IBoard get() = game.board
    override val info: GameInfo get() = game.info
    override val rating: Int? get() = game.rating
    override val id: Int? get() = null
    override val lastMovePly: Ply? get() = game.history.lastOrNull()

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
    override fun isPlayerTurn(): Boolean = info.turn == player
    override fun isOver(): Boolean = game.state is Game.GameState.Finished
    override fun outcome(): Outcome? {
        val state = game.state
        if (state !is Game.GameState.Finished) return null
        return when {
            state.result.isDraw() -> Outcome.Drew
            info.turn == player -> Outcome.Lost
            else -> Outcome.Won
        }
    }
}
