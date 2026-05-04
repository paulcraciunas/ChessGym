package com.paulcraciunas.chessgym.services

import com.google.firebase.auth.FirebaseToken

interface AuthService {
    fun verifyIdToken(idToken: String): FirebaseToken
}
