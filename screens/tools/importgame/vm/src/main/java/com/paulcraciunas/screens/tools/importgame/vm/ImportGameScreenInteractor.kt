package com.paulcraciunas.screens.tools.importgame.vm

import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

interface ImportGameScreenInteractor {
    fun onFenClicked()
    fun onPgnClicked()
    fun onImport(text: String)
    fun onDismissDialog()
    fun onSquareClicked(locus: Locus)
    fun onPromote(to: Piece)
    fun onJumpToStart()
    fun onPreviousMove()
    fun onNextMove()
    fun onJumpToEnd()
}

class StubImportGameScreenInteractor : ImportGameScreenInteractor {
    override fun onFenClicked() {}
    override fun onPgnClicked() {}
    override fun onImport(text: String) {}
    override fun onDismissDialog() {}
    override fun onSquareClicked(locus: Locus) {}
    override fun onPromote(to: Piece) {}
    override fun onJumpToStart() {}
    override fun onPreviousMove() {}
    override fun onNextMove() {}
    override fun onJumpToEnd() {}
}
