package com.paulcraciunas.screens.data

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.algebraic

interface NavigationStrategy {
    fun size(): Int
    fun algebraic(): String

    fun canUndo(): Boolean
    fun canReplay(): Boolean
    fun undoLast()
    fun undoAll()
    fun replayNext()
    fun replayAll()
}

class GameNavigation : NavigationStrategy {
    private var game: Game? = null

    fun load(game: Game) {
        this.game = game
    }

    override fun size(): Int = (game?.historySize ?: 0) / 2
    override fun algebraic(): String = game?.history?.algebraic() ?: ""

    override fun canUndo(): Boolean = game?.canUndo() == true
    override fun canReplay(): Boolean = game?.canReplay() == true
    override fun undoLast() { game?.undoLast() }
    override fun undoAll() { game?.undoAll() }
    override fun replayNext() { game?.replayNext() }
    override fun replayAll() { game?.replayAll() }
}

object NoOpNavigation : NavigationStrategy {
    override fun size(): Int = 0
    override fun algebraic(): String = ""

    override fun canUndo(): Boolean = false
    override fun canReplay(): Boolean = false
    override fun undoLast() = Unit
    override fun undoAll() = Unit
    override fun replayNext() = Unit
    override fun replayAll() = Unit
}
