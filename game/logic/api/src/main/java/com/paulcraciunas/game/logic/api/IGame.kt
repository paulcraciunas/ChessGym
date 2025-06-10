package com.paulcraciunas.game.logic.api

import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.state.IGameState
import com.paulcraciunas.game.logic.api.state.MetaData

interface IGame {
    val state: IGameState

    fun turn(): Side
    fun board(): IBoard
    fun state(): IGameState
    fun metaData(): MetaData

    fun allPlies(): List<IPly>
    fun playablePlies(from: Locus): Collection<IPly>
    fun allPlayablePlies(): Collection<IPly>
    fun requiresPromotion(ply: IPly): Boolean
    fun isOver(): Result?

    fun play(ply: IPly)
    fun promote(piece: Piece, on: IPly)
    fun resign()
    fun agreeToDraw()
}
