package com.paulcraciunas.game.logic.api.board

import com.paulcraciunas.game.logic.api.Side

@Suppress("EnumEntryName")
enum class Locus(val file: File, val rank: Rank) {
    // Rank 1
    a1(File.a, Rank.`1`), b1(File.b, Rank.`1`), c1(File.c, Rank.`1`), d1(File.d, Rank.`1`),
    e1(File.e, Rank.`1`), f1(File.f, Rank.`1`), g1(File.g, Rank.`1`), h1(File.h, Rank.`1`),

    // Rank 2
    a2(File.a, Rank.`2`), b2(File.b, Rank.`2`), c2(File.c, Rank.`2`), d2(File.d, Rank.`2`),
    e2(File.e, Rank.`2`), f2(File.f, Rank.`2`), g2(File.g, Rank.`2`), h2(File.h, Rank.`2`),

    // Rank 3
    a3(File.a, Rank.`3`), b3(File.b, Rank.`3`), c3(File.c, Rank.`3`), d3(File.d, Rank.`3`),
    e3(File.e, Rank.`3`), f3(File.f, Rank.`3`), g3(File.g, Rank.`3`), h3(File.h, Rank.`3`),

    // Rank 4
    a4(File.a, Rank.`4`), b4(File.b, Rank.`4`), c4(File.c, Rank.`4`), d4(File.d, Rank.`4`),
    e4(File.e, Rank.`4`), f4(File.f, Rank.`4`), g4(File.g, Rank.`4`), h4(File.h, Rank.`4`),

    // Rank 5
    a5(File.a, Rank.`5`), b5(File.b, Rank.`5`), c5(File.c, Rank.`5`), d5(File.d, Rank.`5`),
    e5(File.e, Rank.`5`), f5(File.f, Rank.`5`), g5(File.g, Rank.`5`), h5(File.h, Rank.`5`),

    // Rank 6
    a6(File.a, Rank.`6`), b6(File.b, Rank.`6`), c6(File.c, Rank.`6`), d6(File.d, Rank.`6`),
    e6(File.e, Rank.`6`), f6(File.f, Rank.`6`), g6(File.g, Rank.`6`), h6(File.h, Rank.`6`),

    // Rank 7
    a7(File.a, Rank.`7`), b7(File.b, Rank.`7`), c7(File.c, Rank.`7`), d7(File.d, Rank.`7`),
    e7(File.e, Rank.`7`), f7(File.f, Rank.`7`), g7(File.g, Rank.`7`), h7(File.h, Rank.`7`),

    // Rank 8
    a8(File.a, Rank.`8`), b8(File.b, Rank.`8`), c8(File.c, Rank.`8`), d8(File.d, Rank.`8`),
    e8(File.e, Rank.`8`), f8(File.f, Rank.`8`), g8(File.g, Rank.`8`), h8(File.h, Rank.`8`);

    fun top(): Locus? = lookup(file.ordinal, rank.ordinal + 1)
    fun down(): Locus? = lookup(file.ordinal, rank.ordinal - 1)
    fun left(): Locus? = lookup(file.ordinal - 1, rank.ordinal)
    fun right(): Locus? = lookup(file.ordinal + 1, rank.ordinal)

    // Diagonal combos for Bishops and Queens
    fun topLeft(): Locus? = lookup(file.ordinal - 1, rank.ordinal + 1)
    fun topRight(): Locus? = lookup(file.ordinal + 1, rank.ordinal + 1)
    fun downLeft(): Locus? = lookup(file.ordinal - 1, rank.ordinal - 1)
    fun downRight(): Locus? = lookup(file.ordinal + 1, rank.ordinal - 1)

    fun side(): Side = if ((file.dec() + rank.dec()) % 2 == 0) Side.BLACK else Side.WHITE

    companion object {
        // Pre-calculated matrix for fast, boundary-safe directional lookups
        private val grid: Array<Array<Locus>> = Array(File.entries.size) { f ->
            Array(Rank.entries.size) { r ->
                entries.first { it.file.ordinal == f && it.rank.ordinal == r }
            }
        }

        private fun lookup(fileIndex: Int, rankIndex: Int): Locus? {
            if (fileIndex !in File.entries.indices || rankIndex !in Rank.entries.indices) return null
            return grid[fileIndex][rankIndex]
        }

        fun all(action: (Locus) -> Unit) = Locus.entries.forEach(action)
        fun from(file: File, rank: Rank): Locus = grid[file.ordinal][rank.ordinal]
        fun from(algebraic: String): Locus? {
            if (algebraic.length != 2) return null
            val file = algebraic[0].toFile()
            val rank = algebraic[1].toRank()
            return if (file != null && rank != null) {
                from(file, rank)
            } else null
        }
    }
}
