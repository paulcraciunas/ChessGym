package com.paulcraciunas.game.logic.impl.board

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.game.logic.api.board.SidedPiece
import java.util.EnumMap
import java.util.EnumSet

data class Board(private val board: Array<SidedPiece?> = arrayOfNulls(64)) : IBoard {
    private val pieces: EnumMap<SidedPiece, EnumSet<Locus>> = EnumMap(SidedPiece::class.java)

    init {
        SidedPiece.entries.forEach { sidedPiece ->
            pieces[sidedPiece] = EnumSet.noneOf(Locus::class.java)
        }
    }

    override fun from(other: IBoard): Board = apply {
        Locus.entries.forEach { locus ->
            val piece = other.at(locus)
            val side = Side.entries.firstOrNull { other.has(it, locus) }

            board[locus.ordinal] = if (piece != null && side != null) {
                SidedPiece.of(side, piece)
            } else null
        }

        SidedPiece.entries.forEach { sidedPiece ->
            val targetSet = pieces[sidedPiece]!!
            targetSet.clear()
            targetSet.addAll(other.pieces(sidedPiece.side, sidedPiece.piece))
        }
    }

    override fun add(piece: Piece, side: Side, at: Locus) {
        assert(isEmpty(at))
        val sidedPiece = SidedPiece.of(side, piece)
        board[at.ordinal] = sidedPiece
        pieces[sidedPiece]!!.add(at)
    }

    override fun remove(at: Locus): Piece? {
        val removedSidedPiece = board[at.ordinal] ?: return null
        board[at.ordinal] = null

        pieces[removedSidedPiece]!!.remove(at)
        return removedSidedPiece.piece
    }

    override fun forEach(action: (Piece, Locus) -> Unit) {
        Locus.entries.forEach { locus ->
            board[locus.ordinal]?.let { action(it.piece, locus) }
        }
    }

    override fun forEachPiece(turn: Side, action: (Piece, Locus) -> Unit) {
        SidedPiece.entries.filter { it.side == turn }.forEach { sidedPiece ->
            pieces[sidedPiece]!!.forEach { locus ->
                action(sidedPiece.piece, locus)
            }
        }
    }

    override fun has(piece: Piece, side: Side, at: Locus): Boolean =
        pieces[SidedPiece.of(side, piece)]!!.contains(at)

    override fun has(side: Side, at: Locus): Boolean = board[at.ordinal]?.side == side

    override fun has(side: Side, action: (Piece, Locus) -> Boolean): Boolean {
        return SidedPiece.entries.filter { it.side == side }.any { sidedPiece ->
            pieces[sidedPiece]!!.any { locus -> action(sidedPiece.piece, locus) }
        }
    }

    override fun king(side: Side): Locus? = pieces[SidedPiece.of(side, Piece.King)]!!.firstOrNull()
    override fun at(at: Locus): Piece? = board[at.ordinal]?.piece
    override fun at(file: File, rank: Rank): Piece? = board[lookup[rank.ordinal][file.ordinal].ordinal]?.piece
    override fun pieces(side: Side, piece: Piece): Set<Locus> = pieces[SidedPiece.of(side, piece)]!!
    override fun isEmpty(at: Locus): Boolean = board[at.ordinal] == null
    override fun isEmpty(file: File, rank: Rank): Boolean = board[lookup[rank.ordinal][file.ordinal].ordinal] == null
    override fun move(from: Locus, to: Locus, turn: Side): Piece? {
        assert(!isEmpty(from))
        assert(isEmpty(to) || has(turn.other(), to))

        val movingSidedPiece = board[from.ordinal]!!
        val capturedSidedPiece = board[to.ordinal]

        board[to.ordinal] = movingSidedPiece
        board[from.ordinal] = null

        pieces[movingSidedPiece]!!.remove(from)
        pieces[movingSidedPiece]!!.add(to)

        capturedSidedPiece?.let {
            pieces[it]!!.remove(to)
        }

        return capturedSidedPiece?.piece
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Board
        return board.contentEquals(other.board) && pieces == other.pieces
    }

    override fun hashCode(): Int {
        var result = board.contentHashCode()
        result = 31 * result + pieces.hashCode()
        return result
    }

    companion object {
        private val lookup: Array<Array<Locus>> = Array(Rank.entries.size) { r ->
            Array(File.entries.size) { f ->
                Locus.entries.first { it.rank.ordinal == r && it.file.ordinal == f }
            }
        }
    }
}
