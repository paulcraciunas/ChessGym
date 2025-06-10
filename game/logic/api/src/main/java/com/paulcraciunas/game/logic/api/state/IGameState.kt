package com.paulcraciunas.game.logic.api.state

import com.paulcraciunas.game.logic.api.CastleType
import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Side

/**
 * Non-computable information about the current state of the game.
 *
 * Most of the information from here is needed in various parts.
 * For example, there's no way to figure out if a player can castle just by looking at the board.
 * It's necessary to keep track of moves (e.g. king moves) which will affect future castling.
 * Pawn moves are also affected. The only way en-passent is possible is if the previous move was
 * a Pawn move 2 squares forward.
 *
 * Finally, when loading a game in-media res (e.g. from a FEN position), it's important we know
 * how many moves there have been (for computing if the game should end in a draw due to the
 * 50 move rule). In that case, since we don't have access to the move history, we need to know
 * the number of non-pawn and non-capture plies.
 */
// TODO Paul: FIXME
// This isn't needed. Moreover, the actual GameState can be moved to the Game class where it can be accessed by classes like Serializer
interface IGameState {
    val turn: Side
    val lastPly: Ply?
    val inCheckCount: CheckCount
    val whiteCastling: Set<CastleType>
    val blackCastling: Set<CastleType>
    val plieClock: Int // Since last pawn move or capture
    val moveIndex: Int

    fun castling(turn: Side): Set<CastleType>
    fun next(ply: Ply, checkCount: CheckCount): IGameState
}
