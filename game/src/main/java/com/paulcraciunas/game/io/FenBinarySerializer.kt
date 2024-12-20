package com.paulcraciunas.game.io

import com.paulcraciunas.game.io.binary.BinaryPuzzleReader
import com.paulcraciunas.game.io.binary.BinaryPuzzleWriter

/**
 * A serializer which takes a puzzle in FEN format and outputs a binary representation of the same
 * information, or vice-versa.
 *
 * The expected format is a CSV line with:
 *
 * 1. parameter = fen
 *
 * 2. parameter = list of expected moves separated by the space character '_'
 *
 * Notes on implementation: we use a bitboard to more efficiently store the data.
 *
 * A bitboard is a 8x8 matrix which looks like this:
 * ```
 * // We go from top left (a8) to bottom right (h1)
 * a8b8c8...h8 -> 011...0 // -> 1/0 depending on if we have a piece there
 * a7b7c7...h7 -> 110...1 // -> same as above
 * ...
 * a1b1c1...h1 -> 101...0 // -> same as above
 * // represented as a long string of bits in a Long, this becomes
 * a8b8c8...h8a7b7c7...h7...a1...h1
 * // this corresponds to the following bit positions in a 64-bit Long
 * 636261...56555453...48... 7... 0
 * ```
 *
 * More implementation details can be found inside the corresponding implementation classes
 * @see FenSerializer
 * @see BinaryPuzzleReader
 * @see BinaryPuzzleWriter
 */
internal class FenBinarySerializer(
    private val reader: BinaryPuzzleReader,
    private val writer: BinaryPuzzleWriter
) {
    fun toBinary(puzzleString: String): String = writer.toBinary(puzzleString)

    fun fromBinary(binary: String): String = reader.readFen(binary)
}
