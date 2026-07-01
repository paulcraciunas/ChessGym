package com.paulcraciunas.chessgym.repositories

import com.google.cloud.firestore.Firestore
import com.paulcraciunas.chessgym.models.UserDto

class FirestoreUserRepository(
    private val firestore: Firestore,
    private val mapper: UserDtoMapper = UserDtoMapper(),
) : UserRepository {

    override suspend fun findById(userId: String): UserDto? {
        val document = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .get()
            .await()

        if (!document.exists()) return null
        return mapper.fromMap(document.data ?: return null)
    }

    override suspend fun save(userId: String, user: UserDto) {
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .set(mapper.asMap(user))
            .await()
    }

    override suspend fun delete(userId: String) {
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .delete()
            .await()
    }

    override suspend fun findAll(): List<UserDto> {
        val snapshot = firestore.collection(COLLECTION_USERS)
            .get()
            .await()
        return snapshot.documents.mapNotNull { mapper.fromMap(it.data) }
    }

    companion object {
        private const val COLLECTION_USERS: String = "users"
    }
}
