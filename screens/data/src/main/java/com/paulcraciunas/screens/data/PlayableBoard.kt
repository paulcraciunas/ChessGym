package com.paulcraciunas.screens.data

import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.state.GameInfo

interface PlayableBoard {
    val board: IBoard
    val info: GameInfo
    val player: Side
    val playerSide: Side
    val rating: Int?
    val id: Int?
    val lastMovePly: Ply?
    fun initialize()
    fun plies(from: Locus): List<Ply>
    fun ply(from: Locus, to: Locus): Ply?
    fun play(from: Locus, to: Locus)
    fun play(ply: Ply)
    fun resign()
    fun isPlayerTurn(): Boolean
    fun isOver(): Boolean
    fun outcome(): Outcome?
}
