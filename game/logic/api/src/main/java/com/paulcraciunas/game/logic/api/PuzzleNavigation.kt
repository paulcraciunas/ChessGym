package com.paulcraciunas.game.logic.api

import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.state.GameInfo

interface PuzzleNavigation {
    fun start(puzzle: Puzzle)
    fun update()
    fun resign()

    fun isNavigable(): Boolean
    fun canNavigate(to: Navigation): Boolean
    fun navigate(to: Navigation)

    enum class Navigation {
        START,
        BACK,
        NEXT,
        END
    }

    class HistoryItem(
        val info: GameInfo,
        val board: IBoard,
        var parent: HistoryItem? = null,
        var mainChild: HistoryItem? = null,
        var otherChildren: MutableList<HistoryItem>? = null,
    )
}
