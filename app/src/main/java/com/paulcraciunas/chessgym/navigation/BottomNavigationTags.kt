package com.paulcraciunas.chessgym.navigation

object BottomNavigationTags {
    const val BOTTOM_NAV_BAR = "bottom_nav_bar"
    fun tagFor(item: BottomNavItem) = "bottom_nav_item_${item.name}"
}
