package com.paulcraciunas.domain.api.analysis

data class GameAnalysis(
    val moves: List<MoveAnalysis>,
    val averageCentipawnLoss: Float,
    val blunders: List<Int>,
    val mistakes: List<Int>,
    val inaccuracies: List<Int>,
    val brilliancies: List<Int>,
)
