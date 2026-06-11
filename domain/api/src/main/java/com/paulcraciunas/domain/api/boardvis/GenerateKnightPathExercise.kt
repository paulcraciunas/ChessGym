package com.paulcraciunas.domain.api.boardvis

interface GenerateKnightPathExercise {
    operator fun invoke(movesRequired: Int): KnightPathExercise
}
