package com.paulcraciunas.user.di.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.paulcraciunas.global.qualifiers.IoDispatcher
import com.paulcraciunas.user.api.AuthException
import com.paulcraciunas.user.api.AuthService
import com.paulcraciunas.user.api.User
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthService @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AuthService {

    override suspend fun signInWithGoogleToken(idToken: String): User.AuthenticationState =
        performAuth(User.AuthenticationState.AuthProvider.GOOGLE) {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential)
        }

    override suspend fun signInWithEmail(email: String, password: String): User.AuthenticationState =
        performAuth(User.AuthenticationState.AuthProvider.EMAIL) {
            firebaseAuth.signInWithEmailAndPassword(email, password)
        }

    override suspend fun signUpWithEmail(email: String, password: String): User.AuthenticationState =
        performAuth(User.AuthenticationState.AuthProvider.EMAIL) {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
        }

    override suspend fun deleteAccount(): Unit = withContext(ioDispatcher) {
        val user = firebaseAuth.currentUser
            ?: throw AuthException.Unknown(IllegalStateException("No signed-in user to delete"))
        try {
            user.delete().await()
        } catch (e: FirebaseNetworkException) {
            throw AuthException.NetworkError(e)
        } catch (e: Exception) {
            throw AuthException.Unknown(e)
        }
    }

    private suspend fun performAuth(
        provider: User.AuthenticationState.AuthProvider,
        action: () -> Task<AuthResult>,
    ): User.AuthenticationState = withContext(ioDispatcher) {
        try {
            val firebaseResult = action().await()
            val user = firebaseResult.user ?: throw AuthException.Unknown(
                IllegalStateException("Firebase user is null after $provider sign-in")
            )

            User.AuthenticationState(
                userId = user.uid,
                provider = provider,
            )
        } catch (e: AuthException) {
            throw e
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            throw AuthException.InvalidCredentials(e)
        } catch (e: FirebaseAuthInvalidUserException) {
            throw AuthException.UserNotFound(e)
        } catch (e: FirebaseAuthUserCollisionException) {
            throw AuthException.AccountCollision(e)
        } catch (e: FirebaseAuthWeakPasswordException) {
            throw AuthException.WeakPassword(e)
        } catch (e: FirebaseNetworkException) {
            throw AuthException.NetworkError(e)
        } catch (e: Exception) {
            throw AuthException.Unknown(e)
        }
    }
}
