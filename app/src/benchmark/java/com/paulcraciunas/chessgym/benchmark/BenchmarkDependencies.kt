package com.paulcraciunas.chessgym.benchmark

import com.paulcraciunas.domain.api.general.FakeTimer
import com.paulcraciunas.domain.api.general.Timer
import com.paulcraciunas.domain.api.puzzles.GetStreakPuzzle
import com.paulcraciunas.domain.api.puzzles.OnStreakComplete
import com.paulcraciunas.domain.api.puzzles.OnStreakPuzzleComplete
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository

class BenchmarkDependencies {
    val getStreakPuzzle: GetStreakPuzzle = BenchmarkGetStreakPuzzle()
    val onStreakPuzzleComplete: OnStreakPuzzleComplete = BenchmarkOnStreakPuzzleComplete()
    val onStreakComplete: OnStreakComplete = BenchmarkOnStreakComplete()
    val appSettingsRepository: AppSettingsRepository = FakeAppSettingsRepository.default()
    val timer: Timer = FakeTimer()
}

private class BenchmarkOnStreakPuzzleComplete : OnStreakPuzzleComplete {
    override suspend fun invoke(timeSpentMillis: Long) = Unit
}

private class BenchmarkOnStreakComplete : OnStreakComplete {
    override suspend fun invoke(timeSpentMillis: Long): OnStreakComplete.StreakCompleteResult =
        OnStreakComplete.StreakCompleteResult(
            isNewHighScore = false,
            finalStreakCount = 0,
        )
}
