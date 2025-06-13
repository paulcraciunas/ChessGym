package com.paulcraciunas.game.logic.api

import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus

// TODO Paul: clean up api. Also add rating somewhere
interface IPuzzle {
    fun turn(): Side
    fun board(): IBoard
    fun isOver(): Result?
    fun playablePlies(from: Locus): Collection<Ply>

    fun start()
    fun play(ply: Ply)
    fun resign()

    enum class Result {
        Failed,
        Success
    }
}
