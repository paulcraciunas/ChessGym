package com.paulcraciunas.chessgym.main

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import com.paulcraciunas.screens.common.AppDrawer

@Composable
internal fun MainDrawer(
    drawerState: DrawerState,
    gesturesEnabled: Boolean,
    isSignedIn: Boolean,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onHome: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
    onDeleteAccount: () -> Unit,
    closeDrawer: () -> Unit,
    trailingContent: @Composable ColumnScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    ModalNavigationDrawer(
        drawerContent = {
            AppDrawer(
                drawerState = drawerState,
                isSignedIn = isSignedIn,
                onSignIn = onSignIn,
                onSignOut = onSignOut,
                onHome = onHome,
                onSettings = onSettings,
                onAbout = onAbout,
                onDeleteAccount = onDeleteAccount,
                closeDrawer = closeDrawer,
                trailingContent = trailingContent,
            )
        },
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled,
        content = content,
    )
}
