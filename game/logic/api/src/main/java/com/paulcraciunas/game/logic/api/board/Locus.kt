package com.paulcraciunas.game.logic.api.board

import com.paulcraciunas.game.logic.api.Side

@Suppress("EnumEntryName")
enum class Locus(val file: File, val rank: Rank, val side: Side) {
    // Rank 1
    a1(File.a, Rank.`1`, Side.BLACK), b1(File.b, Rank.`1`, Side.WHITE), c1(File.c, Rank.`1`, Side.BLACK), d1(File.d, Rank.`1`, Side.WHITE),
    e1(File.e, Rank.`1`, Side.BLACK), f1(File.f, Rank.`1`, Side.WHITE), g1(File.g, Rank.`1`, Side.BLACK), h1(File.h, Rank.`1`, Side.WHITE),

    // Rank 2
    a2(File.a, Rank.`2`, Side.WHITE), b2(File.b, Rank.`2`, Side.BLACK), c2(File.c, Rank.`2`, Side.WHITE), d2(File.d, Rank.`2`, Side.BLACK),
    e2(File.e, Rank.`2`, Side.WHITE), f2(File.f, Rank.`2`, Side.BLACK), g2(File.g, Rank.`2`, Side.WHITE), h2(File.h, Rank.`2`, Side.BLACK),

    // Rank 3
    a3(File.a, Rank.`3`, Side.BLACK), b3(File.b, Rank.`3`, Side.WHITE), c3(File.c, Rank.`3`, Side.BLACK), d3(File.d, Rank.`3`, Side.WHITE),
    e3(File.e, Rank.`3`, Side.BLACK), f3(File.f, Rank.`3`, Side.WHITE), g3(File.g, Rank.`3`, Side.BLACK), h3(File.h, Rank.`3`, Side.WHITE),

    // Rank 4
    a4(File.a, Rank.`4`, Side.WHITE), b4(File.b, Rank.`4`, Side.BLACK), c4(File.c, Rank.`4`, Side.WHITE), d4(File.d, Rank.`4`, Side.BLACK),
    e4(File.e, Rank.`4`, Side.WHITE), f4(File.f, Rank.`4`, Side.BLACK), g4(File.g, Rank.`4`, Side.WHITE), h4(File.h, Rank.`4`, Side.BLACK),

    // Rank 5
    a5(File.a, Rank.`5`, Side.BLACK), b5(File.b, Rank.`5`, Side.WHITE), c5(File.c, Rank.`5`, Side.BLACK), d5(File.d, Rank.`5`, Side.WHITE),
    e5(File.e, Rank.`5`, Side.BLACK), f5(File.f, Rank.`5`, Side.WHITE), g5(File.g, Rank.`5`, Side.BLACK), h5(File.h, Rank.`5`, Side.WHITE),

    // Rank 6
    a6(File.a, Rank.`6`, Side.WHITE), b6(File.b, Rank.`6`, Side.BLACK), c6(File.c, Rank.`6`, Side.WHITE), d6(File.d, Rank.`6`, Side.BLACK),
    e6(File.e, Rank.`6`, Side.WHITE), f6(File.f, Rank.`6`, Side.BLACK), g6(File.g, Rank.`6`, Side.WHITE), h6(File.h, Rank.`6`, Side.BLACK),

    // Rank 7
    a7(File.a, Rank.`7`, Side.BLACK), b7(File.b, Rank.`7`, Side.WHITE), c7(File.c, Rank.`7`, Side.BLACK), d7(File.d, Rank.`7`, Side.WHITE),
    e7(File.e, Rank.`7`, Side.BLACK), f7(File.f, Rank.`7`, Side.WHITE), g7(File.g, Rank.`7`, Side.BLACK), h7(File.h, Rank.`7`, Side.WHITE),

    // Rank 8
    a8(File.a, Rank.`8`, Side.WHITE), b8(File.b, Rank.`8`, Side.BLACK), c8(File.c, Rank.`8`, Side.WHITE), d8(File.d, Rank.`8`, Side.BLACK),
    e8(File.e, Rank.`8`, Side.WHITE), f8(File.f, Rank.`8`, Side.BLACK), g8(File.g, Rank.`8`, Side.WHITE), h8(File.h, Rank.`8`, Side.BLACK);

    fun top(): Locus? = lookup(file.ordinal, rank.ordinal + 1)
    fun down(): Locus? = lookup(file.ordinal, rank.ordinal - 1)
    fun left(): Locus? = lookup(file.ordinal - 1, rank.ordinal)
    fun right(): Locus? = lookup(file.ordinal + 1, rank.ordinal)

    // Diagonal combos for Bishops and Queens
    fun topLeft(): Locus? = lookup(file.ordinal - 1, rank.ordinal + 1)
    fun topRight(): Locus? = lookup(file.ordinal + 1, rank.ordinal + 1)
    fun downLeft(): Locus? = lookup(file.ordinal - 1, rank.ordinal - 1)
    fun downRight(): Locus? = lookup(file.ordinal + 1, rank.ordinal - 1)

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
