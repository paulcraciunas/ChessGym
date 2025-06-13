package com.paulcraciunas.game.logic.api

import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.state.GameInfo
import com.paulcraciunas.game.logic.api.state.MetaData

interface Game {
    val metadata: MetaData
    val rating: Int?

    val info: GameInfo
    val board: IBoard
    val state: GameState
    val history: List<Ply>

    fun start()

    fun play(ply: Ply)
    fun resign()
    fun draw()

    sealed class GameState {
        data object Ready : GameState()
        data object InProgress : GameState()
        data class Finished(val result: Result) : GameState()
    }
}
