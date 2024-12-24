package com.paulcraciunas.game.io.binary

import com.paulcraciunas.game.logic.Side
import com.paulcraciunas.game.logic.board.File
import com.paulcraciunas.game.logic.board.Locus
import com.paulcraciunas.game.logic.board.Piece
import com.paulcraciunas.game.logic.board.Rank
import com.paulcraciunas.game.logic.plies.CastlePly

/**
 * Adapter for writing and reading game information into/from binary.
 *
 * Implementation notes:
 *
 * 1. A piece & the side it belongs to is encoded onto 4 bits: LSB is side, piece is the next 3 bits
 *
 * 2. A Location is encoded into 6 bits - which later are saved as a whole byte, for simplicity
 * The 3 least significant bits are the file. The next 3 bits are the rank.
 *
 * 3. Castling is encoded into 4 bits - white castling into the 2 LSB, black into the next 2 bits
 *
 * 4. A move is encoded into 2 bytes. The first byte has the "from" location in its 6 LSB.
 * The next byte has the "to" location in its 6 LSB
 *
 *
 * @see BinaryPuzzleReader
 * @see BinaryPuzzleWriter
 */
internal class BinaryAdapter {
    fun toBinary(data: SidedPiece): Int {
        val typeBits = data.piece.code and 0b111 // 3 bits for type
        val sideBit = data.side.code and 0b1 // 1 bit for side
        return (typeBits shl 1) or sideBit
    }

    fun toBinary(loc: Locus): Int {
        val rankBits = (loc.rank.dec() and 0b111) shl 3 // 3 bits for rank
        val fileBits = loc.file.dec() and 0b111 // 3 bits for file
        return rankBits or fileBits // Combine into a single byte
    }

    fun toBinary(
        white: Set<CastlePly.Type>,
        black: Set<CastlePly.Type>,
    ): Int {
        var result = 0
        if (white.contains(CastlePly.Type.KingSide)) result = result or 0b1
        if (white.contains(CastlePly.Type.QueenSide)) result = result or 0b10
        if (black.contains(CastlePly.Type.KingSide)) result = result or 0b100
        if (black.contains(CastlePly.Type.QueenSide)) result = result or 0b1000
        return result
    }

    fun toBinary(move: String): Int {
        // Need a "from" and a "dest" both in algebraic notation
        val from = Locus.from(move.substring(0, 2)) ?: error("Invalid start for move $move")
        val to = Locus.from(move.substring(2, 4)) ?: error("Invalid destination for move $move")

        return if (move.length == 4) {
            (toBinary(from) shl 8) or toBinary(to)
        } else { // if we have a promotion, set the seventh bit for "to" as 1
            assert(move.length == 5)
            assert(promotionBits.contains(move[4]))
            // we mark a bit in the "from" to say there's a promotion
            // we set the 2 MSB in the "to" to the type of piece
            ((toBinary(from) or PROMOTION_MASK) shl 8) or toBinary(to) or promotionBits[move[4]]!!
        }
    }

    fun toPiece(data: Int): SidedPiece {
        val typeBits = (data shr 1) and 0b111 // Extract type (3 bits)
        val sideBit = data and 0b1 // Extract side (1 bit)
        return SidedPiece(Piece.fromCode(typeBits), Side.fromCode(sideBit))
    }

    fun toLocation(data: Int): Locus {
        val rankBits = (data shr 3) and 0b111 // Extract rank (3 bits)
        val fileBits = data and 0b111 // Extract file (3 bits)
        return Locus(File.fromDec(fileBits), Rank.fromDec(rankBits))
    }

    fun toCastling(data: Int): Pair<Set<CastlePly.Type>, Set<CastlePly.Type>> {
        val white = HashSet<CastlePly.Type>()
        val black = HashSet<CastlePly.Type>()

        if ((data and 0b1) != 0) white.add(CastlePly.Type.KingSide)
        if ((data and 0b10) != 0) white.add(CastlePly.Type.QueenSide)
        if ((data and 0b100) != 0) black.add(CastlePly.Type.KingSide)
        if ((data and 0b1000) != 0) black.add(CastlePly.Type.QueenSide)
        return white to black
    }

    fun toMove(move: Int): String {
        val from = toLocation((move shr 8) and 0x3F) // 6 bits for from
        val hasPromotion = ((move shr 8) and PROMOTION_MASK) != 0
        val to = toLocation(move and 0x3F) // 6 bits for to
        val pieceCode = move and 0b1100_0000
        val piece = if (hasPromotion) {
            promotionBits.entries.first { it.value == pieceCode }.key
        } else ""

        return "$from$to$piece"
    }

    internal data class SidedPiece(val piece: Piece, val side: Side)

    companion object {
        private const val PROMOTION_MASK = 0b0100_0000
        private val promotionBits = mapOf(
            Piece.Queen.alg().lowercase().first() to 0b1100_0000,
            Piece.Knight.alg().lowercase().first() to 0b1000_0000,
            Piece.Rook.alg().lowercase().first() to 0b0100_0000,
            Piece.Bishop.alg().lowercase().first() to 0b0000_0000
        )
    }
}
