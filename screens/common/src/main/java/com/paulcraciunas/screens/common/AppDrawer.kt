package com.paulcraciunas.screens.common

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
fun AppDrawer(
    drawerState: DrawerState,
    onSignIn: () -> Unit,
    onHome: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
    closeDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: @Composable ColumnScope.() -> Unit = {},
) {
    ModalDrawerSheet(
        drawerState = drawerState,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(12.dp))
            NavigationDrawerItem(
                label = {
                    Text(
                        text = stringResource(R.string.nav_drawer_sign_in),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                selected = false,
                onClick = { onSignIn(); closeDrawer() },
            )
            HorizontalDivider()
            Text(
                text = stringResource(R.string.nav_drawer_general),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium
            )
            NavigationDrawerItem(
                label = { Text(text = stringResource(R.string.nav_drawer_home)) },
                icon = { Icon(imageVector = Icons.Outlined.Home, contentDescription = null) },
                selected = false,
                onClick = { onHome(); closeDrawer() },
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = stringResource(R.string.nav_drawer_application),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium
            )
            NavigationDrawerItem(
                label = {
                    Text(text = stringResource(R.string.nav_drawer_settings))
                },
                selected = false,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = null
                    )
                },
                onClick = { onSettings(); closeDrawer() },
            )
            NavigationDrawerItem(
                label = { Text(text = stringResource(R.string.nav_drawer_about)) },
                selected = false,
                icon = { Icon(imageVector = Icons.Outlined.Info, contentDescription = null) },
                onClick = { onAbout(); closeDrawer() },
            )
            trailingContent()
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Preview("Drawer contents")
@Preview("Drawer contents (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PreviewAppDrawer() {
    ChessGymTheme {
        AppDrawer(
            drawerState = rememberDrawerState(initialValue = DrawerValue.Open),
            onSignIn = {},
            onHome = {},
            onSettings = {},
            onAbout = {},
            closeDrawer = {}
        )
    }
}
