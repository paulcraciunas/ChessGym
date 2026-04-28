package com.paulcraciunas.tools.verifier

data class FailedPuzzle(
    val id: String,
    val fen: String,
    val moves: String,
    val failureType: FailureType,
    val errorMessage: String,
)
