package com.paulcraciunas.user.api

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class HistoryItemMergerTest {
    private val today = LocalDate.now()
    private val yesterday = LocalDate.now().minusDays(1)

    @Test
    fun `GIVEN same date and type WHEN canMergeWith THEN returns true`() {
        val item1 = ratedPuzzleItem(today)
        val item2 = ratedPuzzleItem(today)

        assertTrue(item1.canMergeWith(item2))
    }

    @Test
    fun `GIVEN different date same type WHEN canMergeWith THEN returns false`() {
        val item1 = ratedPuzzleItem(today)
        val item2 = ratedPuzzleItem(yesterday)

        assertFalse(item1.canMergeWith(item2))
    }

    @Test
    fun `GIVEN same date different type WHEN canMergeWith THEN returns false`() {
        val ratedItem = ratedPuzzleItem(today)
        val rushItem = puzzleRushItem(today)

        assertFalse(ratedItem.canMergeWith(rushItem))
    }

    @Test
    fun `GIVEN RatedPuzzleData WHEN mergeWith THEN sums all fields`() {
        val item1 = User.HistoryItem(
            timestamp = today,
            data = User.HistoryItem.HistoryItemData.RatedPuzzleData(
                puzzlesPlayed = 5,
                puzzlesSolved = 3,
                ratingChange = 20,
                timeSpent = 1000L
            )
        )
        val item2 = User.HistoryItem(
            timestamp = today,
            data = User.HistoryItem.HistoryItemData.RatedPuzzleData(
                puzzlesPlayed = 3,
                puzzlesSolved = 2,
                ratingChange = -10,
                timeSpent = 500L
            )
        )

        val merged = item1.mergeWith(item2)
        val data = merged.data as User.HistoryItem.HistoryItemData.RatedPuzzleData

        assertEquals(8, data.puzzlesPlayed)
        assertEquals(5, data.puzzlesSolved)
        assertEquals(10, data.ratingChange)
        assertEquals(1500L, data.timeSpent)
    }

    @Test
    fun `GIVEN PuzzleRushData WHEN mergeWith THEN sums tries and time and takes max score`() {
        val item1 = User.HistoryItem(
            timestamp = today,
            data = User.HistoryItem.HistoryItemData.PuzzleRushData(
                tries = 2,
                bestScore = 50,
                timeSpent = 1000L
            )
        )
        val item2 = User.HistoryItem(
            timestamp = today,
            data = User.HistoryItem.HistoryItemData.PuzzleRushData(
                tries = 3,
                bestScore = 75,
                timeSpent = 800L
            )
        )

        val merged = item1.mergeWith(item2)
        val data = merged.data as User.HistoryItem.HistoryItemData.PuzzleRushData

        assertEquals(5, data.tries)
        assertEquals(75, data.bestScore)
        assertEquals(1800L, data.timeSpent)
    }

    @Test
    fun `GIVEN BoardVisualizationData WHEN mergeWith THEN sums all fields`() {
        val item1 = User.HistoryItem(
            timestamp = today,
            data = User.HistoryItem.HistoryItemData.BoardVisualizationData(
                sessionsCompleted = 3,
                timeSpent = 600L
            )
        )
        val item2 = User.HistoryItem(
            timestamp = today,
            data = User.HistoryItem.HistoryItemData.BoardVisualizationData(
                sessionsCompleted = 2,
                timeSpent = 400L
            )
        )

        val merged = item1.mergeWith(item2)
        val data = merged.data as User.HistoryItem.HistoryItemData.BoardVisualizationData

        assertEquals(5, data.sessionsCompleted)
        assertEquals(1000L, data.timeSpent)
    }

    @Test
    fun `GIVEN BlindModeTrainingData WHEN mergeWith THEN sums tries and time and takes max moves`() {
        val item1 = User.HistoryItem(
            timestamp = today,
            data = User.HistoryItem.HistoryItemData.BlindModeTrainingData(
                tries = 4,
                mostMovesCompleted = 10,
                timeSpent = 500L
            )
        )
        val item2 = User.HistoryItem(
            timestamp = today,
            data = User.HistoryItem.HistoryItemData.BlindModeTrainingData(
                tries = 2,
                mostMovesCompleted = 15,
                timeSpent = 300L
            )
        )

        val merged = item1.mergeWith(item2)
        val data = merged.data as User.HistoryItem.HistoryItemData.BlindModeTrainingData

        assertEquals(6, data.tries)
        assertEquals(15, data.mostMovesCompleted)
        assertEquals(800L, data.timeSpent)
    }

    @Test
    fun `GIVEN BlindModeData WHEN mergeWith THEN sums all fields`() {
        val item1 = User.HistoryItem(
            timestamp = today,
            data = User.HistoryItem.HistoryItemData.BlindModeData(
                played = 5,
                ratingChange = 30,
                timeSpent = 1200L
            )
        )
        val item2 = User.HistoryItem(
            timestamp = today,
            data = User.HistoryItem.HistoryItemData.BlindModeData(
                played = 3,
                ratingChange = -20,
                timeSpent = 800L
            )
        )

        val merged = item1.mergeWith(item2)
        val data = merged.data as User.HistoryItem.HistoryItemData.BlindModeData

        assertEquals(8, data.played)
        assertEquals(10, data.ratingChange)
        assertEquals(2000L, data.timeSpent)
    }

    private fun ratedPuzzleItem(date: LocalDate) = User.HistoryItem(
        timestamp = date,
        data = User.HistoryItem.HistoryItemData.RatedPuzzleData(
            puzzlesPlayed = 1,
            puzzlesSolved = 1,
            ratingChange = 10,
            timeSpent = 100L
        )
    )

    private fun puzzleRushItem(date: LocalDate) = User.HistoryItem(
        timestamp = date,
        data = User.HistoryItem.HistoryItemData.PuzzleRushData(
            tries = 1,
            bestScore = 50,
            timeSpent = 100L
        )
    )
}
