package com.paulcraciunas.game.engine.impl.uci

import com.paulcraciunas.game.engine.api.Evaluation

internal data class ParsedInfoLine(
    val depth: Int,
    val multiPv: Int,
    val evaluation: Evaluation,
    val principalVariation: List<String>,
)

internal object InfoLineParser {
    private const val INFO_PREFIX = "info "
    private const val DEPTH_TOKEN = "depth"
    private const val MULTI_PV_TOKEN = "multipv"
    private const val SCORE_TOKEN = "score"
    private const val CP_TOKEN = "cp"
    private const val MATE_TOKEN = "mate"
    private const val PV_TOKEN = "pv"
    // Precompiled for performance
    private val SPACE_REGEX = Regex("\\s+")

    fun parse(line: String): ParsedInfoLine? {
        if (!line.startsWith(INFO_PREFIX)) return null

        // Split by any whitespace and remove empty strings
        val tokens = line.split(SPACE_REGEX).filter { it.isNotEmpty() }

        var depth: Int? = null
        var multiPv: Int? = null
        var evaluation: Evaluation? = null
        var pv: List<String>? = null

        var i = 1 // Skip the "info" prefix
        while (i < tokens.size) {
            when (tokens[i]) {
                DEPTH_TOKEN -> {
                    depth = tokens.getOrNull(i + 1)?.toIntOrNull()
                    i += 2
                }
                MULTI_PV_TOKEN -> {
                    multiPv = tokens.getOrNull(i + 1)?.toIntOrNull()
                    i += 2
                }
                SCORE_TOKEN -> {
                    val scoreType = tokens.getOrNull(i + 1)
                    val scoreValue = tokens.getOrNull(i + 2)?.toIntOrNull()
                    if (scoreValue != null) {
                        evaluation = when (scoreType) {
                            CP_TOKEN -> Evaluation.Centipawns(scoreValue)
                            MATE_TOKEN -> Evaluation.Mate(scoreValue)
                            else -> null
                        }
                    }
                    i += 3
                }
                PV_TOKEN -> {
                    // PV is always the last part of the info line in UCI
                    pv = tokens.subList(i + 1, tokens.size)
                    break
                }
                else -> i++ // Skip unknown tokens like 'nodes', 'nps', 'time', etc.
            }
        }

        return if (depth != null && multiPv != null && evaluation != null && !pv.isNullOrEmpty()) {
            ParsedInfoLine(depth, multiPv, evaluation, pv)
        } else {
            null
        }
    }
}
