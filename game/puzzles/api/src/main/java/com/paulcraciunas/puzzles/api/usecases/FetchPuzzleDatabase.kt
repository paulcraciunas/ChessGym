package com.paulcraciunas.puzzles.api.usecases

import kotlinx.coroutines.flow.Flow

interface FetchPuzzleDatabase {
    operator fun invoke(): Flow<Progress>

    data class Progress(
        val download: Int,
        val unpack: Int,
        val buildDb: Int,
    ) {
        init {
            assert(download in 0..100)
            assert(unpack in 0..100)
            assert(buildDb in 0..100)
        }

        fun isComplete(): Boolean = download == 100 && unpack == 100 && buildDb == 100

        companion object {
            fun empty(): Progress = Progress(download = 0, unpack = 0, buildDb = 0)
        }
    }
}
