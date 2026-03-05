package com.paulcraciunas.screens.blindmode.vm

import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.controls.SideSelection

interface BlindModeScreenInteractor {
    fun onTrainingModeToggled(enabled: Boolean)
    fun onSideSelected(side: SideSelection)
    fun onPlayClicked()
    fun onSquareClicked(locus: Locus)
    fun onPromote(to: Piece)
    fun onResign()
    fun onReveal()
    fun onPlayAgain()
    fun onBackPressed(): Boolean
    fun onAbandonConfirmed()
    fun onAbandonDismissed()
}

class StubBlindModeScreenInteractor : BlindModeScreenInteractor {
    override fun onTrainingModeToggled(enabled: Boolean) {}
    override fun onSideSelected(side: SideSelection) {}
    override fun onPlayClicked() {}
    override fun onSquareClicked(locus: Locus) {}
    override fun onPromote(to: Piece) {}
    override fun onResign() {}
    override fun onReveal() {}
    override fun onPlayAgain() {}
    override fun onBackPressed(): Boolean = false
    override fun onAbandonConfirmed() {}
    override fun onAbandonDismissed() {}
}
