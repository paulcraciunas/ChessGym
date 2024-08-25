package com.paulcraciunas.game.io

import com.paulcraciunas.game.Game
import com.paulcraciunas.game.MetaData
import com.paulcraciunas.game.Side
import com.paulcraciunas.game.board.File
import com.paulcraciunas.game.board.Locus
import com.paulcraciunas.game.board.Piece
import com.paulcraciunas.game.board.Rank
import com.paulcraciunas.game.board.toFile
import com.paulcraciunas.game.board.toRank
import com.paulcraciunas.game.plies.CastlePly
import com.paulcraciunas.game.plies.Ply

object PgnSerializer : Serializer {
    // I hate regEx
    private val headerRegex = Regex("\\[([A-Za-z]+)\\s+\"(.+)\"]")
    private val moveSplitRegex = Regex("([0-9]+)\\.\\s?(\\S+)(?:\\s+(\\S+))?")
    private val endingRegex = Regex("(1-0|0-1|1/2-1/2)\$")

    override fun from(gameString: String): Game {
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
        return Game(metaData = MetaData(headers)).apply {
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

    override fun of(game: Game): String = StringBuilder().apply {
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

private fun Game.loadPly(plyString: String) = when {
    kingSideRegex.matches(plyString) -> play(findCastlePly(state().turn, CastlePly.Type.KingSide))
    queenSideRegex.matches(plyString) -> play(findCastlePly(state().turn, CastlePly.Type.QueenSide))
    else -> play(findPly(plyString))
}

private fun Game.findCastlePly(side: Side, castle: CastlePly.Type): Ply {
    val kingLoc = this.board().king(side)
        ?: throw SerializeException("Found castling move but can't find king for $side")
    val ply = playablePlies(kingLoc).find { it.to == castle.end(side) }
        ?: throw SerializeException("Can't find castling move for $side")
    return ply
}

private fun Game.findPly(plyString: String): Ply {
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
        .apply { bits.groupValues[6].promotion()?.let { accept(it) } }
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
