package com.paulcraciunas.screens.puzzles.rated.vm

import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank

interface RatedPuzzleScreenInteractor {
    fun onSquareClicked(rank: Rank, file: File)
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
    override fun onSquareClicked(rank: Rank, file: File) {}
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
