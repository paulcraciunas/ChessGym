package com.paulcraciunas.screens.common

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
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
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.theme.ChessGymTheme

enum class AppBarAlignment {
    Beginning,
    Center
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.app_name),
    titleAlign: AppBarAlignment = AppBarAlignment.Beginning,
    navButton: (@Composable AppBarScope.() -> Unit) = {},
    actions: (@Composable AppBarScope.() -> Unit) = {},
) {
    val appBarScope = remember { AppBarScope() }

    val colors = topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        titleContentColor = MaterialTheme.colorScheme.primary,
    )
    when (titleAlign) {
        AppBarAlignment.Beginning -> {
            TopAppBar(
                colors = colors,
                title = { Text(text = title) },
                navigationIcon = { navButton(appBarScope) },
                actions = { actions(appBarScope) },
                modifier = modifier
            )
        }

        AppBarAlignment.Center -> {
            CenterAlignedTopAppBar(
                colors = colors,
                title = { Text(text = title) },
                navigationIcon = { navButton(appBarScope) },
                actions = { actions(appBarScope) },
                modifier = modifier
            )
        }
    }
}

@Stable
class AppBarScope internal constructor() {
    @Composable
    fun Home(
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        IconButton(onClick = { onClick() }) {
            Icon(
                modifier = modifier,
                imageVector = Icons.Default.Menu,
                contentDescription = stringResource(R.string.nav_drawer_menu)
            )
        }
    }

    @Composable
    fun Back(
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        IconButton(onClick = { onClick() }) {
            Icon(
                modifier = modifier,
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.nav_drawer_back)
            )
        }
    }
}

@Preview("Home AppBar")
@Preview("Home AppBar (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
internal fun Preview_Home() {
    ChessGymTheme {
        AppBar { Home(onClick = {}) }
    }
}

@Preview("Home Centered AppBar")
@Preview("Home Centered (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
internal fun Preview_Home_Centered() {
    ChessGymTheme {
        AppBar(titleAlign = AppBarAlignment.Center) { Home(onClick = {}) }
    }
}


@Preview("Back AppBar")
@Preview("Back AppBar (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
internal fun Preview_Back() {
    ChessGymTheme {
        AppBar(
            title = "Back"
        ) { Back(onClick = {}) }
    }
}

@Preview("Center AppBar")
@Preview("Center AppBar (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
internal fun Preview_Center() {
    ChessGymTheme {
        AppBar(
            title = "ChessGym",
            titleAlign = AppBarAlignment.Center
        )
    }
}

@Preview("Simple AppBar")
@Preview("Simple AppBar (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
internal fun Preview_Simple() {
    ChessGymTheme {
        AppBar(
            title = "ChessGym"
        )
    }
}
