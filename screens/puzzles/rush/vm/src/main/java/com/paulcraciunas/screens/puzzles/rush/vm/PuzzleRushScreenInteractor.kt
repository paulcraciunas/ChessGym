package com.paulcraciunas.screens.puzzles.rush.vm

import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

interface PuzzleRushScreenInteractor {
    fun onSquareClicked(selection: Locus)
    fun onPromote(to: Piece)
    fun onPlayAgain()
    fun onDismissSummary()
    fun onAnalyzeFailedPuzzle(puzzleId: Int)
}

class StubPuzzleRushScreenInteractor : PuzzleRushScreenInteractor {
    override fun onSquareClicked(selection: Locus) {}
    override fun onPromote(to: Piece) {}
    override fun onPlayAgain() {}
    override fun onDismissSummary() {}
    override fun onAnalyzeFailedPuzzle(puzzleId: Int) {}
}
