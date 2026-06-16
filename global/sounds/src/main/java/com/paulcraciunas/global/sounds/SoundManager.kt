package com.paulcraciunas.global.sounds

interface SoundManager {
    fun playMove()
    fun playGameStart()
    fun playGameOver()
    fun startTickCountdown(totalSeconds: Int)
    fun release()
}

// Used in previews and such
class FakeSoundManager : SoundManager {
    override fun playMove() {}
    override fun playGameStart() {}
    override fun playGameOver() {}
    override fun startTickCountdown(totalSeconds: Int) {}
    override fun release() {}
}
