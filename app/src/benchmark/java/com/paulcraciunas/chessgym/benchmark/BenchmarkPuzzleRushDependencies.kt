package com.paulcraciunas.chessgym.benchmark

import com.paulcraciunas.domain.api.general.CountdownTimer
import com.paulcraciunas.domain.api.puzzles.GetBufferedPuzzleSeries
import com.paulcraciunas.domain.api.puzzles.GetPuzzleFen
import com.paulcraciunas.domain.api.puzzles.OnPuzzleRushComplete
import com.paulcraciunas.domain.api.puzzles.PuzzleAnalysisData
import com.paulcraciunas.domain.api.puzzles.PuzzleRushResult
import com.paulcraciunas.domain.impl.general.RealCountdownTimer
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository
import com.paulcraciunas.user.api.AuthResult
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class BenchmarkPuzzleRushDependencies {
    val puzzleSeries: GetBufferedPuzzleSeries = BenchmarkGetBufferedPuzzleSeries()
    val onPuzzleRushComplete: OnPuzzleRushComplete = NoOpOnPuzzleRushComplete()
    val countdownTimer: CountdownTimer = RealCountdownTimer()
    val appSettingsRepository: AppSettingsRepository = FakeAppSettingsRepository.default()
    val userRepository: UserRepository = BenchmarkUserRepository()
    val getPuzzleFen: GetPuzzleFen = NoOpGetPuzzleFen()
}

private class NoOpOnPuzzleRushComplete : OnPuzzleRushComplete {
    override suspend fun invoke(result: PuzzleRushResult) = Unit
}

private class NoOpGetPuzzleFen : GetPuzzleFen {
    override suspend fun invoke(puzzleId: Int): PuzzleAnalysisData? = null
}

private class BenchmarkUserRepository : UserRepository {
    private val user = MutableStateFlow(User())

    override fun userUpdates(): Flow<User> = user
    override suspend fun get(): User = user.value
    override suspend fun update(updated: User) { user.value = updated }
    override suspend fun logHistory(history: List<User.HistoryItem>) = Unit
    override suspend fun signIn(authResult: AuthResult): User = user.value
    override suspend fun signOut() = Unit
    override suspend fun clear() = Unit
    override suspend fun sync() = Unit
}
