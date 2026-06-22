package com.paulcraciunas.serializer.impl

import com.paulcraciunas.game.logic.api.CastleType
import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.GameFactory
import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.game.logic.api.board.toFile
import com.paulcraciunas.game.logic.api.board.toRank
import com.paulcraciunas.game.logic.api.state.MetaData
import com.paulcraciunas.serializer.api.SerializeException
import com.paulcraciunas.serializer.api.Serializer

/**
 * Portable Game Notation serializer
 *
 * Portable Game Notation (PGN) is a standard plain text format for recording chess games
 * (both the moves and related data), which can be read by humans and is also supported by most
 * chess software.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Portable_Game_Notation">PGN Wiki</a>
 **/
class PgnSerializer(
    private val gameFactory: GameFactory,
) : Serializer {
    override fun from(gameString: String): Game {
        val ending = gameString.findEnding()
        val stripped = if (ending != null) gameString.stripEnding(ending) else gameString
        val lines = stripped.lines().filter { it.isNotBlank() }

        var idx = 0
        val headers = mutableMapOf<MetaData.Header, String>()
        for (i in lines.indices) {
            val parsed = lines[i].parseHeader()
            if (parsed != null) {
                MetaData.Header.of(parsed.first)
                    ?.let { header -> headers[header] = parsed.second }
                idx = i + 1
            } else break
        }
        val remaining = lines.drop(idx).joinToString(separator = " ")
        return gameFactory.builder().withMetadata(MetaData(headers)).withDefaultBoard().buildGame()
            .apply {
                start()
                tokenizeMoves(remaining) { token -> loadPly(token) }
                if (plies().isNotEmpty() && ending != null) {
                    if (ending == DRAW_RESULT) draw() else resign()
                }
            }
    }

    override fun of(puzzle: Puzzle): String =
        throw UnsupportedOperationException("PGN format does not support puzzle serialization")

    override fun of(game: Game): String = StringBuilder().apply {
        MetaData.Header.entries.forEach { header ->
            game.metadata.data(header)?.let { value ->
                append("[$header \"$value\"]\n")
            }
        }
        append("\n")
        val plies = game.history
        for (i in plies.indices step 2) {
            append("${i / 2 + 1}.")
            append(plies[i].algebraic()).append(" ")
            if (i + 1 < plies.size) {
                append(plies[i + 1].algebraic()).append(" ")
            }
        }
        (game.state as? Game.GameState.Finished)?.let {
            append(" ").append(it.result.algebraic(game.info.turn))
        }
        append("\n")
    }.toString()
}

private const val DRAW_RESULT = "1/2-1/2"
private const val WHITE_WINS = "1-0"
private const val BLACK_WINS = "0-1"

private fun String.findEnding(): String? {
    var i = length - 1
    // Skip trailing line terminator (matching $ anchor semantics)
    if (i >= 0 && this[i] == '\n') i--
    if (i >= 0 && this[i] == '\r') i--
    if (i < 2) return null
    if (this[i] == '0' && this[i - 1] == '-' && this[i - 2] == '1') return WHITE_WINS
    if (this[i] == '1' && this[i - 1] == '-' && this[i - 2] == '0') return BLACK_WINS
    if (i >= 6 &&
        this[i] == '2' && this[i - 1] == '/' && this[i - 2] == '1' &&
        this[i - 3] == '-' &&
        this[i - 4] == '2' && this[i - 5] == '/' && this[i - 6] == '1'
    ) return DRAW_RESULT
    return null
}

private fun String.stripEnding(ending: String): String {
    var i = length - 1
    if (i >= 0 && this[i] == '\n') i--
    if (i >= 0 && this[i] == '\r') i--
    return substring(0, i + 1 - ending.length)
}

// --- Header parsing (replaces headerRegex) ---

private fun String.parseHeader(): Pair<String, String>? {
    if (isEmpty() || this[0] != '[') return null
    if (length < 5 || this[length - 1] != ']' || this[length - 2] != '"') return null

    var i = 1
    while (i < length && this[i].isAsciiLetter()) i++
    if (i == 1) return null
    val tag = substring(1, i)

    val wsStart = i
    while (i < length && this[i].isWhitespace()) i++
    if (i == wsStart) return null

    if (i >= length || this[i] != '"') return null
    val valueStart = i + 1
    val valueEnd = length - 2
    if (valueStart >= valueEnd) return null

    return Pair(tag, substring(valueStart, valueEnd))
}

private fun Char.isAsciiLetter(): Boolean = this in 'A'..'Z' || this in 'a'..'z'

