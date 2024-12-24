package com.paulcraciunas.game.logic.plies

internal class CheckPly(private val decorated: Ply) : Ply by decorated {
    override fun algebraic(): String = "${decorated.algebraic()}+"
}
