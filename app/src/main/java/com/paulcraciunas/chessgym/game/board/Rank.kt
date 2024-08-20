package com.paulcraciunas.chessgym.game.board

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
}
