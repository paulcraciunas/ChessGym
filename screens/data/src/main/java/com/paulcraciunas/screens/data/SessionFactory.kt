package com.paulcraciunas.screens.data

import com.paulcraciunas.game.engine.api.EngineOrchestrator
import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import kotlinx.coroutines.CoroutineScope

class PuzzleSessionFactory(withSolution: Boolean = true) {
    private val solution: SolutionStrategy = if (withSolution) PuzzleSolution() else NoOpSolution
    private val opponent = ScriptedOpponent()
    private val session = BoardSession(solution = solution, opponent = opponent)

    fun get(): BoardSession = session

    suspend fun load(scope: CoroutineScope, puzzle: Puzzle) {
        val board = PuzzlePlayableBoard(puzzle)
        (solution as? PuzzleSolution)?.load(puzzle) // acceptable downcast
        opponent.load(puzzle)
        opponent.prepare()
        session.load(scope, board)
    }
}

class GameSessionFactory(engineOrchestrator: EngineOrchestrator? = null) {
    private val navigation = GameNavigation()
    private val engineOpponent: EngineOpponent? = engineOrchestrator?.let { EngineOpponent(it) }
    private val opponent: OpponentStrategy = engineOpponent ?: NoOpOpponent
    private val session = BoardSession(navigation = navigation, opponent = opponent)
    private lateinit var game: Game

    fun get(): BoardSession = session
    fun currentGame(): Game = game

    suspend fun load(scope: CoroutineScope, game: Game, side: Side) {
        this.game = game
        val board = GamePlayableBoard(game, side)
        navigation.load(game)
        engineOpponent?.load(game)
        opponent.prepare()
        session.load(scope, board)
    }

    suspend fun endSession() {
        session.endSession()
    }
}
