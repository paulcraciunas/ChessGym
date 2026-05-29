package com.paulcraciunas.screens.common.model

import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.state.GameInfo

interface PlayableBoard {
    val board: IBoard
    val info: GameInfo
    val player: Side
    val activeSide: Side
    val rating: Int?
    val id: Int?
    fun initialize()
    fun plies(from: Locus): List<Ply>
    fun ply(from: Locus, to: Locus): Ply?
    fun play(from: Locus, to: Locus)
    fun play(ply: Ply)
    fun resign()
    fun isOver(): Boolean
    fun outcome(): PlayableData.Outcome?
}
