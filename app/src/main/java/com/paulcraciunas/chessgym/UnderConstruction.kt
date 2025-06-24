package com.paulcraciunas.chessgym

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.paulcraciunas.global.resources.R

@Composable
internal fun UnderConstruction(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            AppBar(onClick = navController::popBackStack) {
                Back()
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Surface(
            modifier = modifier.padding(innerPadding),
        ) {
            Text(
                text = stringResource(R.string.under_construction),
                modifier = modifier
            )
        }
    }
}
