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
import androidx.core.net.toUri

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
                data = "mailto:contact@chessgym.app".toUri()
            }
            context.startActivity(intent)
        },
        onFeedbackEmail = {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:feedback@chessgym.app".toUri()
            }
            context.startActivity(intent)
        },
        interactions = vm,
    )
}
