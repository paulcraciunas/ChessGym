package com.paulcraciunas.game.logic.impl

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Result
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
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
    override val history: MutableList<Playable> = mutableListOf(),
    override val plyFactory: PlyFactory = PlyFactory(),
) : Game, Executable() {
    constructor(board: Board, turn: Side) : this(board = board, info = MutableGameInfo(turn = turn))

    override fun start() {
        assert(state == Game.GameState.Ready)

        state = Game.GameState.InProgress
        info.inCheckCount = checkCount(info.turn)
        updateState()
    }

    override fun play(ply: Ply) {
        assert(state == Game.GameState.InProgress)

        execute(ply)
    }

    override fun play(from: Locus, to: Locus) = play(info.plies(from).first { it.to == to })
    override fun resign() = finish(Result.Resigned)
    override fun draw() = finish(Result.DrawByAgreement)
    override fun isRunning(): Boolean = state == Game.GameState.InProgress

    override fun recomputeState() = stateStrategies.forEach {
        if (state == Game.GameState.InProgress) {
            state = it(this)
        }
    }

    override fun savePly(playable: Playable) {
        history.add(if (plyFactory.isCheck(playable, board)) CheckPly(playable) else playable)
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
