package com.paulcraciunas.user.di.auth

import com.google.firebase.auth.FirebaseAuth
import com.paulcraciunas.global.qualifiers.IoDispatcher
import com.paulcraciunas.user.api.TokenProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseTokenProvider @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : TokenProvider {

    override suspend fun getToken(forceRefresh: Boolean): String? = withContext(ioDispatcher) {
        firebaseAuth.currentUser?.getIdToken(forceRefresh)?.await()?.token
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }
}
