package com.paulcraciunas.game.board

import com.paulcraciunas.game.Side
import com.paulcraciunas.game.api.IBoard
import java.util.EnumMap

internal data class Board(
    private val board: Array<Array<Piece?>> =
        Array(Rank.entries.size) {
            Array(File.entries.size) { null }
        },
) : IBoard {
    // Useful to keep track of, as we use this often in checking move validity
    private val pieces: EnumMap<Side, EnumMap<Piece, HashSet<Locus>>> = EnumMap(Side::class.java)

    init {
        Side.entries.forEach { side ->
            pieces[side] = EnumMap(Piece::class.java)
            Piece.entries.forEach { piece ->
                pieces[side]!![piece] = HashSet()
            }
        }
    }

    fun from(other: Board): Board = apply {
        Locus.all {
            board[it.rank.dec()][it.file.dec()] = other.board[it.rank.dec()][it.file.dec()]
        }
        Side.entries.forEach { side ->
            Piece.entries.forEach { piece ->
                pieces[side]!![piece]!!.clear()
                pieces[side]!![piece]!!.addAll(other.pieces[side]!![piece]!!)
            }
        }
    }

    fun add(piece: Piece, side: Side, at: Locus) {
        assert(isEmpty(at))

        board[at.rank.dec()][at.file.dec()] = piece
        pieces[side]!![piece]!!.add(at)
    }

    fun remove(at: Locus): Piece? {
        val removed = board[at.rank.dec()][at.file.dec()]
        board[at.rank.dec()][at.file.dec()] = null
        removed?.let {
            Side.entries.forEach {
                pieces[it]!![removed]!!.remove(at)
            }
        }
        return removed
    }

    fun forEach(action: (Piece, Locus) -> Unit) {
        Locus.all { loc ->
            board[loc.rank.dec()][loc.file.dec()]?.let { action(it, loc) }
        }
    }

    override fun forEachPiece(turn: Side, action: (Piece, Locus) -> Unit) {
        pieces[turn]!!.forEach { entry ->
            entry.value.forEach { locus ->
                action(entry.key, locus)
            }
        }
    }

    fun has(piece: Piece, side: Side, at: Locus): Boolean =
        pieces[side]!![piece]!!.contains(at)

    fun has(side: Side, at: Locus): Boolean =
        Piece.entries.any { pieces[side]!![it]!!.contains(at) }

    fun has(side: Side, action: (Piece, Locus) -> Boolean): Boolean {
        for (entry in pieces[side]!!) {
            for (locus in entry.value) {
                if (action(entry.key, locus)) {
                    return true
                }
            }
        }
        return false
    }

    fun king(side: Side): Locus? = pieces[side]!![Piece.King]!!.firstOrNull()

    fun at(at: Locus): Piece? = board[at.rank.dec()][at.file.dec()]

    fun isEmpty(at: Locus): Boolean = board[at.rank.dec()][at.file.dec()] == null

    fun move(from: Locus, to: Locus, turn: Side): Piece? {
        assert(!isEmpty(from))
        assert(isEmpty(to) || has(turn.other(), to))

        val captured = board[to.rank.dec()][to.file.dec()]
        board[to.rank.dec()][to.file.dec()] = board[from.rank.dec()][from.file.dec()]
        board[from.rank.dec()][from.file.dec()] = null
        Piece.entries.forEach { piece ->
            if (pieces[turn]!![piece]!!.contains(from)) {
                pieces[turn]!![piece]!!.remove(from)
                pieces[turn]!![piece]!!.add(to)
            }
        }
        captured?.let {
            if (pieces[turn.other()]!![it]!!.contains(to)) {
                pieces[turn.other()]!![it]!!.remove(to)
            }
        }
        return captured
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Board

        if (!board.contentDeepEquals(other.board)) return false
        if (pieces != other.pieces) return false

        return true
    }

    override fun hashCode(): Int {
        var result = board.contentDeepHashCode()
        result = 31 * result + pieces.hashCode()
        return result
    }
}
