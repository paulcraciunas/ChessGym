package com.paulcraciunas.chessgym.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.chessgym.ui.board.BoardOrientation
import com.paulcraciunas.chessgym.ui.model.BoardViewDataBuilder
import com.paulcraciunas.domain.PuzzleRepository
import com.paulcraciunas.game.io.api.PuzzleReader
import com.paulcraciunas.game.logic.Game
import com.paulcraciunas.game.logic.Puzzle
import com.paulcraciunas.game.logic.Side
import com.paulcraciunas.game.logic.board.File
import com.paulcraciunas.game.logic.board.Locus
import com.paulcraciunas.game.logic.board.Rank
import com.paulcraciunas.game.logic.plies.Ply
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

//TODO Paul: This is only temporary. Delete this and reimplement it properly
@HiltViewModel
class GameViewModel @Inject constructor(
    private val puzzleRepository: PuzzleRepository,
    private val puzzleReader: PuzzleReader
) : ViewModel() {
    private val builder = BoardViewDataBuilder()
    private var selection: Locus? = null
    private lateinit var game: Game

    // Backing state
    private val _puzzleState = MutableStateFlow<Puzzle?>(null)
    val puzzleState: StateFlow<Puzzle?> = _puzzleState.asStateFlow()
    private val boardData = MutableStateFlow(builder.build())
    private var orientation = MutableStateFlow(BoardOrientation.White)
    val boardState = boardData.stateIn( // UI state exposed to the UI
        viewModelScope,
        SharingStarted.Eagerly,
        boardData.value
    )
    val orientationState = orientation.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        orientation.value
    )

    fun loadPuzzle(targetRating: Int) {
        viewModelScope.launch {
            val puzzle = withContext(Dispatchers.IO) {
                puzzleReader.readPuzzle(puzzleRepository.getByRating(targetRating)!!.binary)
            }
            _puzzleState.value = puzzle
            game = puzzle.game
            builder.loadBoard(puzzle.board())
            boardData.update {
                builder.build()
            }
        }
    }

    fun onClick(rank: Rank, file: File) {
        viewModelScope.launch {
            select(rank, file)
            boardData.update {
                builder.loadBoard(game.board())
                selection?.let {
                    builder.withSelection(it, game.playablePlies(it).map(Ply::to))
                }
                game.state().lastPly?.let {
                    builder.withLastMove(it.from, it.to)
                }
                builder.build()
            }
            orientation.update { game.turn().toOrientation() }
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

        game.isOver()?.let {
            loadPuzzle(1200)
        }
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

fun Side.toOrientation(): BoardOrientation =
    if (this == Side.WHITE) BoardOrientation.White else BoardOrientation.Black