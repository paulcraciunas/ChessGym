package com.paulcraciunas.screens.common

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import kotlin.math.roundToInt

enum class AppBarAlignment {
    Beginning,
    Center
}

object AppBarTags {
    const val BACK_BUTTON = "app_bar_back_button"
    const val HOME_BUTTON = "app_bar_home_button"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.app_name),
    titleAlign: AppBarAlignment = AppBarAlignment.Beginning,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navButton: (@Composable AppBarScope.() -> Unit) = {},
    actions: (@Composable () -> Unit) = {},
) {
    val appBarScope = remember { AppBarScope() }
    val containerColor = Design.colors.surface
    val dividerColor = Design.colors.divider

    val colors = topAppBarColors(
        containerColor = containerColor,
        titleContentColor = Design.colors.primary,
        scrolledContainerColor = containerColor,
    )

    StatusBarAppBarLayout(
        scrollBehavior = scrollBehavior,
        containerColor = containerColor,
        dividerColor = dividerColor,
        modifier = modifier,
    ) {
        when (titleAlign) {
            AppBarAlignment.Beginning -> TopAppBar(
                colors = colors,
                title = { AppBarTitle(title) },
                navigationIcon = { navButton(appBarScope) },
                actions = { actions() },
                modifier = Modifier.fillMaxWidth(),
                windowInsets = WindowInsets(0, 0, 0, 0),
            )

            AppBarAlignment.Center -> CenterAlignedTopAppBar(
                colors = colors,
                title = { AppBarTitle(title) },
                navigationIcon = { navButton(appBarScope) },
                actions = { actions() },
                modifier = Modifier.fillMaxWidth(),
                windowInsets = WindowInsets(0, 0, 0, 0),
            )
        }
    }
}

/**
 * Wraps the inner [TopAppBar] and adds the status bar height above it.
 * Owns the [scrollBehavior]'s height-offset so the **full** height —
 * including the status-bar area — is collapsible on scroll.
 *
 * The inner TopAppBar must use `windowInsets = WindowInsets(0,0,0,0)` and
 * must NOT receive the [scrollBehavior] itself (this layout drives it).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusBarAppBarLayout(
    scrollBehavior: TopAppBarScrollBehavior?,
    containerColor: Color,
    dividerColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val statusBarTopPx = WindowInsets.statusBars.getTop(density)

    Layout(
        content = content,
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .drawBehind {
                drawRect(containerColor)
                val strokeWidth = 1.dp.toPx()
                val y = size.height - strokeWidth / 2
                drawLine(
                    color = dividerColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth,
                )
            },
    ) { measurables, constraints ->
        val placeable = measurables.first().measure(constraints)
        val totalHeight = statusBarTopPx + placeable.height

        if (scrollBehavior != null) {
            val limit = -totalHeight.toFloat()
            if (scrollBehavior.state.heightOffsetLimit != limit) {
                scrollBehavior.state.heightOffsetLimit = limit
            }
        }

        val offset = scrollBehavior?.state?.heightOffset?.roundToInt() ?: 0
        val visibleHeight = (totalHeight + offset).coerceAtLeast(0)

        layout(constraints.maxWidth, visibleHeight) {
            placeable.place(0, statusBarTopPx + offset)
        }
    }
}

@Composable
private fun AppBarTitle(title: String) {
    Text(
        text = title,
        style = Design.typography.titleLarge,
        color = Design.colors.ink,
    )
}

@Stable
class AppBarScope internal constructor() {
    @Composable
    fun Home(
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        IconButton(
            onClick = { onClick() },
            modifier = modifier.padding(horizontal = Design.dimensions.spacing.xl)
                .size(Design.dimensions.sizes.iconButton)
                .testTag(AppBarTags.HOME_BUTTON)
        ) {
            Icon(
                modifier = modifier,
                imageVector = Icons.Default.Menu,
                contentDescription = stringResource(R.string.nav_drawer_menu),
                tint = Design.colors.ink,
            )
        }
    }

    @Composable
    fun Back(
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        IconButton(
            onClick = { onClick() },
            modifier = modifier.padding(horizontal = Design.dimensions.spacing.xl)
                .size(Design.dimensions.sizes.iconButton)
                .testTag(AppBarTags.BACK_BUTTON)
        ) {
            Icon(
                modifier = modifier,
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.nav_drawer_back),
                tint = Design.colors.ink,
            )
        }
    }
}

@Preview("Home AppBar")
@Preview("Home AppBar (dark)", uiMode = UI_MODE_NIGHT_YES)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Preview_Home() {
    ChessGymTheme {
        AppBar(
            navButton = { Home(onClick = {}) }
        )
    }
}

@Preview("Home Centered AppBar")
@Preview("Home Centered (dark)", uiMode = UI_MODE_NIGHT_YES)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Preview_Home_Centered() {
    ChessGymTheme {
        AppBar(
            titleAlign = AppBarAlignment.Center,
            navButton = { Home(onClick = {}) }
        )
    }
}

@Preview("Back AppBar")
@Preview("Back AppBar (dark)", uiMode = UI_MODE_NIGHT_YES)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Preview_Back() {
    ChessGymTheme {
        AppBar(
            title = "Back",
            navButton = { Back(onClick = {}) }
        )
    }
}

@Preview("Center AppBar")
@Preview("Center AppBar (dark)", uiMode = UI_MODE_NIGHT_YES)
@OptIn(ExperimentalMaterial3Api::class)
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Preview_Simple() {
    ChessGymTheme {
        AppBar(
            title = "ChessGym"
        )
    }
}
