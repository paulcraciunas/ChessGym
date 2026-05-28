package com.paulcraciunas.game.logic.api

import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.state.GameInfo
import com.paulcraciunas.game.logic.api.state.MetaData

interface Game {
    val metadata: MetaData
    val rating: Int?

    val info: GameInfo
    val board: IBoard
    val state: GameState
    val history: List<Ply>
    val historySize: Int
    val currentMoveIndex: Int

    fun start()
    fun plies(): List<Ply>
    fun plies(from: Locus): List<Ply>
    fun ply(from: Locus, to: Locus): Ply?

    fun play(ply: Ply)
    fun play(from: Locus, to: Locus)
    fun play(ply: String)
    fun resign()
    fun draw()

    fun canUndo(): Boolean
    fun undoLast()
    fun undoAll()
    fun canReplay(): Boolean
    fun replayNext()
    fun replayAll()

    sealed class GameState {
        data object Ready : GameState()
        data object InProgress : GameState()
        data class Finished(val result: Result) : GameState()
    }
}
