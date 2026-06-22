package com.paulcraciunas.screens.tools.importgame.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.tools.importgame.vm.ImportGameUiState

@Composable
internal fun ImportGameUiState.PlayerInfo.topName(orientation: Side): String =
    if (orientation == Side.WHITE)
        blackName ?: stringResource(R.string.clock_black)
    else whiteName ?: stringResource(R.string.clock_white)

@Composable
internal fun ImportGameUiState.PlayerInfo.bottomName(orientation: Side): String =
    if (orientation == Side.WHITE)
        whiteName ?: stringResource(R.string.clock_white)
    else blackName ?: stringResource(R.string.clock_black)
