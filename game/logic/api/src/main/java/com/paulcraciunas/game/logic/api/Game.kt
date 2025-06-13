package com.paulcraciunas.game.logic.api

import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.state.GameInfo
import com.paulcraciunas.game.logic.api.state.MetaData

interface Game {
    val info: GameInfo

    fun turn(): Side
    fun board(): IBoard
    fun state(): GameInfo
    fun metaData(): MetaData

    fun allPlies(): List<Ply>
    fun playablePlies(from: Locus): Collection<Ply>
    fun allPlayablePlies(): Collection<Ply>
    fun isOver(): Result?

    fun play(ply: Ply)
    fun promote(piece: Piece, on: Ply)
    fun resign()
    fun agreeToDraw()
}
