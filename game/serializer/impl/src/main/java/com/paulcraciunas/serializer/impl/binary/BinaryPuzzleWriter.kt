package com.paulcraciunas.serializer.impl.binary

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.game.logic.api.state.GameInfo
import com.paulcraciunas.serializer.api.PuzzleWriter
import com.paulcraciunas.serializer.api.Serializer
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.ArrayDeque

/**
 * Write a puzzle written in FEN format, in binary.
 *
 * The Writing and corresponding Reading are done according the the following rules:
 *
 * 1. First, we write a bitboard; a Long holding an 8x8 bits representing piece presence
 *
 * 2. The we write the individual pieces. Each piece is represented by 4 bits. There should be
 * as many pieces as bits in the bit-board loaded at step 1.
 *
 * 3. Write the plie clock - number of plies played, stored as 1 byte - max value is 127
 *
 * 4. Write the move index - number of moves played, stored as 1 byte - max value is 127
 *
 * 5. Write remaining metadata as 1 or 2 bytes. First byte contains the side to play, castling for
 * both sides and 111/000 in the 3 MSB if we have en-passent information or not.
 * The second byte contains the en-passent move. It may be omitted
 *
 * 6. Write the moves sequentially. 2 bytes per move until the end
 *
 * Note: It is important that this order corresponds to the one used by the Reader. Otherwise,
 * obviously, things will break. If you want to change something, run the tests to validate
 * nothing broke
 *
 * @see BinaryPuzzleReader
 * @see BinaryAdapter
 */
class BinaryPuzzleWriter(
    private val serializer: Serializer,
    private val adapter: BinaryAdapter,
) : PuzzleWriter {
    private var int: Int = 0 // So we don't keep allocating ints pointlessly

    override fun write(puzzleAndMoves: String): ByteArray {
        val parts = puzzleAndMoves.split(',')
        assert(parts.size == 2)

        return write(parts[0], parts[1])
    }

    override fun write(puzzle: String, moves: String): ByteArray = ByteArrayOutputStream()
        .writeFenBoard(puzzle)
        .writeMoves(moves.split(' '))
        .toByteArray()

    private fun ByteArrayOutputStream.writeFenBoard(boardString: String) = apply {
        val game = serializer.from(boardString)
        writeBoard(game.board).also { writePieces(it) }
        writeMetadata(game.info)
    }

    private fun OutputStream.writeBoard(board: IBoard): ArrayDeque<BinaryAdapter.SidedPiece> {
        val pieces = ArrayDeque<BinaryAdapter.SidedPiece>()
        var bitBoard: Long = 0
        var pos = 1L
        // read the board, starting with bottom right (h1) -> top left (a8)
        Rank.entries.forEach { rank ->
            File.entries.reversed().forEach { file ->
                board.at(file, rank)?.let {
                    bitBoard = bitBoard or pos
                    val side = if (board.has(it, Side.WHITE, Locus(file, rank))) {
                        Side.WHITE
                    } else {
                        Side.BLACK
                    }
                    // We add first so that we process them in reverse order
                    pieces.addFirst(BinaryAdapter.SidedPiece(piece = it, side = side))
                }
                pos = pos shl 1
            }
        }
        write(bitBoard.toByteArray())
        return pieces
    }

    private fun Long.toByteArray(): ByteArray {
        var l = this
        val result = ByteArray(8)
        for (i in 7 downTo 0) {
            result[i] = (l and 0xFFL).toByte()
            l = l shr 8
        }
        return result
    }

    private fun OutputStream.writePieces(pieces: ArrayDeque<BinaryAdapter.SidedPiece>) {
        int = 0
        var first: BinaryAdapter.SidedPiece?
        var second: BinaryAdapter.SidedPiece?
        while (pieces.isNotEmpty()) {
            first = pieces.poll()
            second = pieces.poll()
            int = adapter.toBinary(first)
            if (second == null) { // if we have an odd number of pieces
                write(int)
                break
            }
            int = (int shl 4) or adapter.toBinary(second)
            write(int)
        }
    }

    // Order here matters. Ye be warned
    private fun OutputStream.writeMetadata(state: GameInfo) {
        write(state.plieClock) // don't care if it's above 127
        write(state.moveIndex) // don't care if it's above 127
        int = 0
        int = state.turn.code
        int = int or (adapter.toBinary( // add castling into the next 4 bits
            white = state.whiteCastling,
            black = state.blackCastling
        ) shl 1)
        // if last ply was a pawn move, add possible en-passent
        // we only care about the file. The rank can be disambiguated depending on the side playing
        state.lastPly?.let {
            int = int or 0b11100000
            write(int)
            if (it.piece != Piece.Pawn) return
            int = when (it.to.rank) {
                Rank.`5` -> adapter.toBinary(Locus(file = it.from.file, rank = Rank.`6`))
                Rank.`4` -> adapter.toBinary(Locus(file = it.from.file, rank = Rank.`3`))
                else -> return
            }
            write(int)
        } ?: write(int) // write the 5 bits we already have, side + castling
    }

    private fun ByteArrayOutputStream.writeMoves(moves: List<String>) = apply {
        moves.forEach {
            // Each move takes 2 bytes
            int = adapter.toBinary(it)
            write(int ushr 8)
            write(int)
        }
    }
}