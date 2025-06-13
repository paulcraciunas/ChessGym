package com.paulcraciunas.game.logic.impl

import com.paulcraciunas.game.logic.api.IGame
import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Result
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.state.CheckCount
import com.paulcraciunas.game.logic.api.state.MetaData
import com.paulcraciunas.game.logic.api.state.Settings
import com.paulcraciunas.game.logic.impl.board.BoardFactory
import com.paulcraciunas.game.logic.impl.plies.CheckPly
import com.paulcraciunas.game.logic.impl.plies.Playable
import com.paulcraciunas.game.logic.impl.plies.PlyFactory
import com.paulcraciunas.game.logic.impl.plies.PromotionPly

class Game(
    private val board: IBoard = BoardFactory.defaultBoard(),
    private val settings: Settings = Settings(),
    private val metaData: MetaData = MetaData(),
    override val info: MutableGameInfo = MutableGameInfo(),
) : IGame {
    constructor(board: IBoard, turn: Side) : this(board = board, info = MutableGameInfo(turn = turn))

    private val plies = mutableListOf<Playable>()
    private var currentState = info
    private var result: Result? = null

    private val plyFactory = PlyFactory()
    private val endingStrategy = EndingStrategy(info.plies, board, plies, settings)

    init {
        // Update initial state. Useful for loading "in media res" (e.g. puzzles)
        currentState.inCheckCount = checkCount(currentState.turn)
        updateState()
    }

    override fun turn(): Side = currentState.turn
    override fun board(): IBoard = board
    override fun state(): MutableGameInfo = currentState
    override fun metaData() = metaData

    override fun allPlies(): List<Playable> = plies
    override fun playablePlies(from: Locus): Collection<Playable> = info.plies.filter { it.from == from }
    override fun allPlayablePlies(): Collection<Playable> = info.plies
    override fun isOver(): Result? = result

    override fun play(ply: Ply) {
        assert(result == null)
        assert(info.plies.contains(ply))
        val playable = info.plies.find { it == ply }!!

        // Execute and keep track
        playable.resolve(info.plies.filter { it.piece == ply.piece && it.to == ply.to }
            .disambiguate())
        playable.exec(board)
        plies.add(if (plyFactory.isCheck(playable, board)) CheckPly(playable) else playable)

        // Update state
        currentState.update(playable, checkCount(currentState.turn.other()))
        updateState()
    }

    override fun promote(piece: Piece, on: Ply) = on.promote(piece)
    override fun resign() {
        result = Result.Resigned
    }

    override fun agreeToDraw() {
        result = Result.DrawByAgreement
    }

    private fun updateState() {
        computeAvailablePlies()
        updateResolution()
    }

    private fun updateResolution() { // Important to call after updating game state
        result = endingStrategy.of(currentState)
        if (result != null) {
            info.plies.clear()
        }
    }

    private fun computeAvailablePlies() {
        info.plies.clear()
        info.plies.addAll(plyFactory.allLegalPlies(board, currentState))
        info.plies.forEach {
            if (it is PromotionPly && settings.autoPromote) {
                it.promote(Piece.Queen)
            }
        }
    }

    private fun checkCount(turn: Side) =
        board.king(turn)?.let { plyFactory.checkCount(it, board, turn.other()) } ?: CheckCount.None
}

private fun List<Playable>.disambiguate(): Ply.Disambiguate = when {
    size >= 3 -> Ply.Disambiguate.Both
    size == 2 -> if (get(0).from.file == get(1).from.file) Ply.Disambiguate.Rank else Ply.Disambiguate.File
    else -> Ply.Disambiguate.None
}
