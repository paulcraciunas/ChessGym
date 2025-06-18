package com.paulcraciunas.chessgym.navigation

interface Navigator {
    fun navigateTo(screen: Screen)
    fun navigateBack()
}
