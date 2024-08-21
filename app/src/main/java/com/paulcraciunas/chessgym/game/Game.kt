package com.paulcraciunas.chessgym.game

import com.paulcraciunas.chessgym.game.board.Board
import com.paulcraciunas.chessgym.game.board.BoardFactory
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece
import com.paulcraciunas.chessgym.game.plies.CastlePly
import com.paulcraciunas.chessgym.game.plies.Ply
import com.paulcraciunas.chessgym.game.plies.PlyFactory
import com.paulcraciunas.chessgym.game.plies.PromotionPly
import com.paulcraciunas.chessgym.game.plies.StandardPly

class Game(
    private val board: Board = BoardFactory.defaultBoard(),
    private val settings: Settings = Settings(),
    turn: Side = Side.WHITE,
) {
    private val availablePlies = mutableListOf<Ply>()
    private val plies = mutableListOf<PlyWithState>()
    private var currentState = GameState(turn = turn)
    private var resolution: Resolution? = null

    private val plyFactory = PlyFactory()
    private val resolutionFactory = ResolutionFactory(availablePlies, board, plies, settings)

    init {
        computeAvailablePlies()
        // Compute initial state. Useful for loading "in media res" (e.g. puzzles)
        val inCheckCount = board.king(currentState.turn)?.let {
            plyFactory.canCheck(it, board, currentState.turn.other())
        } ?: CheckCount.None
        val castling = CastlePly.Type.entries.toSet()
        currentState = currentState.copy(
            inCheckCount = inCheckCount,
            castling = castling
        )
        updateResolution()
    }

    fun turn(): Side = currentState.turn

    fun playablePlies(from: Locus): Collection<Ply> = availablePlies.filter { it.from == from }

    fun play(ply: Ply) {
        assert(resolution == null)
        assert(availablePlies.contains(ply))

        val turn = currentState.turn
        val state = playedState(turn, ply)

        ply.exec(board)
        plies.add(PlyWithState(ply, state))

        // Set new state
        updateCurrentState(turn, ply)

        computeAvailablePlies()
        updateResolution()
    }

    private fun updateResolution() {
        resolution = resolutionFactory.of(currentState)
        if (resolution != null) {
            availablePlies.clear()
        }
    }

    fun resolution(): Resolution? = resolution

    private fun computeAvailablePlies() {
        availablePlies.clear()
        availablePlies.addAll(plyFactory.allLegalPlies(board, currentState))
        availablePlies.forEach {
            if (it is PromotionPly && settings.autoPromote) {
                it.accept(Piece.Queen)
            }
        }
    }

    private fun playedState(turn: Side, ply: Ply) = GameState(
        turn = turn,
        lastPly = plies.lastOrNull()?.ply,
        inCheckCount = CheckCount.None, // We can't move into check
        castling = updateCurrentCastling(ply = ply, state = currentState)
    )

    private fun updateCurrentState(turn: Side, ply: Ply) {
        val inCheckCount = board.king(turn.other())?.let { plyFactory.canCheck(it, board, turn) }
            ?: CheckCount.None
        val castling = plies.getOrNull(plies.size - 2)?.stateAfter?.let {
            updateOpponentCastling(ply, it)
        } ?: CastlePly.Type.entries.toSet()
        currentState = GameState(
            turn = turn.other(),
            lastPly = ply,
            inCheckCount = inCheckCount,
            castling = castling
        )
    }

    private fun updateCurrentCastling(ply: Ply, state: GameState): Set<CastlePly.Type> {
        if (state.castling.isEmpty() || ply.piece == Piece.King) return emptySet()
        val castling = mutableSetOf<CastlePly.Type>().apply { addAll(state.castling) }
        when (ply) {
            is CastlePly -> castling.remove(ply.type)
            is StandardPly -> {
                castling.forEach {
                    // If the corresponding Rook was moved
                    if (ply.piece == Piece.Rook && ply.from == it.rook(state.turn)) {
                        castling.remove(it)
                    }
                }
            }
        }
        return castling
    }

    private fun updateOpponentCastling(ply: Ply, state: GameState): Set<CastlePly.Type> {
        val castling = mutableSetOf<CastlePly.Type>().apply { addAll(state.castling) }
        if (ply is StandardPly && ply.captured == Piece.Rook) {
            castling.forEach {
                // If the corresponding Rook was captured
                if (ply.to == it.rook(state.turn)) {
                    castling.remove(it)
                }
            }
        }
        return castling
    }
}

class PlyWithState(val ply: Ply, val stateAfter: GameState)
