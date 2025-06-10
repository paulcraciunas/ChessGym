package com.paulcraciunas.game.logic.impl.plies

internal class CheckPly(private val decorated: Playable) : Playable by decorated {
    override fun algebraic(): String = "${decorated.algebraic()}+"
}