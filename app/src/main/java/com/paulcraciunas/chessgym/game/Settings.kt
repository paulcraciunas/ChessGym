package com.paulcraciunas.chessgym.game

class Settings(
    val autoPromote: Boolean = true,
    val drawByMoveRuleCount: Int = 50,
    val drawByRepetitionCount: Int = 3,
)
