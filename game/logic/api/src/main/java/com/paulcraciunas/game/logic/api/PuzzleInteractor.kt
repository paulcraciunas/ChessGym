package com.paulcraciunas.game.logic.api

import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

interface PuzzleInteractor {
    val id: Int?
    val rating: Int
    val player: Side
    val captured: Map<Side, List<Piece>>
    val lastPly: Ply?

    fun load(puzzle: Puzzle)

    fun canPlay(from: Locus, to: Locus): Boolean
    fun moves(from: Locus): List<Locus>
    fun play(from: Locus, to: Locus)
    fun canPromote(from: Locus, to: Locus): Boolean
    fun promote(from: Locus, to: Locus, result: Piece)
    fun hint(): Locus
    fun resign()

    /**
     * Plays the next expected move in the solution sequence.
     * This is used to show the solution when the user resigns.
     */
    fun playNextMove()

    fun isOver(): Boolean
    fun isSuccess(): Boolean
}
