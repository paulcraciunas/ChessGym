package com.paulcraciunas.screens.tools.clock.vm

interface ClockScreenInteractor {
    fun onWhiteTapped()
    fun onBlackTapped()
    fun onStop()
    fun onNewGame()
    fun onTimeSelected(minutes: Int)
    fun onIncrementSelected(increment: Int)
}

class StubClockScreenInteractor : ClockScreenInteractor {
    override fun onWhiteTapped() {}
    override fun onBlackTapped() {}
    override fun onStop() {}
    override fun onNewGame() {}
    override fun onTimeSelected(minutes: Int) {}
    override fun onIncrementSelected(increment: Int) {}
}
