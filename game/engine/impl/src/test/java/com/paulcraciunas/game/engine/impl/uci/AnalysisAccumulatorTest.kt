package com.paulcraciunas.game.engine.impl.uci

import com.paulcraciunas.game.engine.api.Evaluation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class AnalysisAccumulatorTest {

    @Test
    fun `GIVEN multipv 1 line WHEN process THEN emits result immediately`() {
        val accumulator = AnalysisAccumulator(multiPvCount = 3)
        val parsed = parsedInfo(depth = 1, multiPv = 1, cp = 30, pv = listOf("e2e4"))

        val result = accumulator.process(parsed)

        assertNotNull(result)
        assertEquals(1, result!!.depth)
        assertEquals(Evaluation.Centipawns(30), result.evaluation)
        assertEquals(1, result.lines.size)
    }

    @Test
    fun `GIVEN multipv 2 line without prior multipv 1 WHEN process THEN does not emit`() {
        val accumulator = AnalysisAccumulator(multiPvCount = 3)
        val parsed = parsedInfo(depth = 1, multiPv = 2, cp = 20, pv = listOf("d2d4"))

        val result = accumulator.process(parsed)

        assertNull(result)
    }

    @Test
    fun `GIVEN all 3 pvs at same depth WHEN last pv processed THEN emits with all lines`() {
        val accumulator = AnalysisAccumulator(multiPvCount = 3)

        accumulator.process(parsedInfo(depth = 5, multiPv = 1, cp = 30, pv = listOf("e2e4")))
        accumulator.process(parsedInfo(depth = 5, multiPv = 2, cp = 20, pv = listOf("d2d4")))
        val result = accumulator.process(parsedInfo(depth = 5, multiPv = 3, cp = 10, pv = listOf("g1f3")))

        assertNotNull(result)
        assertEquals(3, result!!.lines.size)
        assertEquals(Evaluation.Centipawns(30), result.evaluation)
    }

    @Test
    fun `GIVEN deeper depth arrives for pv1 WHEN process THEN emits new result`() {
        val accumulator = AnalysisAccumulator(multiPvCount = 3)

        val first = accumulator.process(parsedInfo(depth = 1, multiPv = 1, cp = 18, pv = listOf("e2e4")))
        val second = accumulator.process(parsedInfo(depth = 2, multiPv = 1, cp = 30, pv = listOf("e2e4", "e7e5")))

        assertNotNull(first)
        assertNotNull(second)
        assertEquals(1, first!!.depth)
        assertEquals(2, second!!.depth)
        assertEquals(Evaluation.Centipawns(30), second.evaluation)
    }

    @Test
    fun `GIVEN lower depth than current WHEN process THEN ignores`() {
        val accumulator = AnalysisAccumulator(multiPvCount = 1)

        accumulator.process(parsedInfo(depth = 5, multiPv = 1, cp = 30, pv = listOf("e2e4")))
        val result = accumulator.process(parsedInfo(depth = 3, multiPv = 1, cp = 10, pv = listOf("d2d4")))

        assertNull(result)
    }

    @Test
    fun `GIVEN reset WHEN process new line THEN accumulates fresh`() {
        val accumulator = AnalysisAccumulator(multiPvCount = 1)

        accumulator.process(parsedInfo(depth = 10, multiPv = 1, cp = 50, pv = listOf("e2e4")))
        accumulator.reset()
        val result = accumulator.process(parsedInfo(depth = 1, multiPv = 1, cp = 5, pv = listOf("d2d4")))

        assertNotNull(result)
        assertEquals(1, result!!.depth)
        assertEquals(Evaluation.Centipawns(5), result.evaluation)
    }

    @Test
    fun `GIVEN single pv mode WHEN pv1 arrives THEN emits each time`() {
        val accumulator = AnalysisAccumulator(multiPvCount = 1)

        val r1 = accumulator.process(parsedInfo(depth = 1, multiPv = 1, cp = 10, pv = listOf("e2e4")))
        val r2 = accumulator.process(parsedInfo(depth = 2, multiPv = 1, cp = 20, pv = listOf("e2e4", "e7e5")))

        assertNotNull(r1)
        assertNotNull(r2)
    }

    @Test
    fun `GIVEN mate evaluation WHEN process THEN result contains mate`() {
        val accumulator = AnalysisAccumulator(multiPvCount = 1)
        val parsed = ParsedInfoLine(
            depth = 20,
            multiPv = 1,
            evaluation = Evaluation.Mate(3),
            principalVariation = listOf("d1h5", "f7f6", "d1f7"),
        )

        val result = accumulator.process(parsed)

        assertNotNull(result)
        assertEquals(Evaluation.Mate(3), result!!.evaluation)
    }

    private fun parsedInfo(
        depth: Int,
        multiPv: Int,
        cp: Int,
        pv: List<String>,
    ): ParsedInfoLine = ParsedInfoLine(
        depth = depth,
        multiPv = multiPv,
        evaluation = Evaluation.Centipawns(cp),
        principalVariation = pv,
    )
}
