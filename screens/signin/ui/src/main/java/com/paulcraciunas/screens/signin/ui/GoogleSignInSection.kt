package com.paulcraciunas.screens.signin.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.screens.common.controls.InfiniteProgressIndicator
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.global.resources.R as GlobalR

@Composable
internal fun GoogleSignInSection(
    onGoogleSignIn: () -> Unit,
    isLoading: Boolean,
    signInMode: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isLoading) {
            GoogleSignInButton(onClick = onGoogleSignIn, signInMode = signInMode)
        } else {
            InfiniteProgressIndicator(modifier = Modifier.size(40.dp))
        }

        SignInDivider()
    }
}

@Composable
internal fun GoogleSignInButton(
    onClick: () -> Unit,
    signInMode: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Image(
                painter = painterResource(id = GlobalR.drawable.ic_google_logo),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = if (signInMode) GlobalR.string.sign_in_google else GlobalR.string.sign_up_google),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
private fun SignInDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HorizontalDivider(Modifier.weight(1f))
        Text(
            text = stringResource(GlobalR.string.sign_in_divider),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(Modifier.weight(1f))
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun GoogleSignInSectionPreview() {
    ChessGymTheme {
        GoogleSignInSection(
            onGoogleSignIn = {},
            signInMode = true,
            isLoading = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GoogleSignInSectionLoadingPreview() {
    ChessGymTheme {
        GoogleSignInSection(
            onGoogleSignIn = {},
            isLoading = true,
            signInMode = true,
            modifier = Modifier.padding(16.dp)
        )
    }
}
