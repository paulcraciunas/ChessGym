package com.paulcraciunas.game.logic.impl

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Result
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.state.CheckCount
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

class MutableGame(
    override val metadata: MetaData = MetaData(),
    override val rating: Int? = null,
    override val info: MutableGameInfo = MutableGameInfo(),
    override val board: Board = BoardFactory.defaultBoard(),
    override var state: Game.GameState = Game.GameState.Ready,
    override val history: MutableList<Playable> = mutableListOf(),
    private val plyFactory: PlyFactory = PlyFactory(),
) : Game {
    constructor(board: Board, turn: Side) : this(board = board, info = MutableGameInfo(turn = turn))

    override fun start() {
        assert(state == Game.GameState.Ready)

        state = Game.GameState.InProgress
        info.inCheckCount = checkCount(info.turn)
        updateState()
    }

    override fun play(ply: Ply) {
        assert(state == Game.GameState.InProgress)
        assert(info.plies.contains(ply))
        val playable = info.plies.find { it == ply }!!

        // Execute and keep track
        playable.resolve(info.plies.filter { it.piece == ply.piece && it.to == ply.to }
            .disambiguate())
        playable.exec(board)
        history.add(if (plyFactory.isCheck(playable, board)) CheckPly(playable) else playable)

        // Update state
        info.update(playable, checkCount = checkCount(info.turn.other()))
        updateState()
    }

    override fun resign() {
        assert(state == Game.GameState.InProgress)

        state = Game.GameState.Finished(Result.Resigned)
    }

    override fun draw() {
        state = Game.GameState.Finished(Result.DrawByAgreement)
    }

    private fun updateState() {
        computeAvailablePlies()
        updateResolution()
    }

    private fun updateResolution() { // Important to call after updating game state
        stateStrategies.forEach {
            if (state == Game.GameState.InProgress) {
                state = it(this)
            }
        }
        if (state != Game.GameState.InProgress) {
            info.plies.clear()
        }
    }

    private fun computeAvailablePlies() {
        info.plies.clear()
        info.plies.addAll(plyFactory.allLegalPlies(board, info))
    }

    private fun checkCount(turn: Side) =
        board.king(turn)?.let { plyFactory.checkCount(it, board, turn.other()) } ?: CheckCount.None
}

private val stateStrategies = listOf(
    CheckMateStrategy(),
    StaleMateStrategy(),
    DrawByRepetitionStrategy(),
    DrawByMoveRuleStrategy(),
    DrawByInsufficientMaterialStrategy(),
)

private fun List<Playable>.disambiguate(): Ply.Disambiguate = when {
    size >= 3 -> Ply.Disambiguate.Both
    size == 2 -> if (get(0).from.file == get(1).from.file) Ply.Disambiguate.Rank else Ply.Disambiguate.File
    else -> Ply.Disambiguate.None
}
