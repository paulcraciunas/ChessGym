package com.paulcraciunas.screens.home.vm

import com.paulcraciunas.user.api.User
import javax.inject.Inject

class HomeUiStateAdapter @Inject constructor() {

    fun adapt(user: User) = HomeUiState(
        userProfile = adaptUserProfile(user),
        userStats = adaptUserStats(user),
        history = adaptHistory(user.history),
        isLoading = false
    )

    private fun adaptUserProfile(user: User) = HomeUiState.UserProfile(
        name = "${user.profile.firstName} ${user.profile.lastName}",
        currentRating = user.ratings.current,
        totalActivities = user.history.size,
        joinDate = user.profile.joinDate
    )

    private fun adaptUserStats(user: User) = HomeUiState.Stats(
        puzzlesPlayed = user.statistics.puzzlesPlayed,
        puzzlesSolved = user.statistics.puzzlesSolved,
        currentRating = user.ratings.current,
        bestRating = user.highScores.ratedPuzzle,
        bestPuzzleRushScore = user.highScores.puzzleRush,
        bestBlindModeScore = user.highScores.blindMode,
        bestVisualizationScore = user.highScores.boardVisualization
    )

    private fun adaptHistory(history: List<User.HistoryItem>): List<HomeUiState.HistoryGroup> {
        return history
            .groupBy { it.timestamp }
            .map { (date, items) ->
                HomeUiState.HistoryGroup(
                    date = date,
                    events = items.map { adaptHistoryItem(it) }
                )
            }
            .sortedByDescending { it.date }
    }

    private fun adaptHistoryItem(historyItem: User.HistoryItem): HomeUiState.HistoryEvent = when (val data = historyItem.data) {
        is User.HistoryItem.HistoryItemData.PuzzleRushData -> {
            HomeUiState.HistoryEvent.PuzzleRushEvent(
                highScore = data.bestScore,
                runs = data.tries
            )
        }

        is User.HistoryItem.HistoryItemData.BoardVisualizationData -> {
            HomeUiState.HistoryEvent.BoardVizEvent(
                runs = data.sessionsCompleted
            )
        }

        is User.HistoryItem.HistoryItemData.BlindModeTrainingData -> {
            HomeUiState.HistoryEvent.BlindModeTrainingEvent(
                mostMovesCompleted = data.mostMovesCompleted,
                runs = data.tries
            )
        }

        is User.HistoryItem.HistoryItemData.BlindModeData -> {
            HomeUiState.HistoryEvent.BlindModeEvent(
                ratingChange = data.ratingChange,
                gamesPlayed = data.played
            )
        }

        is User.HistoryItem.HistoryItemData.RatedPuzzleData -> {
            HomeUiState.HistoryEvent.RatedPuzzleEvent(
                ratingChange = data.ratingChange,
                count = data.puzzlesPlayed
            )
        }
    }
}
