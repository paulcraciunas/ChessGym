package com.paulcraciunas.screens.tools.importgame.vm

import com.paulcraciunas.logic.builders.Builders
import com.paulcraciunas.screens.data.BoardSession
import com.paulcraciunas.screens.data.GameNavigation
import com.paulcraciunas.screens.data.GamePlayableBoard
import com.paulcraciunas.serializer.api.Serializer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class ImportSession(
    private val fenSerializer: Serializer,
    private val pgnSerializer: Serializer,
) {
    private val _game = MutableStateFlow(Builders.gameFactory().builder().withDefaultBoard().buildGame())

    fun import(gameString: String, of: ImportType) {
        _game.update { serializer(of).from(gameString) }
    }

    fun createSession(): BoardSession = BoardSession(navigation = GameNavigation(_game.value))
        .load(GamePlayableBoard(game = _game.value, player = _game.value.info.turn))

    private fun serializer(importType: ImportType): Serializer = when (importType) {
        ImportType.FEN -> fenSerializer
        ImportType.PGN -> pgnSerializer
    }
}
