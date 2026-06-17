package com.paulcraciunas.chessgym.main

import com.paulcraciunas.chessgym.debug.DebugMenuProvider
import com.paulcraciunas.domain.api.achievements.AchievementNotificationManager
import com.paulcraciunas.global.navigation.NavigationDispatcher
import com.paulcraciunas.global.sounds.SoundCoordinator
import com.paulcraciunas.global.sounds.SoundManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MainScreenEntryPoint {
    fun debugMenuProvider(): DebugMenuProvider
    fun navigationDispatcher(): NavigationDispatcher
    fun soundCoordinator(): SoundCoordinator
    fun soundManager(): SoundManager
    fun achievementNotificationManager(): AchievementNotificationManager
}
