package com.paulcraciunas.game.board

import com.paulcraciunas.game.Side

data class Locus(val file: File, val rank: Rank) {
    fun top(): Locus? = rank.next()?.let { Locus(file = file, rank = it) }
    fun down(): Locus? = rank.prev()?.let { Locus(file = file, rank = it) }
    fun left(): Locus? = file.prev()?.let { Locus(file = it, rank = rank) }
    fun right(): Locus? = file.next()?.let { Locus(file = it, rank = rank) }

    fun topLeft(): Locus? =
        file.prev()?.let { f -> rank.next()?.let { r -> Locus(file = f, rank = r) } }

    fun topRight(): Locus? =
        file.next()?.let { f -> rank.next()?.let { r -> Locus(file = f, rank = r) } }

    fun downLeft(): Locus? =
        file.prev()?.let { f -> rank.prev()?.let { r -> Locus(file = f, rank = r) } }

    fun downRight(): Locus? =
        file.next()?.let { f -> rank.prev()?.let { r -> Locus(file = f, rank = r) } }

    fun side(): Side = if ((file.dec() + rank.dec()) % 2 == 0) Side.BLACK else Side.WHITE

    override fun toString(): String = "${file.name}${rank.name}" //e.g. d4

    companion object {
        fun all(action: (Locus) -> Unit) {
            File.entries.forEach { file ->
                Rank.entries.forEach { rank ->
                    action(Locus(file = file, rank = rank))
                }
            }
        }

        fun from(algebraic: String): Locus? {
            if (algebraic.length != 2) return null
            val file = algebraic[0].toFile()
            val rank = algebraic[1].toRank()
            return if (file != null && rank != null) {
                Locus(file, rank)
            } else null
        }
    }
}
