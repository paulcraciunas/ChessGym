package com.paulcraciunas.chessgym.game.io

import com.paulcraciunas.chessgym.game.Game
import com.paulcraciunas.chessgym.game.GameState
import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.board.Board
import com.paulcraciunas.chessgym.game.board.File
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece
import com.paulcraciunas.chessgym.game.board.Rank
import com.paulcraciunas.chessgym.game.plies.CastlePly
import com.paulcraciunas.chessgym.game.plies.Ply
import com.paulcraciunas.chessgym.game.plies.StandardPly

/**
 * Forsyth–Edwards Notation serializer
 *
 * Forsyth–Edwards Notation (FEN) is a standard notation for describing a particular board position
 * of a chess game. The purpose of FEN is to provide all the necessary information to
 * restart a game from a particular position.
 *
 * Implementation note: it could be done with RegEx, but I suck at RegEx
 *
 * @see <a href="https://en.wikipedia.org/wiki/Forsyth–Edwards_Notation">FEN Wiki</a>
 **/
object FenSerializer : Serializer {

    override fun from(gameString: String): Game {
        val fenParts = gameString.fenParts()
        val rows = fenParts[0].rows()

        val board = Board().apply { loadPieces(rows) }
        val castling: Pair<Set<CastlePly.Type>, Set<CastlePly.Type>> = fenParts[2].loadCastling()
        val gameState = GameState(
            turn = fenParts[1].loadSide(),
            lastPly = fenParts[3].loadEnPassent(),
            whiteCastling = castling.first,
            blackCastling = castling.second,
            plieClock = fenParts[4].loadNumber(),
            moveIndex = fenParts[5].loadNumber()
        )

        return Game(board = board, state = gameState)
    }

    override fun of(game: Game): String = StringBuilder().apply {
        append(game.currentBoard().toFen()).append(" ")
        append(if (game.state().turn == Side.WHITE) 'w' else 'b').append(" ")
        append(game.state().castlingFen()).append(" ")
        append(game.allPlies().lastOrNull()?.toEnPassentFen() ?: MISSING).append(" ")
        append(game.allPlies().size).append(" ")
        append(game.state().moveIndex)
    }.toString()
}

private const val EXPECTED_FEN_PARTS = 6
private const val EXPECTED_ROWS = 8
private const val MISSING = "-"
private const val ROW_SPLIT = "/"
private val pieces = mapOf(
    'p' to Pair(Piece.Pawn, Side.BLACK),
    'r' to Pair(Piece.Rook, Side.BLACK),
    'n' to Pair(Piece.Knight, Side.BLACK),
    'b' to Pair(Piece.Bishop, Side.BLACK),
    'q' to Pair(Piece.Queen, Side.BLACK),
    'k' to Pair(Piece.King, Side.BLACK),
    'P' to Pair(Piece.Pawn, Side.WHITE),
    'R' to Pair(Piece.Rook, Side.WHITE),
    'N' to Pair(Piece.Knight, Side.WHITE),
    'B' to Pair(Piece.Bishop, Side.WHITE),
    'Q' to Pair(Piece.Queen, Side.WHITE),
    'K' to Pair(Piece.King, Side.WHITE),
)
private val turns = mapOf('w' to Side.WHITE, 'b' to Side.BLACK)

private fun String.fenParts(): List<String> {
    val result = split(" ")
    if (result.size != EXPECTED_FEN_PARTS)
        throw SerializeException("Expected $EXPECTED_FEN_PARTS FEN parts, found ${result.size}")
    return result
}

private fun String.rows(): List<String> {
    val result = split(ROW_SPLIT)
    if (result.size != EXPECTED_ROWS)
        throw SerializeException("Expected $EXPECTED_ROWS FEN rows, found ${result.size}")
    return result
}

