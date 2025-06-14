package com.paulcraciunas.serializer.impl.binary

import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.logic.di.Builder
import com.paulcraciunas.logic.di.GameFactory
import com.paulcraciunas.serializer.api.PuzzleReader
import com.paulcraciunas.serializer.impl.withEnPassent
import java.util.ArrayDeque
import java.util.Queue
import javax.inject.Inject

/**
 * Read a puzzle written in binary, according to FEN format
 *
 * The Reading and corresponding Writing are done according the the following rules:
 *
 * 1. First, we read the bitboard; a Long holding an 8x8 bits representing piece presence
 *
 * 2. The we read the individual pieces. Each piece is represented by 4 bits. There should be
 * as many pieces as bits in the bit-board loaded at step 1.
 *
 * 3. Read the plie clock - number of plies played, stored as 1 byte - max value is 127
 *
 * 4. Read the move index - number of moves played, stored as 1 byte - max value is 127
 *
 * 5. Read remaining metadata as 1 or 2 bytes. First byte contains the side to play & castling
 * state for both sides. The second byte contains en-passent move availability. It may be omitted
 *
 * 6. Read the moves sequentially. 2 bytes per move until the end
 *
 * Note: It is important that this order corresponds to the one used by the Writer. Otherwise,
 * obviously, things will break. If you want to change something, run the tests to validate
 * nothing broke
 *
 * @see BinaryPuzzleWriter
 * @see BinaryAdapter
 */
internal class BinaryPuzzleReader @Inject constructor(
    private val gameFactory: GameFactory,
    private val adapter: BinaryAdapter,
) : PuzzleReader {
    // These are here so we don't keep allocating these vars pointlessly
    // This is mostly useful when we generate & test all puzzles at once
    private var int: Int = 0
    private var long: Long = 0L

    override fun readPuzzle(rating: Int, bytes: ByteArray): Puzzle {
        int = 0
        // Order here matters. Ye be warned
        return gameFactory.builder()
            .withBoard(bytes)
            .withRating(rating)
            .withPlieClock(bytes[int++].toInt())
            .withMoveIndex(bytes[int++].toInt())
            .withMetadata(bytes)
            .withMoves(bytes.loadMoves())
            .buildPuzzle()
    }

    private fun Builder.withBoard(bytes: ByteArray) = apply {
        val bitBoard = bytes.loadBitBoard()
        val pieces = bytes.loadPieces(bitBoard)
        long = 1L shl 63
        Rank.entries.reversed().forEach { rank ->
            File.entries.forEach { file ->
                if (bitBoard and long != 0L) {
                    val piece = pieces.poll()
                    withPiece(piece!!.piece, piece.side, Locus(file, rank))
                }
                long = long ushr 1
            }
        }
        assert(long == 0L)
    }

    private fun Builder.withMetadata(bytes: ByteArray): Builder {
        val data = bytes[int++].toInt()
        val side = Side.fromCode(data and 1)
        val (white, black) = adapter.toCastling(data shr 1)
        val ply = if (data and 0b11100000 != 0) { // check if we have en-passent information
            adapter.toLocation(bytes[int++].toInt()).toString()
        } else null
        return withTurn(side)
            .withEnPassent(ply)
            .withWhiteCastling(white)
            .withBlackCastling(black)
    }

    private fun ByteArray.loadPieces(bitBoard: Long): ArrayDeque<BinaryAdapter.SidedPiece> {
        val pieces = ArrayDeque<BinaryAdapter.SidedPiece>()
        val pieceCount = bitBoard.countOneBits()
        assert(pieceCount >= 2) // we should have at least 2 pieces; otherwise, it's not a puzzle
        var piecesBinary: Int
        for (i in 1..pieceCount / 2) {
            piecesBinary = get(int++).toInt()
            pieces.add(adapter.toPiece(piecesBinary shr 4))
            pieces.add(adapter.toPiece(piecesBinary))
        }
        if (pieceCount % 2 == 1) { // If we have an odd number of pieces
            pieces.add(adapter.toPiece(get(int++).toInt()))
        }
        return pieces
    }

    private fun ByteArray.loadBitBoard(): Long {
        val first = ((this[int++].toUInt() and 0xFFu) shl 24) or
                ((this[int++].toUInt() and 0xFFu) shl 16) or
                ((this[int++].toUInt() and 0xFFu) shl 8) or
                (this[int++].toUInt() and 0xFFu)
        val second = ((this[int++].toUInt() and 0xFFu) shl 24) or
                ((this[int++].toUInt() and 0xFFu) shl 16) or
                ((this[int++].toUInt() and 0xFFu) shl 8) or
                (this[int++].toUInt() and 0xFFu)

        return first.toLong() shl 32 or second.toLong()
    }

    private fun ByteArray.loadMoves(): Queue<String> {
        val movesList = ArrayDeque<String>()
        var move: Int
        while (int <= size - 2) { // Each move takes 2 bytes
            move = (((get(int++).toUInt() and 0xFFu) shl 8) or
                    (get(int++).toUInt() and 0xFFu)).toInt()
            movesList.add(adapter.toMove(move))
        }
        return movesList
    }
}
