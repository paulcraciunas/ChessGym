package com.paulcraciunas.chessgym.helpers

import com.google.firebase.auth.FirebaseToken
import com.paulcraciunas.chessgym.services.AuthService
import java.lang.reflect.Constructor

class FakeAuthService : AuthService {
    private val validTokens: MutableMap<String, TokenInfo> = mutableMapOf()

    data class TokenInfo(val uid: String, val email: String?)

    fun registerToken(token: String, uid: String, email: String? = null) {
        validTokens[token] = TokenInfo(uid, email)
    }

    fun clear() = validTokens.clear()

    override fun verifyIdToken(idToken: String): FirebaseToken {
        val info = validTokens[idToken]
            ?: throw IllegalArgumentException("Invalid token: $idToken")
        return createFirebaseToken(info.uid, info.email)
    }

    companion object {
        private fun createFirebaseToken(uid: String, email: String?): FirebaseToken {
            val claims = mutableMapOf<String, Any>(
                "sub" to uid,
                "uid" to uid,
            )
            if (email != null) claims["email"] = email

            val constructor: Constructor<FirebaseToken> =
                FirebaseToken::class.java.getDeclaredConstructor(Map::class.java)
            constructor.isAccessible = true
            return constructor.newInstance(claims)
        }
    }
}
