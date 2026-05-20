package com.paulcraciunas.screens.puzzles.streak.vm

import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

interface PuzzleStreakScreenInteractor {
    fun onSquareClicked(selection: Locus)
    fun onPromote(to: Piece)
    fun onHintRequested()
    fun onAbandon()
    fun onAbandonConfirmed()
    fun onAbandonDismissed()
    fun onNewStreak()
    fun onNextPuzzle()
    fun onDismissSummary()
}

class StubPuzzleStreakScreenInteractor : PuzzleStreakScreenInteractor {
    override fun onSquareClicked(selection: Locus) {}
    override fun onPromote(to: Piece) {}
    override fun onHintRequested() {}
    override fun onAbandon() {}
    override fun onAbandonConfirmed() {}
    override fun onAbandonDismissed() {}
    override fun onNewStreak() {}
    override fun onNextPuzzle() {}
    override fun onDismissSummary() {}
}
