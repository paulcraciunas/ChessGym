package com.paulcraciunas.chessgym.debug

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import javax.inject.Inject

class NoOpDebugMenuProvider @Inject constructor() : DebugMenuProvider {
    @Composable
    override fun ColumnScope.DrawerContent(closeDrawer: () -> Unit, onNavigate: (Any) -> Unit) {
        // No debug content in release builds
    }

    override fun NavGraphBuilder.registerDebugScreens(
        navController: NavHostController,
        showBorders: Boolean,
        highlightLegalMoves: Boolean,
        enableAnimations: Boolean,
    ) {
        // No debug screens in release builds
    }
}
