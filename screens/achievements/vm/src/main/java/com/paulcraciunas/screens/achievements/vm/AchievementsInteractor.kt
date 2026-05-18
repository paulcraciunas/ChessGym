package com.paulcraciunas.screens.achievements.vm

interface AchievementsInteractor {
    fun onScreenVisible()
    fun onAchievementClicked(achievement: AchievementsUiState.AchievementState)
    fun onDismissDetail()
}

class StubAchievementsInteractor : AchievementsInteractor {
    override fun onScreenVisible() {}
    override fun onAchievementClicked(achievement: AchievementsUiState.AchievementState) {}
    override fun onDismissDetail() {}
}
