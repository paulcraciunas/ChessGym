package com.paulcraciunas.screens.puzzles.rush.vm

import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

interface PuzzleRushScreenInteractor {
    fun onSquareClicked(selection: Locus)
    fun onPromote(to: Piece)
    fun onPlayAgain()
    fun onDismissSummary()
}

class StubPuzzleRushScreenInteractor : PuzzleRushScreenInteractor {
    override fun onSquareClicked(selection: Locus) {}
    override fun onPromote(to: Piece) {}
    override fun onPlayAgain() {}
    override fun onDismissSummary() {}
}
