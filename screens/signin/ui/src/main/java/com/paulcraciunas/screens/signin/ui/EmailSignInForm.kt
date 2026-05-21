package com.paulcraciunas.screens.signin.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
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
import com.paulcraciunas.screens.signin.vm.AuthError
import com.paulcraciunas.screens.signin.vm.FieldErrors
import com.paulcraciunas.global.resources.R as GlobalR

@Composable
internal fun EmailSignInForm(
    email: String,
    password: String,
    displayName: String,
    isSignUpMode: Boolean,
    isLoading: Boolean,
    fieldErrors: FieldErrors,
    isPasswordResetSent: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onToggleMode: () -> Unit,
    onForgotPassword: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(visible = isSignUpMode) {
            OutlinedTextField(
                value = displayName,
                onValueChange = onDisplayNameChange,
                label = { Text(stringResource(GlobalR.string.sign_in_display_name_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentType = ContentType.Username }
                    .testTag(SignInTags.DISPLAY_NAME_FIELD),
                enabled = !isLoading,
                singleLine = true,
                isError = fieldErrors.displayNameError != null,
                supportingText = fieldErrors.displayNameError?.let { error ->
                    { ErrorText(error) }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Words,
                    autoCorrectEnabled = false,
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Design.colors.bg,
                    unfocusedContainerColor = Design.colors.surface,
                    disabledContainerColor = Design.colors.surfaceAlt
                )
            )
        }
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text(stringResource(GlobalR.string.sign_in_email_label)) },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentType = ContentType.EmailAddress }
                .testTag(SignInTags.EMAIL_FIELD),
            enabled = !isLoading,
            singleLine = true,
            isError = fieldErrors.emailError != null,
            supportingText = fieldErrors.emailError?.let { error ->
                { ErrorText(error) }
            },
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
                }
                .testTag(SignInTags.PASSWORD_FIELD),
            enabled = !isLoading,
            singleLine = true,
            isError = fieldErrors.passwordError != null,
            supportingText = fieldErrors.passwordError?.let { error ->
                { ErrorText(error) }
            },
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
        AnimatedVisibility(visible = !isSignUpMode) {
            TextButton(
                onClick = onForgotPassword,
                enabled = !isLoading,
                modifier = Modifier
                    .align(Alignment.End)
                    .testTag(SignInTags.FORGOT_PASSWORD_BUTTON),
            ) {
                Text(
                    text = stringResource(GlobalR.string.sign_in_forgot_password),
                    style = Design.typography.bodyMedium,
                    color = Design.colors.inkSoft,
                )
            }
        }
        PasswordResetBanner(isVisible = isPasswordResetSent)
        PrimaryButton(
            text = if (isSignUpMode) stringResource(GlobalR.string.sign_up_button)
            else stringResource(GlobalR.string.sign_in_button),
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Design.dimensions.spacing.sm)
                .testTag(SignInTags.SUBMIT_BUTTON),
            enabled = !isLoading,
        )
        TextButton(
            onClick = onToggleMode,
            enabled = !isLoading,
            modifier = Modifier.testTag(SignInTags.TOGGLE_MODE_BUTTON),
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

@Composable
private fun PasswordResetBanner(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(visible = isVisible) {
        Row(
            modifier = modifier
                .testTag(SignInTags.PASSWORD_RESET_BANNER),
            horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircleOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(GlobalR.string.auth_error_password_reset_sent),
                style = Design.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun ErrorText(error: AuthError) {
    Text(
        text = stringResource(error.toMessageRes()),
        color = MaterialTheme.colorScheme.error,
    )
}

object SignInTags {
    const val DISPLAY_NAME_FIELD = "sign_in_display_name"
    const val EMAIL_FIELD = "sign_in_email"
    const val PASSWORD_FIELD = "sign_in_password"
    const val SUBMIT_BUTTON = "sign_in_submit"
    const val TOGGLE_MODE_BUTTON = "sign_in_toggle_mode"
    const val FORGOT_PASSWORD_BUTTON = "sign_in_forgot_password"
    const val GENERAL_ERROR_BANNER = "sign_in_general_error"
    const val PASSWORD_RESET_BANNER = "sign_in_password_reset_sent"
}

@Preview(showBackground = true)
@Composable
private fun EmailSignInFormPreview() {
    ChessGymTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            EmailSignInForm(
                email = "user@example.com",
                password = "password123",
                displayName = "",
                isSignUpMode = false,
                isLoading = false,
                fieldErrors = FieldErrors(),
                isPasswordResetSent = false,
                onEmailChange = {},
                onPasswordChange = {},
                onDisplayNameChange = {},
                onSubmit = {},
                onToggleMode = {},
                onForgotPassword = {},
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
                displayName = "",
                isSignUpMode = true,
                isLoading = false,
                fieldErrors = FieldErrors(),
                isPasswordResetSent = false,
                onEmailChange = {},
                onPasswordChange = {},
                onDisplayNameChange = {},
                onSubmit = {},
                onToggleMode = {},
                onForgotPassword = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmailSignInFormWithErrorsPreview() {
    ChessGymTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            EmailSignInForm(
                email = "bad-email",
                password = "123",
                displayName = "A",
                isSignUpMode = true,
                isLoading = false,
                fieldErrors = FieldErrors(
                    displayNameError = AuthError.DISPLAY_NAME_TOO_SHORT,
                    emailError = AuthError.INVALID_EMAIL,
                    passwordError = AuthError.PASSWORD_TOO_SHORT,
                ),
                isPasswordResetSent = false,
                onEmailChange = {},
                onPasswordChange = {},
                onDisplayNameChange = {},
                onSubmit = {},
                onToggleMode = {},
                onForgotPassword = {},
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
                displayName = "",
                isSignUpMode = false,
                isLoading = true,
                fieldErrors = FieldErrors(),
                isPasswordResetSent = false,
                onEmailChange = {},
                onPasswordChange = {},
                onDisplayNameChange = {},
                onSubmit = {},
                onToggleMode = {},
                onForgotPassword = {},
            )
        }
    }
}
