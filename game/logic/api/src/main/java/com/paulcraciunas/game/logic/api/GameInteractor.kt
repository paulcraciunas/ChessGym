package com.paulcraciunas.game.logic.api

import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

interface GameInteractor {
    val rating: Int?
    val player: Side
    val captured: Map<Side, List<Piece>>
    val lastPly: Ply?

    fun load(game: Game, player: Side)

    fun canPlay(from: Locus, to: Locus): Boolean
    fun moves(from: Locus): List<Locus>
    fun play(from: Locus, to: Locus)
    fun canPromote(from: Locus, to: Locus): Boolean
    fun promote(from: Locus, to: Locus, result: Piece)
    fun resign()

    fun isOver(): Boolean
}
