package com.paulcraciunas.game.api

import com.paulcraciunas.game.Side
import com.paulcraciunas.game.board.Locus
import com.paulcraciunas.game.board.Piece

interface IPuzzle {
    // Methods to query for information
    fun turn(): Side
    fun isOver(): PuzzleResult?
    fun playablePlies(from: Locus): Collection<IPly>
    fun board(): IBoard

    // Methods to play the game
    fun play(ply: IPly)
    fun requiresPromotion(ply: IPly): Boolean
    fun promote(piece: Piece, on: IPly)
    fun resign()

    enum class PuzzleResult {
        Failed,
        Success
    }
}
