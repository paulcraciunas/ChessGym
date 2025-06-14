package com.paulcraciunas.logic.di

import com.paulcraciunas.game.logic.api.CastleType
import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.state.MetaData
import com.paulcraciunas.game.logic.impl.MutableGame
import com.paulcraciunas.game.logic.impl.MutableGameInfo
import com.paulcraciunas.game.logic.impl.MutablePuzzle
import com.paulcraciunas.game.logic.impl.board.Board
import com.paulcraciunas.game.logic.impl.board.BoardFactory
import com.paulcraciunas.game.logic.impl.plies.PlyFactory
import com.paulcraciunas.game.logic.impl.plies.StandardPly
import java.util.ArrayDeque
import java.util.Queue
import javax.inject.Inject

class Builder @Inject constructor(
    private val plyFactory: PlyFactory
) {
    private var board = Board()
    private var rating: Int? = null
    private var moves: Queue<String> = ArrayDeque()
    private var metaData: MetaData = MetaData()
    private var turn: Side = Side.WHITE
    private var lastPly: Ply? = null
    private var whiteCastling: Set<CastleType> = CastleType.entries.toSet()
    private var blackCastling: Set<CastleType> = CastleType.entries.toSet()
    private var plieClock: Int = 0
    private var moveIndex: Int = 1

    fun withMetadata(metaData: MetaData) = apply { this.metaData = metaData }
    fun withRating(rating: Int) = apply { this.rating = rating }
    fun withMoves(moves: Queue<String>) = apply { this.moves.addAll(moves) }
    fun withTurn(side: Side) = apply { turn = side }
    fun withWhiteCastling(casting: Set<CastleType>) = apply { whiteCastling = casting }
    fun withBlackCastling(casting: Set<CastleType>) = apply { blackCastling = casting }
    fun withPlieClock(clock: Int) = apply { plieClock = clock }
    fun withMoveIndex(index: Int) = apply { moveIndex = index }
    fun withLastPly(turn: Side, piece: Piece, from: Locus, to: Locus) = apply {
        lastPly = StandardPly(turn = turn, piece = piece, from = from, to = to)
    }

    fun withDefaultBoard() = apply { board = BoardFactory.defaultBoard() }
    fun withPiece(piece: Piece, side: Side, at: Locus) = apply {
        board.add(piece, side, at)
    }

    fun withCastling(castling: Pair<Set<CastleType>, Set<CastleType>>) = withWhiteCastling(castling.first)
        .withBlackCastling(castling.second)

    fun buildGame(): Game = MutableGame(
        metadata = metaData,
        rating = rating,
        info = MutableGameInfo(
            turn = turn,
            lastPly = lastPly,
            whiteCastling = whiteCastling,
            blackCastling = blackCastling,
            plieClock = plieClock,
            moveIndex = moveIndex
        ),
        board = board,
        plyFactory = plyFactory
    )

    fun buildPuzzle(): Puzzle = MutablePuzzle(
        rating = rating!!,
        player = turn.other(),
        info = MutableGameInfo(
            turn = turn,
            lastPly = lastPly,
            whiteCastling = whiteCastling,
            blackCastling = blackCastling,
            plieClock = plieClock,
            moveIndex = moveIndex
        ),
        board = board,
        moves = moves,
        plyFactory = plyFactory
    )
}
