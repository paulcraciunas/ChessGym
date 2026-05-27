package com.paulcraciunas.chessgym.benchmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.paulcraciunas.screens.puzzles.rush.vm.PuzzleRushViewModel
import com.paulcraciunas.screens.puzzles.streak.vm.PuzzleStreakViewModel
import com.paulcraciunas.screens.puzzles.streak.vm.PuzzleStreakViewModel2

object BenchmarkViewModelFactory {
    fun v1(deps: BenchmarkDependencies): ViewModelProvider.Factory =
        factory {
            PuzzleStreakViewModel(
                getStreakPuzzle = deps.getStreakPuzzle,
                onStreakPuzzleComplete = deps.onStreakPuzzleComplete,
                onStreakComplete = deps.onStreakComplete,
                appSettingsRepository = deps.appSettingsRepository,
                timer = deps.timer,
            )
        }

    fun v2(deps: BenchmarkDependencies): ViewModelProvider.Factory =
        factory {
            PuzzleStreakViewModel2(
                getStreakPuzzle = deps.getStreakPuzzle,
                onStreakPuzzleComplete = deps.onStreakPuzzleComplete,
                onStreakComplete = deps.onStreakComplete,
                appSettingsRepository = deps.appSettingsRepository,
                timer = deps.timer,
            )
        }

    private inline fun <reified T : ViewModel> factory(
        crossinline create: () -> T,
    ): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
                require(modelClass.isAssignableFrom(T::class.java)) {
                    "Unknown ViewModel class ${modelClass.name}"
                }
                return create() as VM
            }
        }
}

class BenchmarkPuzzleRushViewModelFactory(
    private val deps: BenchmarkPuzzleRushDependencies,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
        require(modelClass.isAssignableFrom(PuzzleRushViewModel::class.java)) {
            "Unknown ViewModel class ${modelClass.name}"
        }
        return PuzzleRushViewModel(
            puzzleSeries = deps.puzzleSeries,
            onPuzzleRushComplete = deps.onPuzzleRushComplete,
            countdownTimer = deps.countdownTimer,
            appSettingsRepository = deps.appSettingsRepository,
            userRepository = deps.userRepository,
            getPuzzleFen = deps.getPuzzleFen,
        ) as VM
    }
}
