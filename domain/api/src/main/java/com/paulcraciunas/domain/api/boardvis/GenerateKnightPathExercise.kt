package com.paulcraciunas.domain.api.boardvis

interface GenerateKnightPathExercise {
    suspend operator fun invoke(movesRequired: Int): KnightPathExercise
}
