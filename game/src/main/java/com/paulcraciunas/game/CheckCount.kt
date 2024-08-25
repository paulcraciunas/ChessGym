package com.paulcraciunas.game

/**
 * This is important as it informs our next potential move
 * If the King is in check from only 1 piece, some other piece might block, or capture
 * the checking piece.
 * If the king is in check from 2 pieces, then only the King can move.
 */
enum class CheckCount {
    None,
    One,
    Two;

    operator fun inc(): CheckCount = when (this) {
        None -> One
        One -> Two
        Two -> throw IllegalArgumentException("You can't be in check from more than 2 pieces")
    }
}
