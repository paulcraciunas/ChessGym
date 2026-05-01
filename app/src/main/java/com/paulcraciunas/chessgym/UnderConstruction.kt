package com.paulcraciunas.chessgym

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.AppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UnderConstruction(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { AppBar(navButton = { Back(onClick = navController::popBackStack) }) },
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
