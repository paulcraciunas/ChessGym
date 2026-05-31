package com.paulcraciunas.chessgym.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
internal fun BottomNavigationBar(
    items: List<BottomNavItemState>,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        containerColor = Design.colors.bg,
        tonalElevation = Design.dimensions.elevation.md,
        modifier = modifier
            .testTag { BottomNavigationTags.BOTTOM_NAV_BAR }
            .shadow(
                elevation = Design.dimensions.elevation.md,
                shape = MaterialTheme.shapes.large,
                clip = true
            )
            .border(Design.colors.softBorderStroke)
    ) {
        items.forEach {
            ChessGymBottomNavItem(
                item = it.item,
                selected = it.isSelected,
                onClick = { onItemSelected(it.item) },
                modifier = Modifier.testTag { BottomNavigationTags.tagFor(it.item) }
            )
        }
    }
}

@Composable
private fun RowScope.ChessGymBottomNavItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBarItem(
        icon = {
            Icon(
                painter = item.iconPainter(),
                contentDescription = item.contentDescription(),
                modifier = Modifier.height(Design.dimensions.sizes.navBarIconHeight)
            )
        },
        label = {
            Text(
                text = item.label(),
                style = if (selected) Design.textStyles.eyebrow else Design.textStyles.label,
            )
        },
        selected = selected,
        onClick = { onClick() },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Design.colors.primary,
            unselectedIconColor = Design.colors.inkMuted,
            selectedTextColor = Design.colors.primary,
            unselectedTextColor = Design.colors.inkMuted,
            indicatorColor = Design.colors.accentSoft
        ),
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun BottomNavigationBarHomeSelectedPreview() {
    ChessGymTheme {
        BottomNavigationBar(
            items = BottomNavItemState.default(),
            onItemSelected = {}
        )
    }
}
