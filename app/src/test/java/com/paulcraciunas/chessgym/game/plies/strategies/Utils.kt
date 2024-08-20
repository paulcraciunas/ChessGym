package com.paulcraciunas.chessgym.game.plies.strategies

import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.board.File
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece
import com.paulcraciunas.chessgym.game.board.Rank
import com.paulcraciunas.chessgym.game.plies.Ply
import com.paulcraciunas.chessgym.game.plies.StandardPly
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

internal fun invalidLocations(home: Locus, except: List<Locus> = emptyList()) =
    mutableListOf<Locus>().apply {
        File.entries.forEach { file ->
            Rank.entries.forEach { rank ->
                val it = Locus(file, rank)
                if (home != it && !except.contains(it)) {
                    add(it)
                }
            }
        }
    }

internal fun Collection<Ply>.assertMoves(
    turn: Side,
    piece: Piece,
    home: Locus,
    locations: List<Locus>,
) {
    assertEquals(locations.size, size)
    locations.forEach { dest ->
        assertNotNull(find { ply ->
            ply is StandardPly &&
            ply.turn == turn &&
            ply.piece == piece &&
            ply.from == home &&
            ply.to == dest
        })
    }
}

internal fun Collection<Ply>.assertNoMoves() {
    assertTrue(isEmpty())
}
