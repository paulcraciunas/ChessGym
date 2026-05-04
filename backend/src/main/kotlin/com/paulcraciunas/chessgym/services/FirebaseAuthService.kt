package com.paulcraciunas.chessgym.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken

class FirebaseAuthService : AuthService {
    override fun verifyIdToken(idToken: String): FirebaseToken =
        FirebaseAuth.getInstance().verifyIdToken(idToken)
}
