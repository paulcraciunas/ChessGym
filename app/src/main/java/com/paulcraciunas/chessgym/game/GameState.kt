package com.paulcraciunas.chessgym.game

import com.paulcraciunas.chessgym.game.plies.CastlePly
import com.paulcraciunas.chessgym.game.plies.Ply

data class GameState(
    val turn: Side = Side.WHITE,
    val lastPly: Ply? = null,
    val inCheckCount: CheckCount = CheckCount.None,
    val castling: Set<CastlePly.Type> = CastlePly.Type.entries.toSet(),
)
