package com.paulcraciunas.screens.data

import androidx.compose.runtime.Immutable
import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

@Immutable
data class SessionResult(
    val id: Int?,
    val rating: Int,
    val outcome: Outcome,
) {
    val success: Boolean get() = outcome == Outcome.Won
}

class BoardSession(
    val navigation: NavigationStrategy = NoOpNavigation,
    val solution: SolutionStrategy = NoOpSolution,
    val opponent: OpponentStrategy = NoOpOpponent,
) {
    private lateinit var playable: PlayableBoard
    private lateinit var state: BoardState
    var autoPromote: Boolean = false

    fun load(playable: PlayableBoard): BoardSession = apply {
        this.playable = playable
        playable.initialize()
        state = BoardState(
            player = playable.player,
            rating = playable.rating,
            id = playable.id,
            boardData = BoardViewData.from(
                board = playable.board,
                lastMove = playable.lastMovePly.asPair(),
            ),
            movePlayed = false,
            promotion = null,
            captured = updateCaptured(),
            outcome = null,
        )
    }

    fun boardState(): BoardState = state
    fun clear(): BoardState {
        state = state.copy(boardData = state.boardData.clearSelection())
        return state
    }

    fun result(): SessionResult {
        assert(playable.outcome() != null)
        return SessionResult(id = playable.id, rating = playable.rating!!, outcome = playable.outcome()!!)
    }

    fun onClick(selection: Locus): BoardState {
        if (state.outcome != null) return state
        val current = state.boardData.selection
        state = if (current != null) {
            when {
                current == selection || !canPlay(current, selection) -> {
                    state.copy(boardData = state.boardData.clearSelection(), movePlayed = false)
                }
                canPromote(current, selection) -> {
                    if (autoPromote) {
                        state.copy(
                            boardData = promote(from = current, to = selection, result = Piece.Queen),
                            movePlayed = true,
                            captured = updateCaptured(),
                            outcome = playable.outcome(),
                        )
                    } else {
                        state.copy(promotion = Promotion(showChooser = true, at = selection))
                    }
                }
                else -> state.copy(
                    boardData = play(current, selection),
                    movePlayed = true,
                    captured = updateCaptured(),
                    outcome = playable.outcome(),
                )
            }
        } else if (playable.board.has(state.player, selection)) {
            state.copy(
                boardData = state.boardData.select(at = selection, moves = moves(from = selection)),
                movePlayed = false
            )
        } else {
            state
        }
        return state
    }

    fun promote(to: Piece, at: Locus): BoardState {
        if (state.boardData.selection == null) return state
        state = state.copy(
            boardData = promote(from = state.boardData.selection!!, to = at, result = to),
            promotion = null,
            movePlayed = true,
            captured = updateCaptured(),
            outcome = playable.outcome(),
        )
        return state
    }

    fun promoteIfPending(to: Piece): BoardState =
        state.promotion?.at?.let { promote(to, it) } ?: state

    fun canPlayOpponentMove(): Boolean = opponent.canPlay() && !playable.isPlayerTurn()
    suspend fun playOpponentMove(): Boolean {
        if (!canPlayOpponentMove()) return false
        opponent.playNext()
        state = state.copy(
            boardData = BoardViewData.from(
                board = playable.board,
                lastMove = playable.lastMovePly.asPair(),
                withAnimation = true
            ),
            movePlayed = true,
            promotion = null,
            captured = updateCaptured(),
            outcome = playable.outcome(),
        )
        return true
    }

    fun refresh(withAnimation: Boolean = false): BoardState {
        state = state.copy(
            boardData = BoardViewData.from(
                board = playable.board,
                lastMove = playable.lastMovePly.asPair(),
                withAnimation = withAnimation
            ),
            movePlayed = false,
            captured = updateCaptured(),
            outcome = playable.outcome(),
        )
        return state
    }

    suspend fun close() {
        opponent.shutdown()
    }

    fun completedMoves(): Int = navigation.size()
    fun algebraicHistory(): String = navigation.algebraic()
    fun canNavigate(): Boolean = navigation != NoOpNavigation
    fun canUndo(): Boolean = navigation.canUndo()
    fun canReplay(): Boolean = navigation.canReplay()
    fun undoLast(): BoardState = navigate(::canUndo, navigation::undoLast)
    fun undoAll(): BoardState = navigate(::canUndo, navigation::undoAll)
    fun replayNext(): BoardState = navigate(::canReplay, navigation::replayNext)
    fun replayAll(): BoardState = navigate(::canReplay, navigation::replayAll)

    fun hint(): BoardState {
        val hintSquare = solution.hintSquare() ?: return state
        state = state.copy(boardData = state.boardData.clearSelection().select(hintSquare, moves(from = hintSquare)))
        return state
    }

    fun playNextSolutionMove(): Boolean {
        if (!solution.playNextSolutionMove()) return false
        refresh(withAnimation = true)
        return true
    }

    private fun navigate(guard: () -> Boolean, action: () -> Unit): BoardState {
        if (!guard()) return state
        action()
        return refresh()
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

private fun Ply?.asPair(): Pair<Locus, Locus>? = if (this != null) from to to else null
private val worthSortedPieces = listOf(Piece.Queen, Piece.Rook, Piece.Bishop, Piece.Knight, Piece.Pawn)
