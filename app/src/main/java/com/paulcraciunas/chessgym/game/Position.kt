package com.paulcraciunas.chessgym.game

data class Position(val x: Int, val y: Int) {
    fun isValid(): Boolean = x in 0..7 && y in 0..7

    // TODO Paul: when serializing whole move notation, keep in mind the following
    // TODO Paul: add special chars at the end of move (! for check, + for capture, # for checkmate)
    // TODO Paul: disambiguate moves (e.g. Nbd2 - Knight from B to D2; R8e4 - Rook from 8 to e8)
    // TODO Paul: finally, castling: O-O for short, O-O-O for long; use Oh, not Zero!!
    // TODO Paul: this will probably be a method on the Move class
    fun toAlgebraic(): String {
        assert(isValid())

        return "${filesMap[x]}${rowsMap[y]}" //e.g. d4
    }

    fun next(delta: Delta): Position = Position(x = x + delta.dX, y = y + delta.dY)

    companion object {
        fun from(algebraic: String): Position {
            assert(algebraic.length == 2)
            assert(reverseFilesMap.contains(algebraic[0]))
            assert(reverseRowsMap.contains(algebraic[1]))
            return Position(
                reverseFilesMap[algebraic[0]]!!, reverseRowsMap[algebraic[1]]!!
            ) // safe to bang
        }

        private val filesMap = mapOf(
            0 to 'a',
            1 to 'b',
            2 to 'c',
            3 to 'd',
            4 to 'e',
            5 to 'f',
            6 to 'g',
            7 to 'h',
        )
        private val reverseFilesMap = filesMap.entries.associate { (k, v) -> v to k }
        private val rowsMap = mapOf(
            0 to '1',
            1 to '2',
            2 to '3',
            3 to '4',
            4 to '5',
            5 to '6',
            6 to '7',
            7 to '8',
        )
        private val reverseRowsMap = rowsMap.entries.associate { (k, v) -> v to k }
    }
}
