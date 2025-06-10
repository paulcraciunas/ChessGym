package com.paulcraciunas.game.logic.api.board

import com.paulcraciunas.game.logic.api.Side

interface IBoard {
    fun from(other: IBoard): IBoard
    fun add(piece: Piece, side: Side, at: Locus)
    fun remove(at: Locus): Piece?

    fun forEach(action: (Piece, Locus) -> Unit)
    fun forEachPiece(turn: Side, action: (Piece, Locus) -> Unit)
    fun has(piece: Piece, side: Side, at: Locus): Boolean
    fun has(side: Side, at: Locus): Boolean
    fun has(side: Side, action: (Piece, Locus) -> Boolean): Boolean
    fun king(side: Side): Locus?
    fun at(at: Locus): Piece?
    fun at(file: File, rank: Rank): Piece?
    fun pieces(side: Side, piece: Piece): Set<Locus>

    fun isEmpty(at: Locus): Boolean
    fun isEmpty(file: File, rank: Rank): Boolean

    fun move(from: Locus, to: Locus, turn: Side): Piece?
}
