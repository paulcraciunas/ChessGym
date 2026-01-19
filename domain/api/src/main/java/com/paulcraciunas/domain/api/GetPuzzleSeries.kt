package com.paulcraciunas.domain.api

import com.paulcraciunas.game.logic.api.Puzzle

interface GetPuzzleSeries {
    suspend operator fun invoke(count: Int = COUNT, increment: Int = INCREMENT, from: Int = RATING_START): List<Puzzle>

    companion object Defaults {
        const val RATING_START = 400
        const val INCREMENT = 60
        const val COUNT = 100
    }
}
