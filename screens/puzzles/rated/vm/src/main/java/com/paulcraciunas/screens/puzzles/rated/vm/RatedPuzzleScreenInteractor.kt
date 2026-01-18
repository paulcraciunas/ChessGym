package com.paulcraciunas.screens.puzzles.rated.vm

import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

interface RatedPuzzleScreenInteractor {
    fun onSquareClicked(selection: Locus)
    fun onPromote(to: Piece)
    fun onHintRequested()
    fun onAbandon()
    fun onAbandonConfirmed()
    fun onAbandonDismissed()
    fun onNavigateToStart()
    fun onNavigateBack()
    fun onNavigateNext()
    fun onNavigateToEnd()
    fun onNextPuzzle()
}

class StubRatedPuzzleScreenInteractor : RatedPuzzleScreenInteractor {
    override fun onSquareClicked(selection: Locus) {}
    override fun onPromote(to: Piece) {}
    override fun onHintRequested() {}
    override fun onAbandon() {}
    override fun onAbandonConfirmed() {}
    override fun onAbandonDismissed() {}
    override fun onNavigateToStart() {}
    override fun onNavigateBack() {}
    override fun onNavigateNext() {}
    override fun onNavigateToEnd() {}
    override fun onNextPuzzle() {}
}
