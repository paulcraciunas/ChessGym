package com.paulcraciunas.domain.impl.analysis

import com.paulcraciunas.domain.api.analysis.MoveClassification

internal object MoveClassifier {
    private const val BRILLIANT_THRESHOLD = -50
    private const val GREAT_THRESHOLD = 0
    private const val GOOD_THRESHOLD = 100
    private const val INACCURACY_THRESHOLD = 150
    private const val MISTAKE_THRESHOLD = 200
    // Beyond MISTAKE_THRESHOLD is a Blunder

    fun classify(centipawnLoss: Int): MoveClassification = when {
        centipawnLoss <= BRILLIANT_THRESHOLD -> MoveClassification.Brilliant
        centipawnLoss <= GREAT_THRESHOLD -> MoveClassification.Great
        centipawnLoss <= GOOD_THRESHOLD -> MoveClassification.Good
        centipawnLoss <= INACCURACY_THRESHOLD -> MoveClassification.Inaccuracy
        centipawnLoss <= MISTAKE_THRESHOLD -> MoveClassification.Mistake
        else -> MoveClassification.Blunder
    }
}
