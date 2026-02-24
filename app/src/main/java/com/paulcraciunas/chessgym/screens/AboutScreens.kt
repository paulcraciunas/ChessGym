package com.paulcraciunas.chessgym.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paulcraciunas.screens.about.ui.AboutScreen
import com.paulcraciunas.screens.about.vm.AboutViewModel

@Composable
internal fun About(
    onNavigateBack: () -> Unit,
) {
    val vm: AboutViewModel = hiltViewModel()
    val aboutState by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    AboutScreen(
        uiState = aboutState,
        onNavigateBack = onNavigateBack,
        onContactEmail = {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:contact@chessgym.app")
            }
            context.startActivity(intent)
        },
        onFeedbackEmail = {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:feedback@chessgym.app")
            }
            context.startActivity(intent)
        },
        interactions = vm,
    )
}
