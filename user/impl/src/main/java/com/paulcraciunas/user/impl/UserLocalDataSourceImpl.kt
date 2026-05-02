package com.paulcraciunas.user.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserLocalDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserLocalDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserLocalDataSource {
    private val dataStore: DataStore<User> = context.userDataStore

    override fun userUpdates(): Flow<User> = dataStore.data
        .catch { exception ->
            if (exception is SerializationException) {
                Timber.w(exception, "Failed to deserialize user data, resetting to default")
                clearUserData()
                emit(User())
            } else {
                Timber.w(exception, "Error reading user data from DataStore")
                throw exception
            }
        }

    override suspend fun getUser(): User {
        val user = userUpdates().first()
        if (user.deviceId.isEmpty()) {
            val initialized = user.copy(deviceId = UUID.randomUUID().toString())
            saveUser(initialized)
            return initialized
        }
        return user
    }

    override suspend fun saveUser(user: User) {
        try {
            dataStore.updateData { user }
        } catch (e: IOException) {
            Timber.w(e, "Failed to save user data")
            throw e
        }
    }

    override suspend fun updateUser(updater: (User) -> User) {
        try {
            dataStore.updateData { currentUser ->
                updater(currentUser)
            }
        } catch (e: IOException) {
            Timber.w(e, "Failed to update user data")
            throw e
        }
    }

    override suspend fun clearUserData() {
        try {
            dataStore.updateData { User() }
        } catch (e: IOException) {
            Timber.w(e, "Failed to clear user data")
            throw e
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
private object UserSerializer : Serializer<User> {
    override val defaultValue: User = User()

    override suspend fun readFrom(input: InputStream): User = try {
        Json.decodeFromStream(User.serializer(), input)
    } catch (e: SerializationException) {
        Timber.w(e, "User data corrupted, falling back to defaults")
        defaultValue
    }

    override suspend fun writeTo(t: User, output: OutputStream) {
        Json.encodeToStream( t, output)
    }
}

// DataStore property extension
private val Context.userDataStore: DataStore<User> by dataStore(
    fileName = "user_data.json",
    serializer = UserSerializer
)
