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

class RealBuilder(private val plyFactory: PlyFactory) : Builder {
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

    override fun withMetadata(metaData: MetaData) = apply { this.metaData = metaData }
    override fun withRating(rating: Int) = apply { this.rating = rating }
    override fun withMoves(moves: Queue<String>) = apply { this.moves.addAll(moves) }
    override fun withTurn(side: Side) = apply { turn = side }
    override fun withWhiteCastling(casting: Set<CastleType>) = apply { whiteCastling = casting }
    override fun withBlackCastling(casting: Set<CastleType>) = apply { blackCastling = casting }
    override fun withPlieClock(clock: Int) = apply { plieClock = clock }
    override fun withMoveIndex(index: Int) = apply { moveIndex = index }
    override fun withLastPly(turn: Side, piece: Piece, from: Locus, to: Locus) = apply {
        lastPly = StandardPly(turn = turn, piece = piece, from = from, to = to)
    }

    override fun withDefaultBoard() = apply { board = BoardFactory.defaultBoard() }
    override fun withPiece(piece: Piece, side: Side, at: Locus) = apply {
        board.add(piece, side, at)
    }

    override fun withCastling(castling: Pair<Set<CastleType>, Set<CastleType>>) = withWhiteCastling(castling.first)
        .withBlackCastling(castling.second)

    override fun buildGame(): Game = MutableGame(
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

    override fun buildPuzzle(): Puzzle = MutablePuzzle(
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
