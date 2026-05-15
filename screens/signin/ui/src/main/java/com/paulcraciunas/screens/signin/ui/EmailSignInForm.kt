package com.paulcraciunas.screens.signin.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.screens.common.design.components.IconCircleButton
import com.paulcraciunas.screens.common.design.components.PrimaryButton
import com.paulcraciunas.screens.common.design.components.annotatedTextResource
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.global.resources.R as GlobalR

@Composable
internal fun EmailSignInForm(
    email: String,
    password: String,
    isSignUpMode: Boolean,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onToggleMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text(stringResource(GlobalR.string.sign_in_email_label)) },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentType = ContentType.EmailAddress },
            enabled = !isLoading,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                autoCorrectEnabled = false
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Design.colors.bg,
                unfocusedContainerColor = Design.colors.surface,
                disabledContainerColor = Design.colors.surfaceAlt
            )
        )
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text(stringResource(GlobalR.string.sign_in_password_label)) },
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentType = if (isSignUpMode) {
                        ContentType.NewPassword
                    } else {
                        ContentType.Password
                    }
                },
            enabled = !isLoading,
            singleLine = true,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                val description = if (isPasswordVisible) stringResource(GlobalR.string.sign_in_password_hide) else stringResource(GlobalR.string.sign_in_password_show)
                IconCircleButton(
                    icon = icon,
                    onClick = { isPasswordVisible = !isPasswordVisible },
                    contentDescription = description,
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                autoCorrectEnabled = false
            ),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Design.colors.bg,
                unfocusedContainerColor = Design.colors.surface,
                disabledContainerColor = Design.colors.surfaceAlt
            )
        )
        PrimaryButton(
            text = if (isSignUpMode) stringResource(GlobalR.string.sign_up_button)
            else stringResource(GlobalR.string.sign_in_button),
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Design.dimensions.spacing.sm),
            enabled = !isLoading,
        )
        TextButton(
            onClick = onToggleMode,
            enabled = !isLoading,
        ) {
            Text(
                text = if (isSignUpMode) annotatedTextResource(GlobalR.string.sign_in_switch_to_sign_in)
                else annotatedTextResource(GlobalR.string.sign_in_switch_to_sign_up),
                style = Design.typography.bodyLarge,
                color = Design.colors.inkSoft,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmailSignInFormPreview() {
    ChessGymTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            EmailSignInForm(
                email = "user@example.com",
                password = "password123",
                isSignUpMode = false,
                isLoading = false,
                onEmailChange = {},
                onPasswordChange = {},
                onSubmit = {},
                onToggleMode = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmailSignUpFormPreview() {
    ChessGymTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            EmailSignInForm(
                email = "",
                password = "",
                isSignUpMode = true,
                isLoading = false,
                onEmailChange = {},
                onPasswordChange = {},
                onSubmit = {},
                onToggleMode = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmailSignInLoadingPreview() {
    ChessGymTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            EmailSignInForm(
                email = "user@example.com",
                password = "password123",
                isSignUpMode = false,
                isLoading = true,
                onEmailChange = {},
                onPasswordChange = {},
                onSubmit = {},
                onToggleMode = {}
            )
        }
    }
}
