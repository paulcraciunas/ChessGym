package com.paulcraciunas.chessgym

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.chessgym.ui.theme.ChessGymTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.app_name),
    homeButton: (@Composable AppBarScope.() -> Unit)
) {
    val appBarScope = remember { AppBarScope() }

    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = { onClick() }) {
                homeButton(appBarScope)
            }
        },
        modifier = modifier
    )
}

@Stable
class AppBarScope internal constructor() {
    @Composable
    fun Home(modifier: Modifier = Modifier) {
        Icon(
            modifier = modifier,
            imageVector = Icons.Default.Menu,
            contentDescription = stringResource(R.string.nav_drawer_menu)
        )
    }

    @Composable
    fun Back(modifier: Modifier = Modifier) {
        Icon(
            modifier = modifier,
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.nav_drawer_back)
        )
    }
}

@Preview("Home AppBar")
@Preview("Home AppBar (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PreviewAppBar_Home() {
    ChessGymTheme {
        AppBar(
            onClick = {},
        ) { Home() }
    }
}

@Preview("Back AppBar")
@Preview("Back AppBar (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PreviewAppBar_Back() {
    ChessGymTheme {
        AppBar(
            onClick = {},
            title = "Back"
        ) { Back() }
    }
}
