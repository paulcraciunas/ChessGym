package com.paulcraciunas.game.engine.impl.uci

import com.paulcraciunas.game.engine.api.AnalysisResult
import com.paulcraciunas.game.engine.api.EngineLine
import com.paulcraciunas.game.engine.api.UciMoveParser

internal class AnalysisAccumulator(private val multiPvCount: Int) {
    private val latestByPv = mutableMapOf<Int, ParsedInfoLine>()
    private var lastEmittedDepth: Int = 0

    fun process(parsed: ParsedInfoLine): AnalysisResult? {
        val existing = latestByPv[parsed.multiPv]
        if (existing != null && existing.depth > parsed.depth) return null

        // If we jump to a new depth, clean up old lines
        if (parsed.depth > lastEmittedDepth) {
            latestByPv.values.removeIf { it.depth < parsed.depth - 1 }
        }

        latestByPv[parsed.multiPv] = parsed

        // Determine if we should emit.
        // We emit if we just got a new PV1 OR if we now have a full set for the current depth.
        val isNewPv1 = parsed.multiPv == 1 && parsed.depth >= lastEmittedDepth
        val hasFullSet = latestByPv.values.count { it.depth == parsed.depth } >= multiPvCount

        return if (isNewPv1 || hasFullSet) {
            lastEmittedDepth = parsed.depth
            buildResult(parsed.depth)
        } else {
            null
        }
    }

    private fun buildResult(currentDepth: Int): AnalysisResult {
        val lines = latestByPv.entries
            .sortedBy { it.key }
            .map { (pvIndex, info) ->
                EngineLine(
                    rank = pvIndex,
                    evaluation = info.evaluation,
                    moves = info.principalVariation.mapNotNull { UciMoveParser.parse(it) },
                )
            }

        return AnalysisResult(
            depth = latestByPv[1]?.depth ?: currentDepth,
            evaluation = latestByPv[1]?.evaluation ?: lines.first().evaluation,
            lines = lines,
        )
    }

    fun reset() {
        latestByPv.clear()
        lastEmittedDepth = 0
    }
}
