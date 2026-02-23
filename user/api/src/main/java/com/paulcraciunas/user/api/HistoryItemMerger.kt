package com.paulcraciunas.user.api

import com.paulcraciunas.user.api.User.HistoryItem
import com.paulcraciunas.user.api.User.HistoryItem.HistoryItemData

/**
 * Checks if this history item can be merged with another.
 * Items can be merged if they have the same date and the same data type.
 */
fun HistoryItem.canMergeWith(other: HistoryItem): Boolean =
    timestamp == other.timestamp && data::class == other.data::class

/**
 * Merges this history item with another of the same type and date.
 * Assumes [canMergeWith] returns true.
 */
fun HistoryItem.mergeWith(other: HistoryItem): HistoryItem = copy(
    data = data.mergeWith(other.data)
)

private fun HistoryItemData.mergeWith(other: HistoryItemData): HistoryItemData = when (this) {
    is HistoryItemData.RatedPuzzleData -> mergeWith(other as HistoryItemData.RatedPuzzleData)
    is HistoryItemData.PuzzleRushData -> mergeWith(other as HistoryItemData.PuzzleRushData)
    is HistoryItemData.BoardVisualizationData -> mergeWith(other as HistoryItemData.BoardVisualizationData)
    is HistoryItemData.BlindModeTrainingData -> mergeWith(other as HistoryItemData.BlindModeTrainingData)
    is HistoryItemData.BlindModeData -> mergeWith(other as HistoryItemData.BlindModeData)
    is HistoryItemData.PuzzleStreakData -> mergeWith(other as HistoryItemData.PuzzleStreakData)
    is HistoryItemData.FailedPuzzleData -> mergeWith(other as HistoryItemData.FailedPuzzleData)
}

private fun HistoryItemData.RatedPuzzleData.mergeWith(
    other: HistoryItemData.RatedPuzzleData
): HistoryItemData.RatedPuzzleData = copy(
    puzzlesPlayed = puzzlesPlayed + other.puzzlesPlayed,
    puzzlesSolved = puzzlesSolved + other.puzzlesSolved,
    ratingChange = ratingChange + other.ratingChange,
    timeSpent = timeSpent + other.timeSpent
)

private fun HistoryItemData.PuzzleRushData.mergeWith(
    other: HistoryItemData.PuzzleRushData
): HistoryItemData.PuzzleRushData = copy(
    tries = tries + other.tries,
    bestScore = maxOf(bestScore, other.bestScore),
    timeSpent = timeSpent + other.timeSpent
)

private fun HistoryItemData.BoardVisualizationData.mergeWith(
    other: HistoryItemData.BoardVisualizationData
): HistoryItemData.BoardVisualizationData = copy(
    sessionsCompleted = sessionsCompleted + other.sessionsCompleted,
    timeSpent = timeSpent + other.timeSpent
)

private fun HistoryItemData.BlindModeTrainingData.mergeWith(
    other: HistoryItemData.BlindModeTrainingData
): HistoryItemData.BlindModeTrainingData = copy(
    tries = tries + other.tries,
    mostMovesCompleted = maxOf(mostMovesCompleted, other.mostMovesCompleted),
    timeSpent = timeSpent + other.timeSpent
)

private fun HistoryItemData.BlindModeData.mergeWith(
    other: HistoryItemData.BlindModeData
): HistoryItemData.BlindModeData = copy(
    played = played + other.played,
    ratingChange = ratingChange + other.ratingChange,
    timeSpent = timeSpent + other.timeSpent
)

private fun HistoryItemData.PuzzleStreakData.mergeWith(
    other: HistoryItemData.PuzzleStreakData
): HistoryItemData.PuzzleStreakData = copy(
    finalStreakCount = maxOf(finalStreakCount, other.finalStreakCount),
    timeSpent = timeSpent + other.timeSpent
)

private fun HistoryItemData.FailedPuzzleData.mergeWith(
    other: HistoryItemData.FailedPuzzleData
): HistoryItemData.FailedPuzzleData = copy(
    puzzlesSolved = puzzlesSolved + other.puzzlesSolved,
    timeSpent = timeSpent + other.timeSpent
)
