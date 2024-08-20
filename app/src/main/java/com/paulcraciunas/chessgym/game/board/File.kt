package com.paulcraciunas.chessgym.game.board

@Suppress("EnumEntryName")
enum class File {
    a,
    b,
    c,
    d,
    e,
    f,
    g,
    h;

    fun next(): File? = if (this != h) entries[this.ordinal + 1] else null
    fun prev(): File? = if (this != a) entries[this.ordinal - 1] else null
    fun dec(): Int = ordinal
}
