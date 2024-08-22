package com.paulcraciunas.chessgym.game

import com.paulcraciunas.chessgym.game.board.Board
import com.paulcraciunas.chessgym.game.board.BoardFactory
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece
import com.paulcraciunas.chessgym.game.plies.Ply
import com.paulcraciunas.chessgym.game.plies.PlyFactory
import com.paulcraciunas.chessgym.game.plies.PromotionPly

class Game(
    private val board: Board = BoardFactory.defaultBoard(),
    private val settings: Settings = Settings(),
    state: GameState = GameState(),
) {
    constructor(board: Board, turn: Side) : this(board = board, state = GameState(turn = turn))

    private val availablePlies = mutableListOf<Ply>()
    private val plies = mutableListOf<Ply>()
    private var currentState = state
    private var ending: Ending? = null

    private val plyFactory = PlyFactory()
    private val endingStrategy = EndingStrategy(availablePlies, board, plies, settings)

    init {
        // Update initial state. Useful for loading "in media res" (e.g. puzzles)
        currentState = currentState.copy(inCheckCount = checkCount(currentState.turn))
        updateState()
    }

    fun turn(): Side = currentState.turn

    fun playablePlies(from: Locus): Collection<Ply> = availablePlies.filter { it.from == from }

    fun play(ply: Ply) {
        assert(ending == null)
        assert(availablePlies.contains(ply))

        // Execute and keep track
        ply.exec(board)
        plies.add(ply)

        // Update state
        currentState = currentState.next(plies.last(), checkCount(currentState.turn.other()))
        updateState()
    }

    fun ending(): Ending? = ending

    private fun updateState() {
        computeAvailablePlies()
        updateResolution()
    }

    private fun updateResolution() { // Important to call after updating game state
        ending = endingStrategy.of(currentState)
        if (ending != null) {
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
        board.king(turn)?.let { plyFactory.canCheck(it, board, turn.other()) } ?: CheckCount.None
}
