package com.paulcraciunas.screens.data

import com.paulcraciunas.game.logic.api.Side

enum class SideSelection {
    WHITE,
    BLACK,
    RANDOM,
}

inline fun SideSelection.toSide(randomSide: () -> Int): Side = when (this) {
    SideSelection.WHITE -> Side.WHITE
    SideSelection.BLACK -> Side.BLACK
    SideSelection.RANDOM -> Side.fromCode(randomSide())
}

fun SideSelection.toSide(): Side = when (this) {
    SideSelection.WHITE -> Side.WHITE
    SideSelection.BLACK -> Side.BLACK
    SideSelection.RANDOM -> Side.WHITE
}
