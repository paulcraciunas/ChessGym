package com.paulcraciunas.chessgym.screens

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paulcraciunas.screens.about.ui.AboutDetailScreen
import com.paulcraciunas.screens.about.ui.AboutScreen
import com.paulcraciunas.screens.about.vm.AboutSection
import com.paulcraciunas.screens.about.vm.AboutViewModel

@Composable
internal fun About(
    onNavigateBack: () -> Unit,
    onSectionClicked: (AboutSection) -> Unit,
) {
    val vm: AboutViewModel = hiltViewModel()

    AboutScreen(
        onNavigateBack = onNavigateBack,
        onSectionClicked = onSectionClicked,
        interactions = vm,
    )
}

@Composable
internal fun AboutDetail(
    section: AboutSection,
    onNavigateBack: () -> Unit,
) {
    val vm: AboutViewModel = hiltViewModel()
    val aboutState by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    AboutDetailScreen(
        section = section,
        onNavigateBack = onNavigateBack,
        libraries = aboutState.libraries,
        onEmailClicked = resolveEmailUri(section)?.let { emailUri ->
            {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = emailUri.toUri()
                }
                context.startActivity(intent)
            }
        },
    )
}

private fun resolveEmailUri(section: AboutSection): String? = when (section) {
    AboutSection.CONTACT -> "mailto:contact@chessgym.app"
    AboutSection.FEEDBACK -> "mailto:feedback@chessgym.app"
    else -> null
}
