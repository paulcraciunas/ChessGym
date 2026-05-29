package com.paulcraciunas.screens.common.model

import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.board.Locus

interface SolutionStrategy {
    fun load(playable: PlayableBoard)

    fun hintSquare(): Locus?
    fun playNextSolutionMove(): Boolean
    fun hasSolutionMoves(): Boolean
}

class PuzzleSolution : SolutionStrategy {
    private var puzzle: Puzzle? = null
    override fun load(playable: PlayableBoard) {
        this.puzzle = (playable as? PuzzlePlayableBoard)?.puzzle
    }

    override fun hintSquare(): Locus? = puzzle?.nextExpectedMove()?.first
    override fun playNextSolutionMove(): Boolean {
        if (!hasSolutionMoves()) return false
        puzzle?.playNextMove()
        return true
    }

    override fun hasSolutionMoves(): Boolean = puzzle?.state?.isOver() == false

    companion object {
        const val SOLUTION_MOVE_DELAY_MS = 600L
    }
}

object NoOpSolution : SolutionStrategy {
    override fun load(playable: PlayableBoard) {}
    override fun hintSquare(): Locus? = null
    override fun playNextSolutionMove(): Boolean = false
    override fun hasSolutionMoves(): Boolean = false
}
