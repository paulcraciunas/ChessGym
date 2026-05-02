package com.paulcraciunas.chessgym.error_reporting

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import timber.log.Timber

internal object NavigationLogger : NavController.OnDestinationChangedListener {
    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?,
    ) {
        val route = destination.route ?: "unknown"
        Timber.i("Navigated to: %s", route)
    }
}
