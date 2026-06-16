package com.paulcraciunas.screens.data

import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

abstract class AbstractBoardSession {
    abstract val navigation: NavigationStrategy
    abstract val solution: SolutionStrategy
    abstract val opponent: OpponentStrategy

    private var abandoned = false
    fun markAbandoned() { abandoned = true }
    fun wasAbandoned(): Boolean = abandoned

    // Main API
    abstract fun current(): BoardState
    abstract fun clear(): BoardState
    abstract fun onClick(selection: Locus): BoardState
    abstract fun autoPromote(enabled: Boolean)
    abstract fun promote(to: Piece, at: Locus): BoardState
    abstract fun promoteIfPending(to: Piece): BoardState
    abstract fun resign(): BoardState
    abstract fun result(): SessionResult

    // Opponent API
    abstract fun canPlayOpponentMove(): Boolean
    abstract suspend fun playOpponentMove(): Boolean
    abstract suspend fun close()

    // Navigation API
    fun canNavigate(): Boolean = navigation != NoOpNavigation
    fun canUndo(): Boolean = navigation.canUndo()
    fun canReplay(): Boolean = navigation.canReplay()
    fun undoLast(): BoardState = navigate(::canUndo, navigation::undoLast)
    fun undoAll(): BoardState = navigate(::canUndo, navigation::undoAll)
    fun replayNext(): BoardState = navigate(::canReplay, navigation::replayNext)
    fun replayAll(): BoardState = navigate(::canReplay, navigation::replayAll)
    fun completedMoves(): Int = navigation.size()
    fun algebraicHistory(): String = navigation.algebraic()

    // Solution API
    abstract fun hint(): BoardState
    fun hasSolutionMoves(): Boolean = solution.hasSolutionMoves()
    fun playNextSolutionMove(): Boolean {
        if (!solution.playNextSolutionMove()) return false
        refresh(withAnimation = true)
        return true
    }

    private fun navigate(guard: () -> Boolean, action: () -> Unit): BoardState {
        if (!guard()) return current()
        action()
        return refresh()
    }

    abstract fun refresh(withAnimation: Boolean = false): BoardState
}
