package com.paulcraciunas.screens.home.vm

import com.paulcraciunas.user.api.User
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

class HomeUiStateAdapter @Inject constructor() {

    fun adapt(user: User): HomeUiState = adapt(user, LocalDate.now())

    internal fun adapt(user: User, today: LocalDate): HomeUiState = HomeUiState(
        userProfile = adaptUserProfile(user),
        userStats = adaptUserStats(user),
        history = adaptHistory(user.history, today),
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
        bestPuzzleStreakScore = user.highScores.puzzleStreak,
        bestFindTheSquareScore = user.highScores.findTheSquare,
        bestMoveThePieceScore = user.highScores.moveThePiece,
        bestBlindModeScore = user.highScores.blindMode,
    )

    private fun adaptHistory(
        history: List<User.HistoryItem>,
        today: LocalDate,
    ): List<HomeUiState.HistoryGroup> {
        return history
            .groupBy { timePeriod(it.timestamp, today) }
            .map { (label, items) ->
                HomeUiState.HistoryGroup(
                    label = label,
                    events = items.map { adaptHistoryItem(it) }
                )
            }
            .sortedBy { TIME_PERIOD_ORDER.indexOf(it.label) }
    }

    private fun adaptHistoryItem(historyItem: User.HistoryItem): HomeUiState.HistoryEvent =
        when (val data = historyItem.data) {
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

            is User.HistoryItem.HistoryItemData.PuzzleStreakData -> {
                HomeUiState.HistoryEvent.PuzzleStreakEvent(
                    finalStreakCount = data.finalStreakCount
                )
            }

            is User.HistoryItem.HistoryItemData.FailedPuzzleData -> {
                HomeUiState.HistoryEvent.FailedPuzzleEvent(
                    puzzlesSolved = data.puzzlesSolved
                )
            }
        }

    companion object {
        internal const val LABEL_TODAY = "Today"
        internal const val LABEL_YESTERDAY = "Yesterday"
        internal const val LABEL_THIS_WEEK = "This Week"
        internal const val LABEL_THIS_MONTH = "This Month"
        internal const val LABEL_THIS_YEAR = "This Year"
        internal const val LABEL_OLDER = "Older"

        private val TIME_PERIOD_ORDER = listOf(
            LABEL_TODAY,
            LABEL_YESTERDAY,
            LABEL_THIS_WEEK,
            LABEL_THIS_MONTH,
            LABEL_THIS_YEAR,
            LABEL_OLDER
        )

        internal fun timePeriod(date: LocalDate, today: LocalDate): String {
            val yesterday = today.minusDays(1)
            val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val startOfMonth = today.withDayOfMonth(1)
            val startOfYear = today.withDayOfYear(1)

            return when {
                date == today -> LABEL_TODAY
                date == yesterday -> LABEL_YESTERDAY
                !date.isBefore(startOfWeek) -> LABEL_THIS_WEEK
                !date.isBefore(startOfMonth) -> LABEL_THIS_MONTH
                !date.isBefore(startOfYear) -> LABEL_THIS_YEAR
                else -> LABEL_OLDER
            }
        }
    }
}
