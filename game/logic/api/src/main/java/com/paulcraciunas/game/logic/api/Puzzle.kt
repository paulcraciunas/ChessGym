package com.paulcraciunas.game.logic.api

import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.state.GameInfo

interface Puzzle {
    val rating: Int
    val player: Side
    val state: State
    val info: GameInfo
    val board: IBoard

    fun start()
    fun playNextMove()
    fun play(ply: Ply)
    fun play(from: Locus, to: Locus)
    fun abandon()
    fun hint(): Piece

    enum class State {
        Idle,
        InProgress,
        Failed,
        Success
    }
}
