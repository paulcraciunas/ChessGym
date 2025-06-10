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

    fun allPlies(): List<Ply>
    fun playablePlies(from: Locus): Collection<Ply>
    fun allPlayablePlies(): Collection<Ply>
    fun requiresPromotion(ply: Ply): Boolean
    fun isOver(): Result?

    fun play(ply: Ply)
    fun promote(piece: Piece, on: Ply)
    fun resign()
    fun agreeToDraw()
}
