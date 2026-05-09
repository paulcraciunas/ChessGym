package com.paulcraciunas.chessgym.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.paulcraciunas.domain.api.auth.TokenSource
import com.paulcraciunas.user.api.AuthException
import timber.log.Timber

class GoogleTokenSource(
    private val webClientId: String,
    private val activityContext: Context,
) : TokenSource {

    override suspend fun get(): String {
        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(activityContext)
            val result = credentialManager.getCredential(activityContext, request)
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            return googleIdTokenCredential.idToken
        } catch (e: NoCredentialException) {
            throw AuthException.NoCredentials(e)
        }
    }
}