private fun Board.loadPieces(rows: List<String>) {
    var rank = 7 // Rank.`8`; FEN ranks are from 8 to 1, hence the reverse order
    var file = 0 // File.a
    for (i in rows.indices) {
        if (file != 0) {
            throw SerializeException("Illegal row found: ${rows[i - 1]}")
        }
        for (char in rows[i]) {
            if (file == 8) {
                throw SerializeException("Illegal row found: ${rows[i]}")
            }
            if (pieces[char] != null) {
                add(
                    pieces[char]!!.first,
                    pieces[char]!!.second,
                    Locus(File.fromDec(file++), Rank.fromDec(rank))
                )
            } else {
                if (!char.isDigit()) {
                    throw SerializeException("Illegal character found: $char in ${rows[i]}")
                }
                file += char.digitToInt() // Skip the number of empty squares
            }
        }
        file %= 8
        rank--
    }
    if (rank != -1 || file != 0) { // We should be back where we started
        throw SerializeException("Illegal last row: ${rows.last()}")
    }
}

private fun Board.toFen(): String {
    var skips = 0
    var at: Locus
    fun StringBuilder.addSkips(): StringBuilder {
        if (skips != 0) {
            append(skips)
            skips = 0
        }
        return this
    }

    return StringBuilder().apply {
        Rank.entries.reversed().forEach { rank ->
            File.entries.forEach { file ->
                at = Locus(file, rank)
                at(at)?.let { piece ->
                    addSkips()
                    val symbol = piece.alg().takeIf { it.isNotBlank() } ?: "P"
                    append(if (has(piece, Side.WHITE, at)) symbol else symbol.lowercase())
                } ?: skips++
            }
            addSkips().append(ROW_SPLIT)
        }
        deleteAt(length - 1) // remove trailing ROW_SPLIT
    }.toString()
}

private fun String.loadSide(): Side =
    if (isEmpty()) throw SerializeException("Missing 'w'/'b' as side")
    else turns[get(0)] ?: throw SerializeException("Expecting 'w'/'b' as side, found ${get(0)}")

private fun String.loadNumber(): Int {
    if (isEmpty() || toIntOrNull() == null) throw SerializeException("Expecting number, found: $this")
    return toInt()
}

private fun String.loadEnPassent(): Ply? {
    if (this == MISSING) return null
    // This tells us the location a pawn moved OVER (e.g. e6)
    // To load the correct information as the "previous move", we have to add the from - to
    val loc = Locus.from(this) ?: throw SerializeException("Invalid en-passent location: $this")
    return when (loc.rank) {
        Rank.`3` -> StandardPly(
            turn = Side.WHITE,
            piece = Piece.Pawn,
            from = Locus(loc.file, Rank.`2`),
            to = Locus(loc.file, Rank.`4`)
        )

        Rank.`6` -> StandardPly(
            turn = Side.BLACK,
            piece = Piece.Pawn,
            from = Locus(loc.file, Rank.`7`),
            to = Locus(loc.file, Rank.`5`)
        )

        else -> throw SerializeException("Invalid en-passent rank: ${loc.rank}")
    }
}

private fun String.loadCastling(): Pair<Set<CastlePly.Type>, Set<CastlePly.Type>> {
    if (this == MISSING) return Pair(emptySet(), emptySet())

    val white = mutableSetOf<CastlePly.Type>()
    val black = mutableSetOf<CastlePly.Type>()
    for (char in this) {
        when (char) {
            'K' -> white.add(CastlePly.Type.KingSide)
            'Q' -> white.add(CastlePly.Type.QueenSide)
            'k' -> black.add(CastlePly.Type.KingSide)
            'q' -> black.add(CastlePly.Type.QueenSide)
            else -> throw SerializeException("Unexpected castling symbol found: $char")
        }
    }
    return Pair(white, black)
}

private fun GameState.castlingFen(): String = StringBuilder().apply {
    // Might be important that they're in the correct order
    if (whiteCastling.contains(CastlePly.Type.KingSide)) append('K')
    if (whiteCastling.contains(CastlePly.Type.QueenSide)) append('Q')
    if (blackCastling.contains(CastlePly.Type.KingSide)) append('k')
    if (blackCastling.contains(CastlePly.Type.QueenSide)) append('q')
}.toString()

private fun Ply?.toEnPassentFen(): String = when {
    this == null -> MISSING
    piece == Piece.Pawn && from.rank == Rank.`2` && to.rank == Rank.`4` -> "${from.file}3"
    piece == Piece.Pawn && from.rank == Rank.`7` && to.rank == Rank.`5` -> "${from.file}6"
    else -> MISSING
}
