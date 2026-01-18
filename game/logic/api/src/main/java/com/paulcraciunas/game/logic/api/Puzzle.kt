package com.paulcraciunas.game.logic.api

import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.state.GameInfo

interface Puzzle {
    val id: Int?
    val rating: Int
    val player: Side
    val state: State
    val info: GameInfo
    val board: IBoard
    val expectedMoves: List<String>

    fun start()
    fun plies(from: Locus): List<Ply>
    fun ply(from: Locus, to: Locus): Ply?
    fun play(ply: Ply)
    fun play(from: Locus, to: Locus)
    fun playNextMove()
    fun resign()
    fun nextExpectedMove(): Pair<Locus, Locus>?

    enum class State {
        Idle,
        InProgress,
        Failed,
        Success;

        fun isOver(): Boolean = this == Failed || this == Success
    }
}
