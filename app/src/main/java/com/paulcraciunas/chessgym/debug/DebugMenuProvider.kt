package com.paulcraciunas.chessgym.debug

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

interface DebugMenuProvider {
    @Composable
    fun ColumnScope.DrawerContent(closeDrawer: () -> Unit, onNavigate: (Any) -> Unit)

    fun NavGraphBuilder.registerDebugScreens(
        navController: NavHostController,
        showBorders: Boolean,
        highlightLegalMoves: Boolean,
        enableAnimations: Boolean,
    )
}
