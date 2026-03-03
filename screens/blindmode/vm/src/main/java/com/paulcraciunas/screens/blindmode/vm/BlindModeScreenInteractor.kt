package com.paulcraciunas.screens.blindmode.vm

import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

interface BlindModeScreenInteractor {
    fun onTrainingModeToggled(enabled: Boolean)
    fun onPlayClicked()
    fun onSquareClicked(locus: Locus)
    fun onPromote(to: Piece)
    fun onResign()
    fun onReveal()
    fun onPlayAgain()
}

class StubBlindModeScreenInteractor : BlindModeScreenInteractor {
    override fun onTrainingModeToggled(enabled: Boolean) {}
    override fun onPlayClicked() {}
    override fun onSquareClicked(locus: Locus) {}
    override fun onPromote(to: Piece) {}
    override fun onResign() {}
    override fun onReveal() {}
    override fun onPlayAgain() {}
}
