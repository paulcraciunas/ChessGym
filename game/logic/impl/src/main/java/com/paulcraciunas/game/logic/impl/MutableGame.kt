package com.paulcraciunas.game.logic.impl

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Result
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.diagnostics.PlayedMovesLog
import com.paulcraciunas.game.logic.api.state.MetaData
import com.paulcraciunas.game.logic.impl.board.Board
import com.paulcraciunas.game.logic.impl.board.BoardFactory
import com.paulcraciunas.game.logic.impl.gameover.CheckMateStrategy
import com.paulcraciunas.game.logic.impl.gameover.DrawByInsufficientMaterialStrategy
import com.paulcraciunas.game.logic.impl.gameover.DrawByMoveRuleStrategy
import com.paulcraciunas.game.logic.impl.gameover.DrawByRepetitionStrategy
import com.paulcraciunas.game.logic.impl.gameover.StaleMateStrategy
import com.paulcraciunas.game.logic.impl.plies.CheckPly
import com.paulcraciunas.game.logic.impl.plies.Playable
import com.paulcraciunas.game.logic.impl.plies.PlyFactory

internal class MutableGame(
    override val metadata: MetaData = MetaData(),
    override val rating: Int? = null,
    override val info: MutableGameInfo = MutableGameInfo(),
    override val board: Board = BoardFactory.defaultBoard(),
    override var state: Game.GameState = Game.GameState.Ready,
    override val plyFactory: PlyFactory = PlyFactory(),
    override val plies: MutableList<Playable> = mutableListOf(),
    private val fullHistory: MutableList<MutableGameInfo> = mutableListOf(),
) : Game, Executable() {
    constructor(board: Board, turn: Side) : this(board = board, info = MutableGameInfo(turn = turn))
    private val moveAdapter = MoveAdapter()

    override val history: List<Ply>
        get() = fullHistory.drop(1).take(historyIndex).mapNotNull { it.lastPly }

    override val historySize: Int
        get() = fullHistory.size - 1

    override val currentMoveIndex: Int
        get() = historyIndex

    private var historyIndex = 0

    init {
        if (fullHistory.isEmpty()) {
            fullHistory.add(info.copy())
        }
    }

    override fun start() {
        assert(state == Game.GameState.Ready)

        PlayedMovesLog.reset()
        state = Game.GameState.InProgress
        info.inCheckCount = checkCount(info.turn)
        updateState()
    }

    override fun plies(): List<Ply> = plies
    override fun plies(from: Locus): List<Ply> = plies.filter { it.from == from }
    override fun ply(from: Locus, to: Locus): Ply? = plies.firstOrNull { it.from == from && it.to == to }
    override fun play(ply: Ply) {
        assert(state == Game.GameState.InProgress)

        execute(ply)
    }

    override fun play(from: Locus, to: Locus) = play(plies(from).first { it.to == to })
    override fun play(ply: String) {
        assert(state == Game.GameState.InProgress)
        val move = moveAdapter.from(ply)
        plies(move.from).first { it.to == move.to }.apply {
            if (move.promotion != null) {
                promote(move.promotion)
            } else {
                play(from, to)
            }
        }
    }
    override fun resign() = finish(Result.Resigned)
    override fun draw() = finish(Result.DrawByAgreement)
    override fun isRunning(): Boolean = state == Game.GameState.InProgress

    override fun recomputeState(ply: Ply?) = stateStrategies.forEach {
        if (state == Game.GameState.InProgress) {
            state = it(this)
        }
    }

    override fun saveInfo() {
        // Clear future history if we're not at the end
        while (canReplay()) {
            fullHistory.removeLastOrNull()
        }

        info.lastPly?.let {
            info.lastPly = if (plyFactory.isCheck(it, board)) CheckPly(it) else it
        }
        fullHistory.add(info.copy())
        historyIndex++
    }

    override fun canUndo(): Boolean = historyIndex > 0

    override fun undoLast() {
        assert(canUndo())
        // If the game ended, we need to un-End it, otherwise attempting to play a move will throw an exception
        if (state is Game.GameState.Finished) {
            state = Game.GameState.InProgress
        }

        --historyIndex
        info.lastPly?.undo(on = board)
        info.update(to = fullHistory[historyIndex])
        updateState()
    }

    override fun undoAll() {
        if (!canUndo()) return

        // If the game ended, we need to un-End it, otherwise attempting to play a move will throw an exception
        if (state is Game.GameState.Finished) {
            state = Game.GameState.InProgress
        }

        while (canUndo()) {
            info.lastPly?.undo(on = board)
            --historyIndex
            info.update(to = fullHistory[historyIndex])
        }
        updateState()
    }

    override fun canReplay(): Boolean = historyIndex < fullHistory.size - 1

    override fun replayNext() {
        assert(canReplay())

        ++historyIndex
        info.update(to = fullHistory[historyIndex])
        info.lastPly?.exec(on = board)
        updateState()
    }

    override fun replayAll() {
        if (!canReplay()) return

        while (canReplay()) {
            ++historyIndex
            info.update(to = fullHistory[historyIndex])
            info.lastPly?.exec(on = board)
        }
        updateState()
    }

    private fun finish(result: Result) {
        assert(state == Game.GameState.InProgress)

        state = Game.GameState.Finished(result)
    }
}

private val stateStrategies = listOf(
    CheckMateStrategy(),
    StaleMateStrategy(),
    DrawByRepetitionStrategy(),
    DrawByMoveRuleStrategy(),
    DrawByInsufficientMaterialStrategy(),
)
