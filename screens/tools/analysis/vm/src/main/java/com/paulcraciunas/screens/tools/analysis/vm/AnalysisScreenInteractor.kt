package com.paulcraciunas.screens.tools.analysis.vm

import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

interface AnalysisScreenInteractor {
    fun onSquareClicked(locus: Locus)
    fun onPromote(to: Piece)
    fun onFlipBoard()
}

class StubAnalysisScreenInteractor : AnalysisScreenInteractor {
    override fun onSquareClicked(locus: Locus) {}
    override fun onPromote(to: Piece) {}
    override fun onFlipBoard() {}
}
