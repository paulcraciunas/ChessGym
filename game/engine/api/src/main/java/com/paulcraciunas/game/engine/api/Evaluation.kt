package com.paulcraciunas.game.engine.api

sealed class Evaluation {
    /**
     * Converts the evaluation to a fraction between -1 and 1.
     */
    abstract fun toFraction(): Float
    abstract fun format(): String

    data class Centipawns(val value: Int) : Evaluation() {
        override fun toFraction(): Float {
            val pawns = value / 100f
            val normalized = (pawns / MAX_EVAL_PAWNS).coerceIn(-1f, 1f)
            return 0.5f + normalized * 0.5f
        }

        override fun format(): String {
            val pawns = value / 100f
            return if (pawns >= 0) "+%.1f".format(pawns)
            else "%.1f".format(pawns)
        }
    }

    data class Mate(val movesToMate: Int) : Evaluation() {
        override fun toFraction(): Float = if (movesToMate > 0) MAX_FRACTION else MIN_FRACTION
        override fun format(): String = "M${movesToMate}"
    }

    companion object {
        private const val MAX_EVAL_PAWNS = 10f
        const val MIN_FRACTION = 0.03f
        const val MAX_FRACTION = 0.97f
    }
}
