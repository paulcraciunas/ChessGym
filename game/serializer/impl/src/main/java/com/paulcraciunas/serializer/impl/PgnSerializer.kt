package com.paulcraciunas.serializer.impl

import com.paulcraciunas.game.logic.api.CastleType
import com.paulcraciunas.game.logic.api.IGame
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.game.logic.api.board.toFile
import com.paulcraciunas.game.logic.api.board.toRank
import com.paulcraciunas.game.logic.api.state.GameInfo
import com.paulcraciunas.game.logic.api.state.MetaData
import com.paulcraciunas.game.logic.impl.MutableGame
import com.paulcraciunas.game.logic.impl.plies.Playable
import com.paulcraciunas.serializer.api.SerializeException
import com.paulcraciunas.serializer.api.Serializer

/**
 * Portable Game Notation serializer
 *
 * Portable Game Notation (PGN) is a standard plain text format for recording chess games
 * (both the moves and related data), which can be read by humans and is also supported by most
 * chess software.
 *
 * Implementation note: it is currently implemented with RegEx, which is why it's so slow
 *
 * @see <a href="https://en.wikipedia.org/wiki/Portable_Game_Notation">PGN Wiki</a>
 **/
internal object PgnSerializer : Serializer {
    // I hate regEx
    private val headerRegex = Regex("\\[([A-Za-z]+)\\s+\"(.+)\"]")
    private val moveSplitRegex = Regex("([0-9]+)\\.\\s?(\\S+)(?:\\s+(\\S+))?")
    private val endingRegex = Regex("(1-0|0-1|1/2-1/2)\$")

    override fun serialize(gameString: String): Pair<IBoard, GameInfo> {
        val game = from(gameString)
        return Pair(game.board(), game.state())
    }

    override fun from(gameString: String): IGame {
        // TODO Paul: this is horrendously slow
        // TODO Paul: rewrite this without regex
        val lines = gameString.replace(endingRegex, "")
            .lines().filter { it.isNotBlank() }

        var idx = 0
        val headers = mutableMapOf<MetaData.Header, String>()
        for (i in lines.indices) {
            headerRegex.matchEntire(lines[i])?.let { bits ->
                MetaData.Header.of(bits.groupValues[1])
                    ?.let { header -> headers[header] = bits.groupValues[2] }
                idx = i + 1
            } ?: break
        }
        val remaining = lines.drop(idx).joinToString(separator = " ")
        return MutableGame(metaData = MetaData(headers)).apply {
            // Match moves
            moveSplitRegex.findAll(remaining).forEach { moves ->
                // ignore part 0 - the move count
                moves.groupValues[2].takeIf { it.isNotBlank() }?.let { loadPly(it) }
                moves.groupValues[3].takeIf { it.isNotBlank() }?.let { loadPly(it) }
            }
            // Match ending if we didn't already compute it
            if (allPlayablePlies().isNotEmpty()) {
                endingRegex.find(gameString)?.let {
                    if (it.groupValues[1].replace(" ", "") == "1/2-1/2") {
                        agreeToDraw()
                    } else {
                        resign()
                    }
                }
            }
        }
    }

    override fun of(game: IGame): String = StringBuilder().apply {
        MetaData.Header.entries.forEach { header ->
            game.metaData().data(header)?.let { value ->
                append("[$header \"$value\"]\n")
            }
        }
        append("\n")
        val plies = game.allPlies()
        for (i in plies.indices step 2) {
            append("${i / 2 + 1}.")
            append(plies[i].algebraic()).append(" ")
            if (i + 1 < plies.size) {
                append(plies[i + 1].algebraic()).append(" ")
            }
        }
        game.isOver()?.let { append(" ").append(it.algebraic(game.state().turn)) }
        append("\n")
    }.toString()
}

private fun MutableGame.loadPly(plyString: String) = when {
    kingSideRegex.matches(plyString) -> play(findCastlePly(state().turn, CastleType.KingSide))
    queenSideRegex.matches(plyString) -> play(findCastlePly(state().turn, CastleType.QueenSide))
    else -> play(findPly(plyString))
}

private fun MutableGame.findCastlePly(side: Side, castle: CastleType): Playable {
    val kingLoc = this.board().king(side)
        ?: throw SerializeException("Found castling move but can't find king for $side")
    val ply = playablePlies(kingLoc).find { it.to == castle.end(side) }
        ?: throw SerializeException("Can't find castling move for $side")
    return ply
}

private fun MutableGame.findPly(plyString: String): Playable {
    // Nice thing about find is we can skip game annotations
    val bits = moveRegex.find(plyString) ?: throw SerializeException("Invalid move: $plyString")
    val to = Locus.from(bits.groupValues[4] + bits.groupValues[5])
        ?: throw SerializeException("Invalid destination at $plyString")

    return allPlayablePlies()
        .filter {
            it.to == to &&
                    it.piece == pieceMap[bits.groupValues[1]]!!
        }
        .filter { // Disambiguate if needed
            bits.groupValues[2].file()?.let { file -> it.from.file == file } ?: true &&
                    bits.groupValues[3].rank()?.let { rank -> it.from.rank == rank } ?: true
        }
        .apply { if (size != 1) throw SerializeException("Can't find single ply in: $plyString") }
        .first()
        .apply { bits.groupValues[6].promotion()?.let { promote(it) } }
}

private fun String.file(): File? = if (isNotEmpty()) get(0).toFile() else null
private fun String.rank(): Rank? = if (isNotEmpty()) get(0).toRank() else null
private fun String.promotion(): Piece? = if (isNotEmpty()) pieceMap[substring(1)] else null
private val kingSideRegex = Regex("O-O[+#]?")
private val queenSideRegex = Regex("O-O-O[+#]?")
private val moveRegex = Regex("([NBRQK])?([abcdefgh])?([1-8])?x?([abcdefgh])([1-8])(=[KBRQ])?[+#]?")
private val pieceMap = mapOf(
    "" to Piece.Pawn,
    "N" to Piece.Knight,
    "B" to Piece.Bishop,
    "R" to Piece.Rook,
    "Q" to Piece.Queen,
    "K" to Piece.King
)