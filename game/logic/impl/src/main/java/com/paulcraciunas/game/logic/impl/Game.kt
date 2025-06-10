package com.paulcraciunas.game.logic.impl

import com.paulcraciunas.game.logic.api.IGame
import com.paulcraciunas.game.logic.api.IPly
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
    override val state: GameState = GameState(),
) : IGame {
    constructor(board: IBoard, turn: Side) : this(board = board, state = GameState(turn = turn))

    private val availablePlies = mutableListOf<Playable>()
    private val plies = mutableListOf<Playable>()
    private var currentState = state
    private var result: Result? = null

    private val plyFactory = PlyFactory()
    private val endingStrategy = EndingStrategy(availablePlies, board, plies, settings)

    init {
        // Update initial state. Useful for loading "in media res" (e.g. puzzles)
        currentState = currentState.copy(inCheckCount = checkCount(currentState.turn))
        updateState()
    }

    override fun turn(): Side = currentState.turn
    override fun board(): IBoard = board
    override fun state(): GameState = currentState
    override fun metaData() = metaData

    override fun allPlies(): List<Playable> = plies
    override fun playablePlies(from: Locus): Collection<Playable> = availablePlies.filter { it.from == from }
    override fun allPlayablePlies(): Collection<Playable> = availablePlies
    override fun requiresPromotion(ply: IPly): Boolean = !settings.autoPromote
    override fun isOver(): Result? = result

    override fun play(ply: IPly) {
        assert(result == null)
        assert(availablePlies.contains(ply))

        // Execute and keep track
        ply as Playable //TODO Paul: fix down-casting
        ply.resolve(availablePlies.filter { it.piece == ply.piece && it.to == ply.to }
            .disambiguate())
        ply.exec(board)
        plies.add(if (plyFactory.isCheck(ply, board)) CheckPly(ply) else ply)

        // Update state
        currentState = currentState.next(plies.last(), checkCount(currentState.turn.other()))
        updateState()
    }

    override fun promote(piece: Piece, on: IPly) = (on as Playable).accept(piece) // TODO Paul: fix down-casting
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
            availablePlies.clear()
        }
    }

    private fun computeAvailablePlies() {
        availablePlies.clear()
        availablePlies.addAll(plyFactory.allLegalPlies(board, currentState))
        availablePlies.forEach {
            if (it is PromotionPly && settings.autoPromote) {
                it.accept(Piece.Queen)
            }
        }
    }

    private fun checkCount(turn: Side) =
        board.king(turn)?.let { plyFactory.checkCount(it, board, turn.other()) } ?: CheckCount.None
}

private fun List<Playable>.disambiguate(): Playable.Disambiguate = when {
    size >= 3 -> Playable.Disambiguate.Both
    size == 2 -> if (get(0).from.file == get(1).from.file) Playable.Disambiguate.Rank else Playable.Disambiguate.File
    else -> Playable.Disambiguate.None
}
