package com.paulcraciunas.screens.signin.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.screens.common.ChildAppBar
import com.paulcraciunas.screens.common.Footer
import com.paulcraciunas.screens.common.controls.Header
import com.paulcraciunas.screens.common.controls.HeaderAlign
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.signin.vm.AuthError
import com.paulcraciunas.screens.signin.vm.FieldErrors
import com.paulcraciunas.screens.signin.vm.SignInUiState
import com.paulcraciunas.global.resources.R as GlobalR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    uiState: SignInUiState,
    onGoogleSignIn: () -> Unit,
    onEmailSignIn: (String, String) -> Unit,
    onEmailSignUp: (String, String, String) -> Unit,
    onForgotPassword: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
    signUp: Boolean = false,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var displayName by rememberSaveable { mutableStateOf("") }
    var isSignUpMode by rememberSaveable { mutableStateOf(signUp) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onNavigateBack()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            ChildAppBar(
                onBack = onNavigateBack,
                title = stringResource(if (isSignUpMode) GlobalR.string.sign_up_title else GlobalR.string.sign_in_title),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Design.dimensions.spacing.xxl, vertical = Design.dimensions.spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedContent(
                targetState = isSignUpMode,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "HeaderTransition"
            ) { isSignUp ->
                Header(
                    eyebrowRes = if (isSignUp) GlobalR.string.sign_up_eyebrow else GlobalR.string.sign_in_eyebrow,
                    titleRes = GlobalR.string.sign_in_welcome,
                    subtitleRes = if (isSignUp) GlobalR.string.sign_up_subtitle else GlobalR.string.sign_in_subtitle,
                    align = HeaderAlign.Centered,
                )
            }
            GeneralErrorBanner(error = uiState.generalError)
            GoogleSignInSection(
                onGoogleSignIn = onGoogleSignIn,
                isLoading = uiState.isLoading,
                signInMode = !isSignUpMode,
            )
            EmailSignInForm(
                email = email,
                password = password,
                displayName = displayName,
                isSignUpMode = isSignUpMode,
                isLoading = uiState.isLoading,
                fieldErrors = uiState.fieldErrors,
                isPasswordResetSent = uiState.isPasswordResetSent,
                onEmailChange = {
                    email = it
                    onClearError()
                },
                onPasswordChange = {
                    password = it
                    onClearError()
                },
                onDisplayNameChange = {
                    displayName = it
                    onClearError()
                },
                onSubmit = {
                    if (!uiState.isLoading) {
                        if (isSignUpMode) onEmailSignUp(displayName, email, password)
                        else onEmailSignIn(email, password)
                    }
                },
                onToggleMode = { isSignUpMode = !isSignUpMode },
                onForgotPassword = { onForgotPassword(email) },
            )
            Footer()
        }
    }
}

@Composable
private fun GeneralErrorBanner(
    error: AuthError?,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(visible = error != null) {
        if (error != null) {
            Row(
                modifier = modifier
                    .testTag(SignInTags.GENERAL_ERROR_BANNER),
                horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = stringResource(error.toMessageRes()),
                    style = Design.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SignInScreenIdlePreview() {
    ChessGymTheme {
        SignInScreen(
            uiState = SignInUiState(),
            onGoogleSignIn = {},
            onEmailSignIn = { _, _ -> },
            onEmailSignUp = { _, _, _ -> },
            onForgotPassword = {},
            onNavigateBack = {},
            onClearError = {},
        )
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SignUpScreenIdlePreview() {
    ChessGymTheme {
        SignInScreen(
            uiState = SignInUiState(),
            onGoogleSignIn = {},
            onEmailSignIn = { _, _ -> },
            onEmailSignUp = { _, _, _ -> },
            onForgotPassword = {},
            onNavigateBack = {},
            onClearError = {},
            signUp = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SignInScreenWithErrorsPreview() {
    ChessGymTheme {
        SignInScreen(
            uiState = SignInUiState(
                fieldErrors = FieldErrors(
                    emailError = AuthError.INVALID_EMAIL,
                    passwordError = AuthError.PASSWORD_TOO_SHORT,
                )
            ),
            onGoogleSignIn = {},
            onEmailSignIn = { _, _ -> },
            onEmailSignUp = { _, _, _ -> },
            onForgotPassword = {},
            onNavigateBack = {},
            onClearError = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SignInScreenGeneralErrorPreview() {
    ChessGymTheme {
        SignInScreen(
            uiState = SignInUiState(generalError = AuthError.NETWORK_ERROR),
            onGoogleSignIn = {},
            onEmailSignIn = { _, _ -> },
            onEmailSignUp = { _, _, _ -> },
            onForgotPassword = {},
            onNavigateBack = {},
            onClearError = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SignInScreenPasswordResetSentPreview() {
    ChessGymTheme {
        SignInScreen(
            uiState = SignInUiState(isPasswordResetSent = true),
            onGoogleSignIn = {},
            onEmailSignIn = { _, _ -> },
            onEmailSignUp = { _, _, _ -> },
            onForgotPassword = {},
            onNavigateBack = {},
            onClearError = {},
        )
    }
}
