package com.paulcraciunas.screens.achievements.vm

interface AchievementsInteractor {
    fun onScreenVisible()
}

class StubAchievementsInteractor : AchievementsInteractor {
    override fun onScreenVisible() {}
}
