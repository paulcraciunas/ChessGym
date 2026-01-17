package com.paulcraciunas.game.logic.impl

import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.PuzzleNavigation

// TODO Paul: Temporary implementation
// FIXME: Paul, complete the implementation by exposing current state and info
internal class SinglePuzzleNavigation : PuzzleNavigation {
    private var navigationRoot: PuzzleNavigation.HistoryItem? = null
    private var navigationEnd: PuzzleNavigation.HistoryItem? = null
    private var navigation: PuzzleNavigation.HistoryItem? = null

    private var _puzzle: MutablePuzzle? = null
    private val puzzle: MutablePuzzle
        get() = _puzzle!!

    override fun start(puzzle: Puzzle) {
        _puzzle = puzzle as MutablePuzzle

        navigationRoot = PuzzleNavigation.HistoryItem(
            info = puzzle.info,
            board = puzzle.board,
        )
    }

    override fun update() {
        addItemToHistory()
    }

    override fun resign() {
        addSolutionToHistory()
    }

    override fun isNavigable(): Boolean = navigationEnd != null

    override fun canNavigate(to: PuzzleNavigation.Navigation): Boolean =
        isNavigable() && when (to) {
            PuzzleNavigation.Navigation.START,
            PuzzleNavigation.Navigation.BACK -> navigation != navigationRoot
            PuzzleNavigation.Navigation.NEXT -> navigation!!.mainChild != null
            PuzzleNavigation.Navigation.END -> navigation != navigationEnd
        }

    override fun navigate(to: PuzzleNavigation.Navigation) {
        assert(canNavigate(to))
        navigation = when (to) {
            PuzzleNavigation.Navigation.START -> navigationRoot
            PuzzleNavigation.Navigation.BACK -> navigation!!.parent
            PuzzleNavigation.Navigation.NEXT -> navigation!!.mainChild
            PuzzleNavigation.Navigation.END -> navigationEnd
        }
    }

    private fun addItemToHistory() {
        val item = PuzzleNavigation.HistoryItem(
            info = puzzle.info,
            board = puzzle.board,
        )
        item.parent = navigation
        if (puzzle.state == Puzzle.State.Failed) {
            navigation!!.otherChildren = mutableListOf(item)
            addSolutionToHistory()
        } else {
            navigation!!.mainChild = item
        }
        navigation = item
        if (puzzle.state == Puzzle.State.Success) {
            navigationEnd = navigation
        }
    }

    private fun addSolutionToHistory() {
        var currentRoot = navigation

        while (puzzle.isRunning()) {
            puzzle.playNextMove()
            val item = PuzzleNavigation.HistoryItem(
                info = puzzle.info,
                board = puzzle.board,
            )
            item.parent = currentRoot
            currentRoot!!.mainChild = item
            currentRoot = item
        }
        navigationEnd = currentRoot
    }
}