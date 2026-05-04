package com.paulcraciunas.chessgym.repositories

import com.google.cloud.firestore.Firestore
import com.paulcraciunas.chessgym.models.UserDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirestoreUserRepository(
    private val firestore: Firestore,
    private val mapper: UserDtoMapper = UserDtoMapper(),
) : UserRepository {

    override suspend fun findById(userId: String): UserDto? = withContext(Dispatchers.IO) {
        val document = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .get()
            .get()

        if (!document.exists()) return@withContext null
        mapper.fromMap(document.data ?: return@withContext null)
    }

    override suspend fun save(userId: String, user: UserDto): Unit = withContext(Dispatchers.IO) {
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .set(mapper.asMap(user))
            .get()
    }

    override suspend fun delete(userId: String): Unit = withContext(Dispatchers.IO) {
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .delete()
            .get()
    }

    override suspend fun findAll(): List<UserDto> = withContext(Dispatchers.IO) {
        firestore.collection(COLLECTION_USERS)
            .get()
            .get()
            .documents
            .mapNotNull { doc -> doc.data?.let { mapper.fromMap(it) } }
    }

    companion object {
        private const val COLLECTION_USERS: String = "users"
    }
}
