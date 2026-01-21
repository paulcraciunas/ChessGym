package com.paulcraciunas.screens.puzzles.streak.vm

import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

interface PuzzleStreakScreenInteractor {
    fun onSquareClicked(selection: Locus)
    fun onPromote(to: Piece)
    fun onHintRequested()
    fun onNewStreak()
    fun onDismissSummary()
}

class StubPuzzleStreakScreenInteractor : PuzzleStreakScreenInteractor {
    override fun onSquareClicked(selection: Locus) {}
    override fun onPromote(to: Piece) {}
    override fun onHintRequested() {}
    override fun onNewStreak() {}
    override fun onDismissSummary() {}
}
