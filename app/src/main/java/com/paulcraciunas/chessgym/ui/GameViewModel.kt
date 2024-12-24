package com.paulcraciunas.chessgym.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.chessgym.ui.model.BoardViewDataBuilder
import com.paulcraciunas.game.logic.Game
import com.paulcraciunas.game.logic.Settings
import com.paulcraciunas.game.logic.board.File
import com.paulcraciunas.game.logic.board.Locus
import com.paulcraciunas.game.logic.board.Rank
import com.paulcraciunas.game.logic.plies.Ply
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(
    private var game: Game = Game(settings = Settings())
) : ViewModel() {
    private var selection: Locus? = null

    private val boardData = MutableStateFlow(BoardViewDataBuilder(game.board()).build())
    val boardState = boardData.stateIn( // UI state exposed to the UI
        viewModelScope,
        SharingStarted.Eagerly,
        boardData.value
    )

    fun onClick(rank: Rank, file: File) {
        viewModelScope.launch {
            select(rank, file)
            boardData.update {
                BoardViewDataBuilder(game.board()).apply {
                    selection?.let {
                        withSelection(it, game.playablePlies(it).map(Ply::to))
                    }
                    game.state().lastPly?.let {
                        withLastMove(it.from, it.to)
                    }
                }.build()
            }
        }
    }

    private fun select(rank: Rank, file: File) {
        val at = Locus(file, rank)

        selection?.let { from -> // If we have a piece already selected
            // and can move to the new destination
            game.playablePlies(from).firstOrNull { it.to == at }?.let { ply ->
                if (game.requiresPromotion(ply)) {
                    // TODO Paul: show a dialog to select promotion
                } else {
                    game.play(ply)
                }
            }
            selection = null // whether we move or not, clear the selection
        } ?: markSelected(rank, file)
    }

    private fun markSelected(rank: Rank, file: File) {
        // only select if we click on an actual piece
        boardData.value.squares[rank.dec()][file.dec()].piece?.let {
            if (it.side == game.turn()) { // and the piece is of our turn
                selection = Locus(file, rank)
            }
        }
    }
}
