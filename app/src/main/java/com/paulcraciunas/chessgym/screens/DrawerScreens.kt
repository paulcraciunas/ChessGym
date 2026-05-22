package com.paulcraciunas.chessgym.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManagerFactory
import com.paulcraciunas.chessgym.R
import com.paulcraciunas.chessgym.auth.GoogleTokenSource
import com.paulcraciunas.chessgym.navigation.Screen
import com.paulcraciunas.domain.api.billing.BillingUseCase
import com.paulcraciunas.global.billing.PlayStoreDonate
import com.paulcraciunas.screens.about.ui.AboutDetailScreen
import com.paulcraciunas.screens.about.ui.AboutScreen
import com.paulcraciunas.screens.about.vm.AboutEvent
import com.paulcraciunas.screens.about.vm.AboutSection
import com.paulcraciunas.screens.about.vm.AboutViewModel
import com.paulcraciunas.screens.signin.ui.SignInScreen
import com.paulcraciunas.screens.signin.vm.SignInViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import com.paulcraciunas.global.resources.R as GlobalR

@Composable
internal fun SignIn(tabNavController: NavHostController) {
    val vm: SignInViewModel = hiltViewModel()
    val signInState by vm.uiState.collectAsStateWithLifecycle()
    val webClientId = stringResource(R.string.default_web_client_id)
    val context = LocalContext.current
    SignInScreen(
        uiState = signInState,
        onGoogleSignIn = { vm.onGoogleSignIn(GoogleTokenSource(webClientId, context)) },
        onEmailSignIn = vm::onEmailSignIn,
        onEmailSignUp = vm::onEmailSignUp,
        onForgotPassword = vm::onForgotPassword,
        onNavigateBack = tabNavController::popBackStack,
        onClearError = {
            vm.clearError()
            vm.clearFieldErrors()
        },
    )
}

@Composable
internal fun About(tabNavController: NavHostController) {
    val vm: AboutViewModel = hiltViewModel()
    val aboutState by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = LocalActivity.current
    val snackbarHostState = remember { SnackbarHostState() }
    val thankYouMessage = stringResource(GlobalR.string.generic_thank_you)

    LaunchedEffect(Unit) {
        launch {
            vm.uiEvents.collect { event ->
                when (event) {
                    AboutEvent.RateTheApp -> {
                        if (activity == null) return@collect
                        val manager = ReviewManagerFactory.create(context)
                        try {
                            val reviewInfo = manager.requestReview()
                            manager.launchReview(activity, reviewInfo)
                        } catch (e: Exception) {
                            Timber.w(e, "Failed to open Play Store ReviewManager")
                            openPlayStoreDirectly(context)
                        }
                    }
                    is AboutEvent.Donate -> {
                        if (activity == null) return@collect
                        vm.billingUseCase.donate(event = PlayStoreDonate(activity = activity, type = event.product))
                    }
                }
            }
        }
        launch {
            vm.billingUseCase.events.collect { event ->
                when (event) {
                    is BillingUseCase.PurchaseEvent.Success -> {
                        snackbarHostState.showSnackbar(thankYouMessage)
                    }
                    is BillingUseCase.PurchaseEvent.Error -> {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
            }
        }
    }

    AboutScreen(
        onNavigateBack = tabNavController::popBackStack,
        onSectionClicked = { section ->
            tabNavController.navigate(Screen.AboutDetail(section = section.name))
        },
        interactions = vm,
        showDonationDialog = aboutState.showDonationDialog,
        snackbarHostState = snackbarHostState,
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

private fun openPlayStoreDirectly(context: Context) {
    val playStoreAppUri = "market://details?id=${context.packageName}".toUri()
    val playStoreWebUri = "https://play.google.com/store/apps/details?id=${context.packageName}".toUri()

    try {
        val appIntent = Intent(Intent.ACTION_VIEW, playStoreAppUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        }
        context.startActivity(appIntent)
    } catch (e: ActivityNotFoundException) {
        Timber.w(e, "Failed to open Google Play Store application page")
        // Fallback to the web browser if "market://" fails
        val webIntent = Intent(Intent.ACTION_VIEW, playStoreWebUri)
        context.startActivity(webIntent)
    }
}
