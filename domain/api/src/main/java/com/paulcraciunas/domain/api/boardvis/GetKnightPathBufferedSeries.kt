package com.paulcraciunas.domain.api.boardvis

import kotlinx.coroutines.flow.Flow

interface GetKnightPathBufferedSeries {
    operator fun invoke(bufferSize: Int = BUFFER_SIZE): Flow<KnightPathExercise>

    companion object Defaults {
        const val BUFFER_SIZE = 5
        const val MIN_MOVES = 2
        const val MAX_MOVES = 6
    }
}
