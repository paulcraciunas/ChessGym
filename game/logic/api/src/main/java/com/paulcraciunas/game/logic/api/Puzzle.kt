package com.paulcraciunas.game.logic.api

import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.state.GameInfo

interface Puzzle {
    val rating: Int
    val player: Side
    val state: State
    val info: GameInfo
    val board: IBoard

    fun start()
    fun plies(from: Locus): List<Ply>
    // TODO Paul: probably should get rid of this
    fun ply(from: Locus, to: Locus): Ply?
    fun playNextMove()
    fun play(ply: Ply)
    fun play(from: Locus, to: Locus)
    fun resign()
    fun hint(): Locus

    enum class State {
        Idle,
        InProgress,
        Failed,
        Success;

        fun isOver(): Boolean = this == Failed || this == Success
    }
}
