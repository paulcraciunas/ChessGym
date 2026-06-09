package com.paulcraciunas.screens.data

import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.board.Locus

interface SolutionStrategy {
    fun hintSquare(): Locus?
    fun playNextSolutionMove(): Boolean
    fun hasSolutionMoves(): Boolean
}

class PuzzleSolution(private val puzzle: Puzzle) : SolutionStrategy {
    override fun hintSquare(): Locus? = puzzle.nextExpectedMove()?.first
    override fun playNextSolutionMove(): Boolean {
        if (!hasSolutionMoves()) return false
        puzzle.playNextMove()
        return true
    }

    override fun hasSolutionMoves(): Boolean = !puzzle.state.isOver()
}

object NoOpSolution : SolutionStrategy {
    override fun hintSquare(): Locus? = null
    override fun playNextSolutionMove(): Boolean = false
    override fun hasSolutionMoves(): Boolean = false
}
