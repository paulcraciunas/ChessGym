package com.paulcraciunas.screens.common

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.controls.TimerDisplay
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.data.RemainingTime

object AppBarTags {
    const val BACK_BUTTON = "app_bar_back_button"
    const val HOME_BUTTON = "app_bar_home_button"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBarBase(
    modifier: Modifier = Modifier,
    appBar: (@Composable (Modifier, TopAppBarColors) -> Unit),
) {
    val dividerColor = Design.colors.divider
    val colors = topAppBarColors(
        containerColor = Design.colors.surface,
        titleContentColor = Design.colors.primary,
        scrolledContainerColor = Design.colors.surfaceAlt,
    )
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { 1.dp.toPx() }

    val appBarModifier = modifier
        .fillMaxWidth()
        .drawWithContent {
            drawContent()
            val y = size.height - (strokeWidthPx / 2f)
            drawLine(
                color = dividerColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokeWidthPx,
                cap = StrokeCap.Square
            )
        }
    appBar(appBarModifier, colors)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopLevelAppBar(
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.app_name),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: (@Composable RowScope.() -> Unit) = {},
) {
    AppBarBase(modifier = modifier) { appBarModifier, colors ->
        CenterAlignedTopAppBar(
            colors = colors,
            title = { AppBarTitle(title = title) },
            navigationIcon = { NavIcon(onClick = onHome) },
            actions = { actions() },
            modifier = appBarModifier,
            windowInsets = WindowInsets.statusBars,
            scrollBehavior = scrollBehavior,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildAppBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.app_name),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: (@Composable RowScope.() -> Unit) = {},
) {
    AppBarBase(modifier = modifier) { appBarModifier, colors ->
        TopAppBar(
            colors = colors,
            title = { AppBarTitle(title = title) },
            navigationIcon = {
                NavIcon(
                    onClick = onBack,
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescriptor = R.string.nav_drawer_back,
                    tag = AppBarTags.BACK_BUTTON,
                )
            },
            actions = { actions() },
            modifier = appBarModifier,
            windowInsets = WindowInsets.statusBars,
            scrollBehavior = scrollBehavior,
        )
    }
}

@Composable
private fun AppBarTitle(title: String) {
    Text(
        text = title,
        style = Design.typography.headlineSmall,
        color = Design.colors.ink,
    )
}

@Composable
private fun NavIcon(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageVector: ImageVector = Icons.Default.Menu,
    @StringRes contentDescriptor: Int = R.string.nav_drawer_menu,
    tag: String = AppBarTags.HOME_BUTTON,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .padding(horizontal = Design.dimensions.spacing.xl)
            .size(Design.dimensions.sizes.iconButton)
            .testTag(tag)
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = stringResource(contentDescriptor),
            tint = Design.colors.ink,
        )
    }
}

@Preview("Home AppBar")
@Preview("Home AppBar (dark)", uiMode = UI_MODE_NIGHT_YES)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Preview_Home() {
    ChessGymTheme {
        TopLevelAppBar(onHome = {})
    }
}

@Preview("Back AppBar")
@Preview("Back AppBar (dark)", uiMode = UI_MODE_NIGHT_YES)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Preview_Back() {
    ChessGymTheme {
        ChildAppBar(
            title = "Back",
            onBack = {},
        )
    }
}

@Preview("Center AppBar")
@Preview("Center AppBar (dark)", uiMode = UI_MODE_NIGHT_YES)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Preview_Center() {
    ChessGymTheme {
        ChildAppBar(
            title = "ChessGym",
            onBack = {},
            actions = { TimerDisplay(remainingTime = RemainingTime(value = "22.4", danger = false)) }
        )
    }
}
