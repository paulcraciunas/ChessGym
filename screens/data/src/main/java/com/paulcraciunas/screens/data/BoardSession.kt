package com.paulcraciunas.screens.data

import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BoardSession(
    private val navigation: NavigationStrategy = NoOpNavigation,
    private val solution: SolutionStrategy = NoOpSolution,
    private val opponent: OpponentStrategy = NoOpOpponent,
) {
    private lateinit var playable: PlayableBoard
    private lateinit var scope: CoroutineScope
    var autoPromote: Boolean = false
    var enableAnimations: Boolean = true

    fun bindSettings(scope: CoroutineScope, settings: Flow<SessionSettings>) {
        scope.launch {
            settings.collect {
                autoPromote = it.autoPromote
                enableAnimations = it.enableAnimations
            }
        }
    }

    private val _data = MutableStateFlow(BoardState.empty)
    val data: Flow<BoardState> = _data.filter { it != BoardState.empty }
    val currentState: BoardState get() = _data.value

    internal fun load(scope: CoroutineScope, playable: PlayableBoard) {
        this.playable = playable
        this.scope = scope
        playable.initialize()
        _data.update {
            BoardState(
                rating = playable.rating,
                player = playable.player,
                id = playable.id,
                boardData = BoardViewData.from(
                    board = playable.board,
                    lastMove = playable.lastMovePly.asPair()
                ),
                movePlayed = false,
                promotion = null,
                captured = updateCaptured(),
                isOver = false,
                interactive = true,
                outcome = null,
            )
        }
        if (playable.info.turn != playable.playerSide && opponent.canPlay()) {
            _data.update { it.copy(interactive = false) }
            playOpponentMove()
        }
    }

    fun isLoaded(): Boolean = currentState != BoardState.empty
    fun toMove() = playable.info.turn

    fun onClick(selection: Locus) {
        if (!_data.value.interactive || playable.isOver()) return
        val selectableSide = if (opponent.isParticipating) playable.playerSide else playable.info.turn
        _data.update {
            val current = it.boardData.selection
            if (current != null) {
                when {
                    current == selection || !canPlay(current, selection) ->
                        it.copy(boardData = it.boardData.clearSelection(), movePlayed = false)
                    canPromote(current, selection) -> {
                        if (autoPromote) {
                            applyMove(it) { promote(from = current, to = selection, result = Piece.Queen) }
                        } else {
                            it.copy(promotion = Promotion(showChooser = true, at = selection))
                        }
                    }
                    else -> applyMove(it) { play(current, selection) }
                }
            } else if (playable.board.has(selectableSide, selection)) {
                it.copy(boardData = it.boardData.select(at = selection, moves = moves(from = selection)), movePlayed = false)
            } else {
                it
            }
        }
        afterMove()
    }

    fun promote(to: Piece, at: Locus) {
        if (!_data.value.interactive) return
        _data.update {
            applyMove(it) { promote(from = it.boardData.selection!!, to = at, result = to) }
        }
        afterMove()
    }

    fun promoteIfPending(to: Piece) {
        val at = _data.value.promotion?.at ?: return
        promote(to, at)
    }

    fun refresh(withAnimation: Boolean = false) {
        _data.update {
            it.copy(
                boardData = BoardViewData.from(
                    board = playable.board,
                    lastMove = playable.lastMovePly.asPair(),
                    withAnimation = withAnimation
                ),
                movePlayed = false,
                captured = updateCaptured(),
                isOver = playable.isOver(),
                outcome = playable.outcome(),
            )
        }
    }

    fun resign() {
        playable.resign()
        refresh()
    }

    fun completedMoves(): Int = navigation.size()
    fun algebraicHistory(): String = navigation.algebraic()
    fun canUndo(): Boolean = navigation.canUndo()
    fun canReplay(): Boolean = navigation.canReplay()
    fun undoLast() = navigate(::canUndo, navigation::undoLast)
    fun undoAll() = navigate(::canUndo, navigation::undoAll)
    fun replayNext() = navigate(::canReplay, navigation::replayNext)
    fun replayAll() = navigate(::canReplay, navigation::replayAll)

    fun hint() {
        val hintSquare = solution.hintSquare() ?: return
        _data.update {
            it.copy(boardData = it.boardData.clearSelection().select(hintSquare, moves(from = hintSquare)))
        }
    }

    internal fun playNextSolutionMove(): Boolean {
        if (!solution.playNextSolutionMove()) return false
        refresh(withAnimation = true)
        return true
    }

    suspend fun playSolution(shouldContinue: (BoardState) -> Boolean) {
        while (solution.hasSolutionMoves()) {
            delay(PuzzleSolution.SOLUTION_MOVE_DELAY_MS)
            val didPlay = playNextSolutionMove()
            if (!didPlay || !shouldContinue(_data.value)) break
        }
    }

    suspend fun endSession() {
        opponent.shutdown()
    }

    private fun afterMove() {
        if (!_data.value.movePlayed) return
        when {
            opponent.canPlay() -> {
                _data.update { it.copy(interactive = false) }
                playOpponentMove()
            }
            _data.value.isOver && enableAnimations -> {
                _data.update { it.copy(interactive = false) }
                scope.launch {
                    delay(COMPLETION_ANIMATION_WAIT_MS)
                    _data.update { it.copy(interactive = true) }
                }
            }
        }
    }

    private fun playOpponentMove() {
        scope.launch {
            opponent.playNext()
            val gameOver = playable.isOver()
            _data.update {
                it.copy(
                    boardData = BoardViewData.from(
                        board = playable.board,
                        lastMove = playable.lastMovePly.asPair(),
                        withAnimation = true
                    ),
                    movePlayed = true,
                    promotion = null,
                    captured = updateCaptured(),
                    isOver = gameOver,
                    interactive = !(gameOver && enableAnimations),
                    outcome = playable.outcome(),
                )
            }
            if (gameOver && enableAnimations) {
                delay(COMPLETION_ANIMATION_WAIT_MS)
                _data.update { it.copy(interactive = true) }
            }
        }
    }

    private inline fun applyMove(
        state: BoardState,
        boardUpdate: () -> BoardViewData,
    ): BoardState = state.copy(
        boardData = boardUpdate(),
        movePlayed = true,
        promotion = null,
        captured = updateCaptured(),
        isOver = playable.isOver(),
        interactive = !opponent.canPlay(),
        outcome = playable.outcome(),
    )

    private fun navigate(guard: () -> Boolean, action: () -> Unit) {
        if (!guard()) return
        action()
        refresh()
    }

    private fun moves(from: Locus): List<Locus> = playable.plies(from).map { it.to }
    private fun canPlay(from: Locus, to: Locus): Boolean = playable.ply(from, to) != null
    private fun canPromote(from: Locus, to: Locus): Boolean = playable.ply(from, to)?.isPromotion() ?: false

    private fun promote(from: Locus, to: Locus, result: Piece): BoardViewData {
        assert(canPromote(from, to))
        playable.ply(from, to)!!.promote(result)
        return play(from, to)
    }

    private fun play(from: Locus, to: Locus): BoardViewData {
        assert(canPlay(from, to))
        playable.play(from, to)
        return reloadBoard()
    }

    private fun reloadBoard(): BoardViewData = BoardViewData.from(
        board = playable.board,
        lastMove = playable.lastMovePly.asPair(),
        withAnimation = true
    )

    private fun updateCaptured(): CapturedPieces {
        val playerCaptured = mutableListOf<Piece>()
        val otherCaptured = mutableListOf<Piece>()
        worthSortedPieces.forEach { piece ->
            val otherCount = (piece.defaultCount - playable.board.pieces(playable.player.other(), piece).size).coerceAtLeast(0)
            val playerCount = (piece.defaultCount - playable.board.pieces(playable.player, piece).size).coerceAtLeast(0)
            repeat(otherCount) { playerCaptured.add(piece) }
            repeat(playerCount) { otherCaptured.add(piece) }
        }
        return CapturedPieces(
            byPlayer = playerCaptured.joinToString(separator = "") { it.unicode },
            byOpponent = otherCaptured.joinToString(separator = "") { it.unicode },
        )
    }
}

data class SessionSettings(
    val autoPromote: Boolean,
    val enableAnimations: Boolean,
)

private fun Ply?.asPair(): Pair<Locus, Locus>? = if (this != null) from to to else null
private val worthSortedPieces = listOf(Piece.Queen, Piece.Rook, Piece.Bishop, Piece.Knight, Piece.Pawn)
