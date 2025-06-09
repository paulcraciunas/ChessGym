package com.paulcraciunas.game.logic.api

import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.state.IGameState

// TODO Paul: clean up api. Also add rating somewhere
interface IPuzzle {
    fun turn(): Side
    fun board(): IBoard
    fun state(): IGameState
    fun isOver(): Result?
    fun playablePlies(from: Locus): Collection<IPly>
    fun requiresPromotion(ply: IPly): Boolean

    fun play(ply: IPly)
    fun promote(piece: Piece, on: IPly)
    fun resign()

    enum class Result {
        Failed,
        Success
    }
}
