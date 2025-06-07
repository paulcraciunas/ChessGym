package com.paulcraciunas.game.logic

import com.paulcraciunas.game.logic.board.Board
import com.paulcraciunas.game.logic.board.BoardFactory
import com.paulcraciunas.game.logic.board.Locus
import com.paulcraciunas.game.logic.board.Piece
import com.paulcraciunas.game.logic.plies.CheckPly
import com.paulcraciunas.game.logic.plies.Ply
import com.paulcraciunas.game.logic.plies.PlyFactory
import com.paulcraciunas.game.logic.plies.PromotionPly

class Game(
    private val board: Board = BoardFactory.defaultBoard(),
    private val settings: Settings = Settings(),
    private val metaData: MetaData = MetaData(),
    state: GameState = GameState(),
) {
    constructor(board: Board, turn: Side) : this(board = board, state = GameState(turn = turn))

    private val availablePlies = mutableListOf<Ply>()
    private val plies = mutableListOf<Ply>()
    private var currentState = state
    private var result: Result? = null

    private val plyFactory = PlyFactory()
    private val endingStrategy = EndingStrategy(availablePlies, board, plies, settings)

    init {
        // Update initial state. Useful for loading "in media res" (e.g. puzzles)
        currentState = currentState.copy(inCheckCount = checkCount(currentState.turn))
        updateState()
    }

    fun turn(): Side = currentState.turn

    fun playablePlies(from: Locus): Collection<Ply> =
        availablePlies.filter { it.from == from }

    fun play(ply: Ply) {
        assert(result == null)
        assert(availablePlies.contains(ply))

        // Execute and keep track
        ply.resolve(availablePlies.filter { it.piece == ply.piece && it.to == ply.to }
            .disambiguate())
        ply.exec(board)
        plies.add(if (plyFactory.isCheck(ply, board)) CheckPly(ply) else ply)

        // Update state
        currentState = currentState.next(plies.last(), checkCount(currentState.turn.other()))
        updateState()
    }

    fun requiresPromotion(ply: Ply): Boolean = !settings.autoPromote

    fun promote(piece: Piece, on: Ply) {
        on.accept(piece)
    }

    fun isOver(): Result? = result

    fun board(): Board = Board().from(board)

    fun state(): GameState = currentState

    fun resign() {
        result = Result.Resigned
    }

    fun agreeToDraw() {
        result = Result.DrawByAgreement
    }

    fun allPlies(): List<Ply> = plies

    fun allPlayablePlies(): Collection<Ply> = availablePlies

    fun metaData() = metaData

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

private fun List<Ply>.disambiguate(): Ply.Disambiguate = when {
    size >= 3 -> Ply.Disambiguate.Both
    size == 2 -> if (get(0).from.file == get(1).from.file) Ply.Disambiguate.Rank else Ply.Disambiguate.File
    else -> Ply.Disambiguate.None
}
