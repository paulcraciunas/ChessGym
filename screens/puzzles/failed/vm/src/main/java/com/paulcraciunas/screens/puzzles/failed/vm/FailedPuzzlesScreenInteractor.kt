package com.paulcraciunas.screens.puzzles.failed.vm

import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

interface FailedPuzzlesScreenInteractor {
    fun onSquareClicked(selection: Locus)
    fun onPromote(to: Piece)
    fun onDismissCompletion()
    fun onAnalyzeFailedPuzzle(puzzleId: Int)
}

class StubFailedPuzzlesScreenInteractor : FailedPuzzlesScreenInteractor {
    override fun onSquareClicked(selection: Locus) {}
    override fun onPromote(to: Piece) {}
    override fun onDismissCompletion() {}
    override fun onAnalyzeFailedPuzzle(puzzleId: Int) {}
}
