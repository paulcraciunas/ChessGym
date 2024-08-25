package com.paulcraciunas.chessgym.game.board

import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.Side.WHITE

@Suppress("EnumEntryName")
enum class Rank {
    `1`,
    `2`,
    `3`,
    `4`,
    `5`,
    `6`,
    `7`,
    `8`;

    fun next(): Rank? = if (this != `8`) entries[this.ordinal + 1] else null
    fun prev(): Rank? = if (this != `1`) entries[this.ordinal - 1] else null
    fun dec(): Int = ordinal

    companion object {
        fun fromDec(dec: Int): Rank =
            if (dec in 0..7) Rank.entries[dec]
            else throw IllegalArgumentException("Wrong decimal value. Expecting [0 - 7]")
    }
}

fun Char.toRank(): Rank? = if (this in "12345678") {
    Rank.valueOf(toString())
} else null
fun promotion(side: Side): Rank = if (side == WHITE) Rank.`8` else Rank.`1`
fun pawnStart(side: Side): Rank = if (side == WHITE) Rank.`2` else Rank.`7`
