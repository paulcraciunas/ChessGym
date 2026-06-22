package com.paulcraciunas.domain.impl.analysis

import com.paulcraciunas.domain.api.analysis.MoveClassification
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class MoveClassifierTest {

    @Test
    fun `GIVEN negative centipawn loss WHEN classify THEN returns Brilliant`() {
        assertEquals(MoveClassification.Brilliant, MoveClassifier.classify(-100))
        assertEquals(MoveClassification.Brilliant, MoveClassifier.classify(-51))
    }

    @Test
    fun `GIVEN centipawn loss at brilliant boundary WHEN classify THEN returns Brilliant`() {
        assertEquals(MoveClassification.Brilliant, MoveClassifier.classify(-50))
    }

    @Test
    fun `GIVEN centipawn loss between brilliant and great WHEN classify THEN returns Great`() {
        assertEquals(MoveClassification.Great, MoveClassifier.classify(-49))
        assertEquals(MoveClassification.Great, MoveClassifier.classify(-1))
        assertEquals(MoveClassification.Great, MoveClassifier.classify(0))
    }

    @Test
    fun `GIVEN small centipawn loss WHEN classify THEN returns Good`() {
        assertEquals(MoveClassification.Good, MoveClassifier.classify(1))
        assertEquals(MoveClassification.Good, MoveClassifier.classify(99))
    }

    @Test
    fun `GIVEN moderate centipawn loss WHEN classify THEN returns Inaccuracy`() {
        assertEquals(MoveClassification.Inaccuracy, MoveClassifier.classify(101))
        assertEquals(MoveClassification.Inaccuracy, MoveClassifier.classify(150))
    }

    @Test
    fun `GIVEN large centipawn loss WHEN classify THEN returns Mistake`() {
        assertEquals(MoveClassification.Mistake, MoveClassifier.classify(151))
        assertEquals(MoveClassification.Mistake, MoveClassifier.classify(200))
    }

    @Test
    fun `GIVEN very large centipawn loss WHEN classify THEN returns Blunder`() {
        assertEquals(MoveClassification.Blunder, MoveClassifier.classify(201))
        assertEquals(MoveClassification.Blunder, MoveClassifier.classify(300))
        assertEquals(MoveClassification.Blunder, MoveClassifier.classify(1000))
    }
}
