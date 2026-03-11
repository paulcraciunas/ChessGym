package com.paulcraciunas.chessgym.dsl.setup

import com.paulcraciunas.user.api.User
import java.time.LocalDate

object Puzzles {
    fun historyItem(
        tries: Int = 5,
        score: Int = 18,
        timeSpent: Long = 300_000,
        timestamp: LocalDate = LocalDate.now()
    ) = User.HistoryItem(
        timestamp = timestamp,
        data = User.HistoryItem.HistoryItemData.PuzzleRushData(
            tries = tries,
            bestScore = score,
            timeSpent = timeSpent,
        )
    )

    fun ratedItem(
        played: Int = 12,
        solved: Int = 10,
        ratingChange: Int = 42,
        timeSpent: Long = 600_000,
        timestamp: LocalDate = LocalDate.now()
    ) = User.HistoryItem(
        timestamp = timestamp,
        data = User.HistoryItem.HistoryItemData.RatedPuzzleData(
            puzzlesPlayed = played,
            puzzlesSolved = solved,
            ratingChange = ratingChange,
            timeSpent = timeSpent,
        )
    )
}
