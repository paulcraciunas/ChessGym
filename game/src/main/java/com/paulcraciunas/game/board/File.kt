package com.paulcraciunas.game.board

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

    companion object {
        fun fromDec(dec: Int): File =
            if (dec in 0..7) File.entries[dec]
            else throw IllegalArgumentException("Wrong decimal value. Expecting [0 - 7]")
    }
}

fun Char.toFile(): File? = if (this in "abcdefgh") {
    File.valueOf(toString())
} else null
