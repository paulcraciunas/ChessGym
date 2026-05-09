package com.paulcraciunas.screens.signin.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.Footer
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.signin.vm.SignInUiState
import com.paulcraciunas.global.resources.R as GlobalR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    uiState: SignInUiState,
    onGoogleSignIn: () -> Unit,
    onEmailSignIn: (String, String) -> Unit,
    onEmailSignUp: (String, String) -> Unit,
    onNavigateBack: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
    signUp: Boolean = false,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isSignUpMode by rememberSaveable { mutableStateOf(signUp) }
    val isLoading = uiState is SignInUiState.Loading

    LaunchedEffect(uiState) {
        when (uiState) {
            is SignInUiState.Success -> onNavigateBack()
            is SignInUiState.Error -> {
                val errorMessage = uiState.error.toMessage(context)
                snackbarHostState.showSnackbar(errorMessage)
                onClearError()
            }
            else -> {}
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            AppBar(
                title = stringResource(
                    if (isSignUpMode) GlobalR.string.sign_up_title
                    else GlobalR.string.sign_in_title
                ),
                navButton = { Back(onClick = onNavigateBack) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SignInHeader()
            GoogleSignInSection(
                onGoogleSignIn = onGoogleSignIn,
                isLoading = isLoading,
                signInMode = !isSignUpMode
            )
            EmailSignInForm(
                email = email,
                password = password,
                isSignUpMode = isSignUpMode,
                isLoading = isLoading,
                onEmailChange = { email = it },
                onPasswordChange = { password = it },
                onSubmit = {
                    if (!isLoading) {
                        if (isSignUpMode) onEmailSignUp(email, password)
                        else onEmailSignIn(email, password)
                    }
                },
                onToggleMode = { isSignUpMode = !isSignUpMode },
            )
            Footer()
        }
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SignInScreenIdlePreview() {
    ChessGymTheme {
        SignInScreen(
            uiState = SignInUiState.Idle,
            onGoogleSignIn = {},
            onEmailSignIn = { _, _ -> },
            onEmailSignUp = { _, _ -> },
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
            uiState = SignInUiState.Idle,
            onGoogleSignIn = {},
            onEmailSignIn = { _, _ -> },
            onEmailSignUp = { _, _ -> },
            onNavigateBack = {},
            onClearError = {},
            signUp = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SignInScreenLoadingPreview() {
    ChessGymTheme {
        SignInScreen(
            uiState = SignInUiState.Loading,
            onGoogleSignIn = {},
            onEmailSignIn = { _, _ -> },
            onEmailSignUp = { _, _ -> },
            onNavigateBack = {},
            onClearError = {},
        )
    }
}
