package com.paulcraciunas.domain.impl.boardvis

import com.paulcraciunas.domain.api.boardvis.GenerateKnightPathExercise
import com.paulcraciunas.domain.api.boardvis.GetKnightPathBufferedSeries
import com.paulcraciunas.domain.api.boardvis.KnightPathExercise
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetKnightPathBufferedSeriesImpl @Inject constructor(
    private val generateExercise: GenerateKnightPathExercise,
) : GetKnightPathBufferedSeries {
    override fun invoke(bufferSize: Int): Flow<KnightPathExercise>  = flow {
        (GetKnightPathBufferedSeries.MIN_MOVES .. GetKnightPathBufferedSeries.MAX_MOVES).forEach { moves ->
            repeat(EXERCISES_PER_DIFFICULTY) {
                emit(generateExercise(moves))
            }
        }
        repeat(EXHAUSTION_LIMIT) {
            emit(generateExercise(GetKnightPathBufferedSeries.MAX_MOVES))
        }
    }.buffer(capacity = bufferSize, onBufferOverflow = BufferOverflow.SUSPEND)

    companion object {
        private const val EXERCISES_PER_DIFFICULTY = 4
        private const val EXHAUSTION_LIMIT = 100
    }
}
