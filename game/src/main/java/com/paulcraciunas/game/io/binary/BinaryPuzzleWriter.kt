package com.paulcraciunas.game.io.binary

import com.paulcraciunas.game.Game
import com.paulcraciunas.game.Side
import com.paulcraciunas.game.board.File
import com.paulcraciunas.game.board.Locus
import com.paulcraciunas.game.board.Piece
import com.paulcraciunas.game.board.Rank
import com.paulcraciunas.game.io.FenSerializer
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
 * 5. Write remaining metadata as 1 or 2 bytes. First byte contains the side to play & castling
 * state for both sides. The second byte contains en-passent move availability. It may be omitted
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
internal class BinaryPuzzleWriter(
    private val fen: FenSerializer,
    private val adapter: BinaryAdapter,
) {
    private var int: Int = 0 // So we don't keep allocating ints pointlessly

    fun toBinary(puzzleString: String): String {
        val parts = puzzleString.split(',')
        assert(parts.size == 2)

        return with(StringBuilder()) {
            writeFenBoard(parts[0]) // first part is FEN board
            writeMoves(parts[1].split(' ')) // second part is moves
        }.toString()
    }

    private fun StringBuilder.writeFenBoard(boardString: String) {
        val game = fen.from(boardString)
        writeBoard(game).also { writePieces(it) }
        writeMetadata(game)
    }

    private fun StringBuilder.writeBoard(game: Game): ArrayDeque<BinaryAdapter.SidedPiece> {
        val pieces = ArrayDeque<BinaryAdapter.SidedPiece>()
        var bitBoard: Long = 0
        var pos = 1L
        // read the board, starting with bottom right (h1) -> top left (a8)
        Rank.entries.forEach { rank ->
            File.entries.reversed().forEach { file ->
                game.board().at(file, rank)?.let {
                    bitBoard = bitBoard or pos
                    val side = if (game.board().has(it, Side.WHITE, Locus(file, rank))) {
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
        append(bitBoard)
        return pieces
    }

    private fun StringBuilder.writePieces(pieces: ArrayDeque<BinaryAdapter.SidedPiece>) {
        int = 0
        var first: BinaryAdapter.SidedPiece? = null
        var second: BinaryAdapter.SidedPiece?
        while (pieces.isNotEmpty()) {
            first = pieces.poll()
            second = pieces.poll()
            if (second == null) {
                break
            }
            int = int or adapter.toBinary(first) shl 4
            int = int or adapter.toBinary(second)
            append(int.toByte())
        }
        if (first != null) { // if we have an odd number of pieces
            append(adapter.toBinary(first).toByte())
        }
    }

    // Order here matters. Ye be warned
    private fun StringBuilder.writeMetadata(game: Game) {
        append(game.state().plieClock.toByte()) // don't care if it's above 127
        append(game.state().moveIndex.toByte()) // don't care if it's above 127
        int = 0
        int = game.state().turn.code
        int = int or (adapter.toBinary( // add castling into the most significant 4 bits
            white = game.state().whiteCastling,
            black = game.state().blackCastling
        ) shl 4)
        append(int.toByte())
        // if last ply was a pawn move, add possible en-passent
        // we only care about the file. The rank can be disambiguated depending on the side playing
        game.state().lastPly?.let {
            if (it.piece != Piece.Pawn) return
            int = when (it.to.rank) {
                Rank.`5` -> adapter.toBinary(Locus(file = it.from.file, rank = Rank.`6`))
                Rank.`4` -> adapter.toBinary(Locus(file = it.from.file, rank = Rank.`3`))
                else -> return
            }
            append(int.toByte())
        }
    }

    private fun StringBuilder.writeMoves(moves: List<String>) {
        moves.forEach {
            // Each move takes 2 bytes
            append(adapter.toBinary(it).toShort())
        }
    }
}