private inline fun tokenizeMoves(movetext: String, onToken: (String) -> Unit) {
    var i = 0
    val len = movetext.length
    while (i < len) {
        // Skip whitespace and brace comments
        while (i < len) {
            val c = movetext[i]
            if (c.isWhitespace()) {
                i++
            } else if (c == '{') {
                while (i < len && movetext[i] != '}') i++
                if (i < len) i++ // skip the closing '}'
            } else {
                break
            }
        }
        if (i >= len) break

        val start = i
        // Find end of token, but stop if we hit a comment start
        while (i < len && !movetext[i].isWhitespace() && movetext[i] != '{') i++

        // Extract move from potential move number (e.g., "1.e4" or "1...e5")
        var j = start
        while (j < i && movetext[j].isDigit()) j++
        if (j > start && j < i && movetext[j] == '.') {
            while (j < i && movetext[j] == '.') j++
            if (j < i) onToken(movetext.substring(j, i))
        } else {
            // Handle leading dots (e.g., "...Nf6")
            var k = start
            while (k < i && movetext[k] == '.') k++
            if (k < i) onToken(movetext.substring(k, i))
        }
    }
}

private fun Game.loadPly(token: String) = when {
    isKingSideCastle(token) -> play(findCastlePly(info.turn, CastleType.KingSide))
    isQueenSideCastle(token) -> play(findCastlePly(info.turn, CastleType.QueenSide))
    else -> play(findPly(token))
}

private fun isKingSideCastle(token: String): Boolean =
    token.startsWith("O-O") && !token.startsWith("O-O-O") &&
        token.drop(3).all { it.isPgnSuffix() }

private fun isQueenSideCastle(token: String): Boolean =
    token.startsWith("O-O-O") &&
        token.drop(5).all { it.isPgnSuffix() }

private fun Char.isPgnSuffix(): Boolean = this == '+' || this == '#' || this == '!' || this == '?'

private fun Game.findCastlePly(side: Side, castle: CastleType): Ply {
    val kingLoc = this.board.king(side)
        ?: throw SerializeException("Found castling move but can't find king for $side")
    val ply = plies(kingLoc).find { it.to == castle.end(side) }
        ?: throw SerializeException("Can't find castling move for $side")
    return ply
}

/**
 * Backward SAN parser: strips annotations from the tail inward, then reads
 * promotion, destination, capture marker, piece letter, and disambiguation.
 */
private fun Game.findPly(token: String): Ply {
    var end = token.length
    while (end > 0 && token[end - 1].isPgnSuffix()) end--

    var promotion: Piece? = null
    if (end >= 2 && token[end - 2] == '=') {
        promotion = charToPiece(token[end - 1])
            ?: throw SerializeException("Invalid promotion in: $token")
        end -= 2
    }

    if (end < 2) throw SerializeException("Invalid move: $token")
    val destFile = token[end - 2].toFile()
        ?: throw SerializeException("Invalid destination in: $token")
    val destRank = token[end - 1].toRank()
        ?: throw SerializeException("Invalid destination in: $token")
    val to = Locus.from(destFile, destRank)
    end -= 2

    if (end > 0 && token[end - 1] == 'x') end--

    val piece: Piece
    var disambigFile: File? = null
    var disambigRank: Rank? = null

    if (end > 0 && token[0] in PIECE_CHARS) {
        piece = charToPiece(token[0])!!
        var d = 1
        if (d < end) { token[d].toFile()?.let { disambigFile = it; d++ } }
        if (d < end) { token[d].toRank()?.let { disambigRank = it } }
    } else {
        piece = Piece.Pawn
        var d = 0
        if (d < end) { token[d].toFile()?.let { disambigFile = it; d++ } }
        if (d < end) { token[d].toRank()?.let { disambigRank = it } }
    }

    return plies()
        .filter { it.to == to && it.piece == piece }
        .filter {
            disambigFile?.let { file -> it.from.file == file } ?: true &&
                disambigRank?.let { rank -> it.from.rank == rank } ?: true
        }
        .apply { if (size != 1) throw SerializeException("Can't find single ply in: $token") }
        .first()
        .apply { promotion?.let { promote(it) } }
}

private const val PIECE_CHARS = "NBRQK"

private fun charToPiece(c: Char): Piece? = when (c) {
    'N' -> Piece.Knight
    'B' -> Piece.Bishop
    'R' -> Piece.Rook
    'Q' -> Piece.Queen
    'K' -> Piece.King
    else -> null
}
