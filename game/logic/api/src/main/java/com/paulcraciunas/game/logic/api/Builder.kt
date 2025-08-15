package com.paulcraciunas.game.logic.api

import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.state.MetaData
import java.util.Queue

interface Builder {
    fun withMetadata(metaData: MetaData): Builder
    fun withRating(rating: Int): Builder
    fun withMoves(moves: Queue<String>): Builder
    fun withMoves(vararg moves: String): Builder
    fun withTurn(side: Side): Builder
    fun withWhiteCastling(casting: Set<CastleType>): Builder
    fun withBlackCastling(casting: Set<CastleType>): Builder
    fun withPlieClock(clock: Int): Builder
    fun withMoveIndex(index: Int): Builder
    fun withLastPly(turn: Side, piece: Piece, from: Locus, to: Locus): Builder
    fun withDefaultBoard(): Builder
    fun withPiece(piece: Piece, side: Side, at: Locus): Builder
    fun withCastling(castling: Pair<Set<CastleType>, Set<CastleType>>): Builder

    fun buildGame(): Game
    fun buildPuzzle(): Puzzle
}
